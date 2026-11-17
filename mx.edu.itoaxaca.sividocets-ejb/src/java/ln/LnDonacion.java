package ln;

import acceso_datos.HistorialExternoFacade;
import acceso_datos.PersonalCETSFacade;
import acceso_datos.RegistroDonacionFacade;
import acceso_datos.SolicitudReposicionFacade;
import acceso_datos.UsuarioFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import modelo.HistorialExterno;
import modelo.PersonalCETS;
import modelo.RegistroDonacion;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Lógica de negocio del paquete donaciones (ControlDonacion → LnDonacion).
 *
 * Bloque [Galilea]: registro y búsqueda (HU-5.1, HU-5.4) e historial externo (HU-7.3).
 * Bloque [Diego]: métodos de consulta obtenerHistorial, obtenerElegibilidad, verificarExclusion y esElegible
 * (HU-5.2, HU-5.3). Van al final del archivo para no editar las mismas líneas.
 */
@Stateless
@LocalBean
public class LnDonacion {

    // HU-5.1 Periodo mínimo entre donaciones: 56 días (8 semanas)
    private static final int DIAS_EXCLUSION = 56;
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    @EJB
    private RegistroDonacionFacade registroF;

    @EJB
    private UsuarioFacade usuarioF;

    @EJB
    private PersonalCETSFacade personalF;

    @EJB
    private SolicitudReposicionFacade solicitudF;

    @EJB
    private HistorialExternoFacade historialExternoF;

    // ===================== [Galilea] =====================

    // HU-5.1 (1.1) registrarDonacion. r trae usuarioid (id del donador), personalCETSUsuarioid (personal en
    // sesión), fechaDonacion y, opcional, solicitudReposicionid (id o vacío = donación general; si viene, debe estar Activa).
    public boolean registrarDonacion(RegistroDonacion r) {
        // Sin donador, sin personal o sin fecha no se puede registrar (regresa false, sin excepción)
        if (r.getUsuarioid() == null || r.getUsuarioid().getId() == null
                || r.getPersonalCETSUsuarioid() == null || r.getPersonalCETSUsuarioid().getUsuarioid() == null
                || r.getFechaDonacion() == null || aLocalDate(r.getFechaDonacion()).isAfter(LocalDate.now(ZONA))) {
            return false;
        }
        Usuario donador = usuarioF.find(r.getUsuarioid().getId());
        PersonalCETS personal = personalF.find(r.getPersonalCETSUsuarioid().getUsuarioid());
        if (donador == null || personal == null || !personal.getActivo()) {
            return false;                                  // solo el personal CETS activo registra donaciones
        }
        // La solicitud es opcional (donación general); si viene, debe existir y estar Activa
        SolicitudReposicion s = r.getSolicitudReposicionid();
        SolicitudReposicion solicitud = null;
        if (s != null && s.getId() != null) {
            solicitud = solicitudF.find(s.getId());
            if (solicitud == null || !"Activa".equals(solicitud.getEstatus())) {
                return false;
            }
        }
        r.setUsuarioid(donador);
        r.setPersonalCETSUsuarioid(personal);
        r.setSolicitudReposicionid(solicitud);
        r.setProximaFechaElegible(calcularProximaFecha(r.getFechaDonacion()));   // 1.1.1.2
        registroF.create(r);                                                     // 1.1.2
        donador.getRegistroDonacionList().add(r);        // relación bidireccional al día (caché de JPA)
        return true;
    }

    // HU-5.4 (1.1) buscarDonador: criterio.nombreCompleto = texto a buscar (nombre o correo)
    public List<Usuario> buscarDonador(Usuario criterio) {
        if (criterio.getNombreCompleto() == null || criterio.getNombreCompleto().isBlank()) {
            return List.of();
        }
        return usuarioF.buscarDonadores(criterio.getNombreCompleto().trim());
    }

    // HU-5.4 (2.1) UIFichaDonador.mostrar(donadorId): lee el donador seleccionado
    public Usuario obtenerDonador(Usuario u) {
        return u.getId() == null ? null : usuarioF.find(u.getId());
    }

    // HU-7.3 (1.1) obtenerHistorialExterno
    public List<HistorialExterno> obtenerHistorialExterno(Usuario donador) {
        return historialExternoF.buscarPorUsuario(donador);
    }

    // HU-5.1 (1.1.1.2) RegistroDonacion.calcularProximaFecha: fechaDonacion + 56 días
    private Date calcularProximaFecha(Date fechaDonacion) {
        LocalDate proxima = aLocalDate(fechaDonacion).plusDays(DIAS_EXCLUSION);
        return Date.from(proxima.atStartOfDay(ZONA).toInstant());
    }

    private LocalDate aLocalDate(Date fecha) {
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZONA).toLocalDate();
    }

    // ===================== [Diego] =====================
    // HU-5.2 / HU-5.3: obtenerHistorial, obtenerElegibilidad, verificarExclusion, esElegible

    // ===== STUB TEMPORAL de [Alex] (HU-3.3) =====
    // Lo necesita LnNotificacion.registrarInteres para compilar.
    // [Diego]: al implementar HU-5.3, BORRA este bloque completo y deja tu versión real
    // (misma firma: public boolean esElegible(Usuario donador)).
    // Versión real: !exclusionPermanente && (sin donación || proximaFechaElegible <= hoy)
    public boolean esElegible(Usuario donador) {
        return true;
    }
    // ===== FIN DEL STUB =====
}
