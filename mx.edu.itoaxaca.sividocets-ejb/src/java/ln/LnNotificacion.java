/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ln;

import acceso_datos.InteresFacade;
import acceso_datos.NotificacionFacade;
import acceso_datos.PerfilClinicoFacade;
import acceso_datos.SolicitudReposicionFacade;
import acceso_datos.UsuarioFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Hospital;
import modelo.Interes;
import modelo.InteresPK;
import modelo.Notificacion;
import modelo.PerfilClinico;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * ControlNotificacion: HU-3.1 (notificar a donadores cercanos), HU-3.2 (radio) y HU-3.3 (interés).
 *
 * @author Alex Torres
 */
@Stateless
@LocalBean
public class LnNotificacion {

    @EJB
    private PerfilClinicoFacade perfilF;

    @EJB
    private NotificacionFacade notificacionF;

    @EJB
    private InteresFacade interesF;

    @EJB
    private SolicitudReposicionFacade solicitudF;

    @EJB
    private UsuarioFacade usuarioF;

    // Compatibilidad ABO/Rh (donador → receptores): reutiliza LnCuentas.receptoresCompatibles
    // en vez de repetir la tabla (TASKS.md, inconsistencia 28).
    @EJB
    private LnCuentas lnCuentas;

    // HU-3.3: LnDonacion.esElegible(Usuario) (HU-5.3, [Diego])
    @EJB
    private LnDonacion lnDonacion;

    private static final double RADIO_TIERRA_KM = 6371.0;
    // Distancia en pasos de 0.5 km: aproximada, nunca la ubicación exacta (HU-3.2)
    private static final double PASO_DISTANCIA_KM = 0.5;

    // HU-3.1 Avisa a donadores compatibles dentro de su radio (la llama LnSolicitud.registrarSolicitud).
    // Omite: solicitud inexistente o no Activa, el solicitante y repetidos (UNIQUE usuario + solicitud).
    public void buscarDonadoresCercanos(SolicitudReposicion s) {
        try {
            avisarDonadores(s);
        } catch (RuntimeException e) {
            // Un fallo al avisar no debe tumbar el registro de la solicitud (LnSolicitud): se captura aquí
            // para que el contenedor no marque rollback.
            Logger.getLogger(LnNotificacion.class.getName()).log(Level.WARNING,
                    "No se pudieron crear los avisos de la solicitud", e);
        }
    }

    private void avisarDonadores(SolicitudReposicion s) {
        if (s == null || s.getId() == null) {
            return;
        }
        SolicitudReposicion solicitud = solicitudF.find(s.getId());
        if (solicitud == null || !"Activa".equals(solicitud.getEstatus())) {
            return;
        }
        for (PerfilClinico perfil : perfilF.buscarDonadoresActivos()) {
            Usuario donador = perfil.getUsuario();
            boolean esElSolicitante = donador.getId().equals(solicitud.getUsuarioid().getId());
            if (esElSolicitante || !validarCompatibilidad(perfil, solicitud)) {
                continue;
            }
            if (notificacionF.buscarPorUsuarioYSolicitud(donador, solicitud) != null) {
                continue;
            }
            Notificacion n = new Notificacion();
            n.setUsuarioid(donador);
            n.setSolicitudReposicionid(solicitud);
            boolean dentroRadio = calcularRadioCercania(n, perfil, solicitud);
            if (dentroRadio) {
                n.setFechaEnvio(new Date());
                n.setEstatus("Enviada");
                notificacionF.create(n);
                // Referencias inversas (#35): evitan listas desactualizadas en el caché compartido
                agregar(donador.getNotificacionList(), n);
                agregar(solicitud.getNotificacionList(), n);
            }
            // Fuera del radio: no se crea (en el diagrama, «destroy» Notificacion)
        }
    }

    // HU-3.1 Obtiene un aviso por n.id o por n.usuarioid + n.solicitudReposicionid. usuarioid es obligatorio
    // y el aviso debe ser suyo (nadie lee avisos ajenos); null si no existe.
    public Notificacion obtenerNotificacion(Notificacion n) {
        boolean traeUsuario = n != null && n.getUsuarioid() != null && n.getUsuarioid().getId() != null;
        if (!traeUsuario) {
            return null;
        }
        Notificacion encontrada = null;
        if (n.getId() != null) {
            encontrada = notificacionF.find(n.getId());
        } else if (n.getSolicitudReposicionid() != null && n.getSolicitudReposicionid().getId() != null) {
            Usuario usuario = usuarioF.find(n.getUsuarioid().getId());
            SolicitudReposicion solicitud = solicitudF.find(n.getSolicitudReposicionid().getId());
            if (usuario != null && solicitud != null) {
                encontrada = notificacionF.buscarPorUsuarioYSolicitud(usuario, solicitud);
            }
        }
        boolean ajena = encontrada != null && !encontrada.getUsuarioid().getId().equals(n.getUsuarioid().getId());
        return ajena ? null : encontrada;
    }

    // HU-3.1 Avisos del donador (lo necesita la vista notificaciones; no está en el diagrama).
    // #25: solo los de solicitudes Activas.
    public List<Notificacion> listarMisNotificaciones(Usuario u) {
        if (u == null || u.getId() == null) {
            return List.of();
        }
        return notificacionF.buscarPorUsuario(u).stream()
                .filter(n -> "Activa".equals(n.getSolicitudReposicionid().getEstatus()))
                .toList();
    }

    // HU-3.3 Solicitud a la que se quiere confirmar interés (lo necesita confirmarInteres; no está en
    // el diagrama). s trae el id; regresa la solicitud o null.
    public SolicitudReposicion obtenerSolicitud(SolicitudReposicion s) {
        if (s == null || s.getId() == null) {
            return null;
        }
        return solicitudF.find(s.getId());
    }

    // HU-3.3 ¿Puede el donador confirmar interés en la solicitud? null si sí; si no, el motivo en texto.
    // Lo usan registrarInteres y el controller (avisar y deshabilitar el botón); u y s traen su id.
    public String motivoRechazoInteres(Usuario u, SolicitudReposicion s) {
        Usuario usuario = u == null || u.getId() == null ? null : usuarioF.find(u.getId());
        SolicitudReposicion solicitud = s == null || s.getId() == null ? null : solicitudF.find(s.getId());
        if (usuario == null || solicitud == null) {
            return "La solicitud no existe.";
        }
        if (!"Activa".equals(solicitud.getEstatus())) {
            return "La solicitud ya no está activa (estatus: " + solicitud.getEstatus() + ").";
        }
        if (solicitud.getUsuarioid().getId().equals(usuario.getId())) {
            return "Esta solicitud la registraste tú; no puedes confirmar interés en ella.";
        }
        PerfilClinico perfil = perfilF.find(usuario.getId());
        if (perfil == null || perfil.getTipoSanguineo() == null || perfil.getFactorRh() == null) {
            return "Completa tu perfil clínico (grupo sanguíneo y Rh) para confirmar interés.";
        }
        if (!validarCompatibilidad(perfil, solicitud)) {
            return "Tu grupo sanguíneo (" + perfil.getTipoSanguineo() + perfil.getFactorRh()
                    + ") no es compatible con esta solicitud (" + solicitud.getTipoSanguineo()
                    + solicitud.getFactorRh() + ").";
        }
        if (!lnDonacion.esElegible(usuario)) {
            return "Todavía no cumples con los requisitos para donar (periodo de recuperación o exclusión).";
        }
        return null;
    }

    // HU-3.3 Confirmar interés: i.usuario (sesión) e i.solicitudReposicion.id. Regresa la solicitud o
    // null si no procede (motivoRechazoInteres); repetirlo no duplica el renglón (PK usuario + solicitud).
    public SolicitudReposicion registrarInteres(Interes i) {
        boolean completo = i.getUsuario() != null && i.getUsuario().getId() != null
                && i.getSolicitudReposicion() != null && i.getSolicitudReposicion().getId() != null;
        if (!completo || motivoRechazoInteres(i.getUsuario(), i.getSolicitudReposicion()) != null) {
            return null;
        }
        Usuario usuario = usuarioF.find(i.getUsuario().getId());
        SolicitudReposicion solicitud = solicitudF.find(i.getSolicitudReposicion().getId());
        InteresPK pk = new InteresPK(usuario.getId(), solicitud.getId());
        if (interesF.find(pk) == null) {
            i.setInteresPK(pk);
            i.setUsuario(usuario);
            i.setSolicitudReposicion(solicitud);
            i.setFechaInteres(new Date());
            interesF.create(i);
            // Referencias inversas (#35)
            agregar(usuario.getInteresList(), i);
            agregar(solicitud.getInteresList(), i);
        }
        return solicitud;
    }

    // Agrega al lado inverso de una relación bidireccional (null-safe)
    private <T> void agregar(List<T> lista, T elemento) {
        if (lista != null) {
            lista.add(elemento);
        }
    }

    // HU-3.1 PerfilClinico.validarCompatibilidad (va en el Ln, no en la entidad): ¿la sangre del
    // donador puede darse al receptor de la solicitud?
    private boolean validarCompatibilidad(PerfilClinico donador, SolicitudReposicion s) {
        return lnCuentas.receptoresCompatibles(donador).contains(s.getTipoSanguineo() + s.getFactorRh());
    }

    // HU-3.2 Notificacion.calcularRadioCercania (va en el Ln): distancia donador-hospital (la solicitud
    // no tiene coordenadas, Inconsistencias 8), hacia arriba a 0.5 km; con radios 3/5/10 equivale a la real.
    private boolean calcularRadioCercania(Notificacion n, PerfilClinico donador, SolicitudReposicion s) {
        Hospital hospital = s.getHospitalid();
        double km = distanciaKm(donador.getLatitud().doubleValue(), donador.getLongitud().doubleValue(),
                hospital.getLatitud().doubleValue(), hospital.getLongitud().doubleValue());
        double aproximada = Math.max(PASO_DISTANCIA_KM,
                Math.ceil(km / PASO_DISTANCIA_KM) * PASO_DISTANCIA_KM);
        n.setDistanciaAproximada(BigDecimal.valueOf(aproximada).setScale(2, RoundingMode.HALF_UP));
        return aproximada <= donador.getRadioNotificacion();
    }

    // Fórmula de haversine (distancia sobre la esfera terrestre, en km)
    private double distanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
