package ln;

import acceso_datos.HospitalFacade;
import acceso_datos.SolicitudReposicionFacade;
import acceso_datos.UsuarioFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import modelo.Hospital;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Lógica de negocio del paquete solicitudes (ControlSolicitud → LnSolicitud).
 * HU-2.1, HU-2.2, HU-2.3, HU-2.4 y HU-1.4.
 *
 * @author Galilea
 */
@Stateless
@LocalBean
public class LnSolicitud {

    // HU-2.4 Formato del folio CETS (decisión D4). Una sola constante para cambiarlo fácil.
    public static final String FORMATO_FOLIO = "^CETS-OAX-\\d{4}-\\d{4}$";

    // HU-2.2 Ruta pública de la publicación (decisión D5: el QR codifica esta URL con uuidPublico)
    private static final String RUTA_PUBLICACION = "/faces/vista/publico/publicacion.xhtml?uuid=";

    private static final int MAX_DESCRIPCION = 250;
    private static final List<String> TIPOS_SANGUINEOS = List.of("A", "B", "AB", "O");
    private static final List<String> FACTORES_RH = List.of("+", "-");

    @EJB
    private SolicitudReposicionFacade solicitudF;

    @EJB
    private HospitalFacade hospitalF;

    @EJB
    private UsuarioFacade usuarioF;

    // HU-2.4 (1.1) validarFolio: formato válido y sin otra solicitud Activa con el mismo folio
    public boolean validarFolio(SolicitudReposicion s) {
        if (!validarFormatoFolio(s)) {
            return false;
        }
        return !solicitudF.existeFolioActivo(s.getFolioCETS());   // 1.1.2 verificarFolioUnico
    }

    // HU-2.4 (1.1.1) validarFormatoFolio: regla del diagrama en la entidad, implementada en el Ln.
    // Normaliza el folio (sin espacios y en mayúsculas) antes de validarlo.
    public boolean validarFormatoFolio(SolicitudReposicion s) {
        if (s.getFolioCETS() == null) {
            return false;
        }
        s.setFolioCETS(s.getFolioCETS().trim().toUpperCase());
        return s.getFolioCETS().matches(FORMATO_FOLIO);
    }

    // HU-2.1 (2.1) registrarSolicitud. La vista llena folio, tipo, Rh, hospitalid.id y descripción;
    // el controller pone el usuario de la sesión. estatus = "Activa" ya viene del constructor.
    public boolean registrarSolicitud(SolicitudReposicion s) {
        boolean valido = validarFolio(s);                                  // 2.1.1
        if (!valido || !validarTipoSangre(s)) {
            return false;
        }
        // El hospital y el usuario deben existir (un id nulo o inexistente no lanza excepción: regresa false)
        Hospital hospital = s.getHospitalid() == null || s.getHospitalid().getId() == null
                ? null : hospitalF.find(s.getHospitalid().getId());
        Usuario usuario = s.getUsuarioid() == null || s.getUsuarioid().getId() == null
                ? null : usuarioF.find(s.getUsuarioid().getId());
        if (hospital == null || usuario == null) {
            return false;
        }
        s.setDescripcion(sanitizar(s.getDescripcion()));
        s.setHospitalid(hospital);
        s.setUsuarioid(usuario);
        s.setUuidPublico(UUID.randomUUID().toString());                  // 2.1.2.1.1 generarUuidPublico
        s.setFechaHoraRegistro(new Date());
        solicitudF.create(s);                                            // 2.1.3 guardarSolicitud
        // TODO HU-3.1: lnNotificacion.buscarDonadoresCercanos(s) cuando [Alex] lo suba
        return true;
    }

    // HU-2.1 Catálogo para el combo de hospitales (no está en el diagrama)
    public List<Hospital> listarHospitales() {
        return hospitalF.findAll();
    }

    // HU-2.2 (1) generarPublicacion: ruta pública de la solicitud (1.1 obtenerUuidPublico);
    // null si la solicitud no existe.
    // El controller le antepone el servidor y el contexto; el navegador dibuja el QR (D5).
    public String generarPublicacion(SolicitudReposicion s) {
        SolicitudReposicion solicitud = s.getId() == null ? null : solicitudF.find(s.getId());
        return solicitud == null ? null : RUTA_PUBLICACION + solicitud.getUuidPublico();
    }

    // HU-2.2 / HU-1.4 obtenerPublicacion: s.uuidPublico lleno; regresa la solicitud o null.
    // No se oculta nada en la entidad: la vista nunca pinta los datos del familiar (neg de HU-1.4).
    public SolicitudReposicion obtenerPublicacion(SolicitudReposicion s) {
        if (s.getUuidPublico() == null || s.getUuidPublico().isBlank()) {
            return null;
        }
        return solicitudF.buscarPorUuid(s.getUuidPublico().trim());
    }

    // HU-2.3 (2) listarMisSolicitudes
    public List<SolicitudReposicion> listarMisSolicitudes(Usuario u) {
        return solicitudF.buscarPorUsuario(u);
    }

    // HU-2.1 tipoSanguineo ∈ {A, B, AB, O} y factorRh ∈ {+, -} (CHECK de la BD)
    private boolean validarTipoSangre(SolicitudReposicion s) {
        return s.getTipoSanguineo() != null && TIPOS_SANGUINEOS.contains(s.getTipoSanguineo())
                && s.getFactorRh() != null && FACTORES_RH.contains(s.getFactorRh());
    }

    // HU-2.1 Descripción opcional: sin etiquetas HTML ni caracteres de control, máximo 250
    private String sanitizar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.replaceAll("<[^>]*>", "")
                .replaceAll("[\\p{Cntrl}&&[^\\n]]", "")
                .trim();
        if (limpio.length() > MAX_DESCRIPCION) {
            limpio = limpio.substring(0, MAX_DESCRIPCION);
        }
        return limpio.isEmpty() ? null : limpio;
    }
}
