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
import modelo.RegistroDonacion;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Lógica de negocio del paquete donaciones (ControlDonacion → LnDonacion).
 * HU-5.1 Capturar donación, HU-5.4 Historial de un donador,
 * HU-7.3 Historial recibido de institución externa.
 */
@Stateless
@LocalBean
public class LnDonacion {

    // HU-5.3 Periodo mínimo entre donaciones (valor de prueba)
    public static final int SEMANAS_EXCLUSION = 8;
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

    // HU-5.1 r trae usuarioid (donador), personalCETSUsuarioid (quien captura),
    // fechaDonacion y, opcional, solicitudReposicionid. Regresa false si algo no es válido.
    public boolean registrarDonacion(RegistroDonacion r) {
        Usuario donador = usuarioF.find(r.getUsuarioid().getId());
        if (donador == null || r.getFechaDonacion() == null
                || aLocalDate(r.getFechaDonacion()).isAfter(hoy())) {
            return false;
        }
        r.setUsuarioid(donador);
        r.setPersonalCETSUsuarioid(personalF.find(r.getPersonalCETSUsuarioid().getUsuarioid()));
        if (r.getPersonalCETSUsuarioid() == null) {
            return false; // solo el personal del CETS captura donaciones
        }
        SolicitudReposicion s = r.getSolicitudReposicionid();
        r.setSolicitudReposicionid(s == null || s.getId() == null ? null : solicitudF.find(s.getId()));
        r.setProximaFechaElegible(calcularProximaFecha(r.getFechaDonacion()));
        registroF.create(r);
        return true;
    }

    // HU-5.3 Fecha de la donación + periodo de exclusión
    public Date calcularProximaFecha(Date fechaDonacion) {
        LocalDate proxima = aLocalDate(fechaDonacion).plusWeeks(SEMANAS_EXCLUSION);
        return Date.from(proxima.atStartOfDay(ZONA).toInstant());
    }

    // HU-5.4 Buscar donador por nombre, correo o id
    public List<Usuario> buscarDonador(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return List.of();
        }
        return usuarioF.buscarDonadores(criterio);
    }

    public Usuario obtenerDonador(Integer id) {
        return id == null ? null : usuarioF.find(id);
    }

    // HU-5.4 Historial de donaciones en el CETS
    public List<RegistroDonacion> obtenerHistorial(Usuario donador) {
        return registroF.consultarDonaciones(donador);
    }

    // HU-5.4 Próxima fecha de elegibilidad (null si nunca ha donado)
    public Date obtenerElegibilidad(Usuario donador) {
        RegistroDonacion ultima = registroF.consultarUltimaDonacion(donador);
        if (ultima == null) {
            return null;
        }
        return ultima.getProximaFechaElegible() != null
                ? ultima.getProximaFechaElegible()
                : calcularProximaFecha(ultima.getFechaDonacion());
    }

    // HU-5.4 Elegible si no tiene exclusión permanente y ya pasó su próxima fecha
    public boolean esElegible(Usuario donador) {
        if (donador.getExclusionPermanente()) {
            return false;
        }
        Date proxima = obtenerElegibilidad(donador);
        return proxima == null || !aLocalDate(proxima).isAfter(hoy());
    }

    // HU-7.3 Historial compartido por instituciones externas
    public List<HistorialExterno> obtenerHistorialExterno(Usuario donador) {
        return historialExternoF.consultarHistorialExterno(donador);
    }

    // HU-5.1 Solicitudes activas para vincular la donación
    public List<SolicitudReposicion> listarSolicitudesActivas() {
        return solicitudF.listarActivas();
    }

    private LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    private LocalDate aLocalDate(Date fecha) {
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZONA).toLocalDate();
    }
}
