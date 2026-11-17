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
import modelo.Hospital;
import modelo.Interes;
import modelo.InteresPK;
import modelo.Notificacion;
import modelo.PerfilClinico;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * ControlNotificacion del diagrama: HU-3.1 (notificar a donadores cercanos), HU-3.2 (radio de cercanía)
 * y HU-3.3 (confirmar interés).
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

    // Tabla de compatibilidad ABO/Rh (donador → receptores). Vive en LnCuentas (receptoresCompatibles),
    // que ya la usa miPerfil: aquí se reutiliza para no repetirla (TASKS.md, inconsistencia 28).
    @EJB
    private LnCuentas lnCuentas;

    // HU-3.3: esElegible(Usuario) lo escribe [Diego] en LnDonacion (HU-5.3)
    @EJB
    private LnDonacion lnDonacion;

    private static final double RADIO_TIERRA_KM = 6371.0;
    // La distancia se guarda en pasos de 0.5 km: es una distancia aproximada, nunca la ubicación exacta (HU-3.2)
    private static final double PASO_DISTANCIA_KM = 0.5;

    // HU-3.1 Buscar donadores cercanos. La llama LnSolicitud.registrarSolicitud al crear una solicitud.
    // s trae el id de la solicitud ya guardada. Crea un aviso por cada donador compatible y dentro de su radio.
    // No avisa si la solicitud no existe o ya no está Activa, al propio solicitante, ni dos veces al mismo
    // donador (la BD tiene UNIQUE Usuarioid + SolicitudReposicionid).
    public void buscarDonadoresCercanos(SolicitudReposicion s) {
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
            }
            // Fuera del radio: no se crea (en el diagrama, «destroy» Notificacion)
        }
    }

    // HU-3.1 Obtener un aviso. Se busca por n.id o, si no viene, por el par n.usuarioid + n.solicitudReposicionid.
    // Si n.usuarioid viene lleno, el aviso debe ser de ese usuario (nadie lee avisos ajenos). null si no existe.
    public Notificacion obtenerNotificacion(Notificacion n) {
        Notificacion encontrada = null;
        boolean traeUsuario = n.getUsuarioid() != null && n.getUsuarioid().getId() != null;
        if (n.getId() != null) {
            encontrada = notificacionF.find(n.getId());
        } else if (traeUsuario && n.getSolicitudReposicionid() != null && n.getSolicitudReposicionid().getId() != null) {
            Usuario usuario = usuarioF.find(n.getUsuarioid().getId());
            SolicitudReposicion solicitud = solicitudF.find(n.getSolicitudReposicionid().getId());
            if (usuario != null && solicitud != null) {
                encontrada = notificacionF.buscarPorUsuarioYSolicitud(usuario, solicitud);
            }
        }
        boolean ajena = encontrada != null && traeUsuario
                && !encontrada.getUsuarioid().getId().equals(n.getUsuarioid().getId());
        return ajena ? null : encontrada;
    }

    // HU-3.1 Avisos del donador (no está en el diagrama; lo necesita la vista notificaciones)
    public List<Notificacion> listarMisNotificaciones(Usuario u) {
        if (u == null || u.getId() == null) {
            return List.of();
        }
        return notificacionF.buscarPorUsuario(u);
    }

    // HU-3.3 Datos de la solicitud a la que se quiere confirmar interés (no está en el diagrama; lo necesita
    // la vista confirmarInteres). s trae el id; regresa la solicitud o null.
    public SolicitudReposicion obtenerSolicitud(SolicitudReposicion s) {
        if (s == null || s.getId() == null) {
            return null;
        }
        return solicitudF.find(s.getId());
    }

    // HU-3.3 Confirmar interés. i.usuario es el usuario en sesión (lo pone el controller) e
    // i.solicitudReposicion trae el id de la solicitud. Regresa la solicitud si el interés quedó registrado
    // y null si no se pudo: solicitud inexistente o ya no Activa, o donador no elegible (LnDonacion.esElegible).
    // Confirmar dos veces es inofensivo: no se duplica el renglón (PK compartida Usuarioid + SolicitudReposicionid).
    public SolicitudReposicion registrarInteres(Interes i) {
        boolean completo = i.getUsuario() != null && i.getUsuario().getId() != null
                && i.getSolicitudReposicion() != null && i.getSolicitudReposicion().getId() != null;
        if (!completo) {
            return null;
        }
        Usuario usuario = usuarioF.find(i.getUsuario().getId());
        SolicitudReposicion solicitud = solicitudF.find(i.getSolicitudReposicion().getId());
        if (usuario == null || solicitud == null || !"Activa".equals(solicitud.getEstatus())) {
            return null;
        }
        boolean elegible = lnDonacion.esElegible(usuario);
        if (!elegible) {
            return null;
        }
        InteresPK pk = new InteresPK(usuario.getId(), solicitud.getId());
        if (interesF.find(pk) == null) {
            i.setInteresPK(pk);
            i.setUsuario(usuario);
            i.setSolicitudReposicion(solicitud);
            i.setFechaInteres(new Date());
            interesF.create(i);
        }
        return solicitud;
    }

    // HU-3.1 PerfilClinico.validarCompatibilidad (el diagrama la pone en la entidad; aquí va en el Ln):
    // ¿la sangre del donador puede darse al receptor de la solicitud?
    private boolean validarCompatibilidad(PerfilClinico donador, SolicitudReposicion s) {
        return lnCuentas.receptoresCompatibles(donador).contains(s.getTipoSanguineo() + s.getFactorRh());
    }

    // HU-3.2 Notificacion.calcularRadioCercania (también va en el Ln). Distancia entre el donador y el hospital
    // de la solicitud (la solicitud no tiene coordenadas propias, Inconsistencias 8). Se guarda en n solo una
    // distancia aproximada, redondeada hacia arriba a 0.5 km: jamás las coordenadas. Como los radios son 3, 5
    // y 10, "aproximada <= radio" equivale a "distancia real <= radio".
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
