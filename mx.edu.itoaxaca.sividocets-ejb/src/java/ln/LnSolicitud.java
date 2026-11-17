package ln;

import acceso_datos.HospitalFacade;
import acceso_datos.SolicitudReposicionFacade;
import acceso_datos.UsuarioFacade;
import io.nayuki.qrcodegen.QrCode;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import modelo.Hospital;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Lógica de negocio del paquete solicitudes (ControlSolicitud → LnSolicitud).
 * HU-2.1 Capturar solicitud, HU-2.2 Publicación con QR,
 * HU-2.3 Mis solicitudes, HU-2.4 Validación de formato del folio.
 */
@Stateless
@LocalBean
public class LnSolicitud {

    // HU-2.4 Formato del folio CETS: CETS-OAX-AAAA-NNNN (ej. CETS-OAX-2026-0582)
    public static final String FORMATO_FOLIO = "CETS-OAX-AAAA-NNNN";
    private static final Pattern PATRON_FOLIO = Pattern.compile("^CETS-OAX-\\d{4}-\\d{4}$");
    private static final int MAX_DESCRIPCION = 250;
    private static final int TAMANO_QR = 400; // px (mínimo pedido: 300x300)

    @EJB
    private SolicitudReposicionFacade solicitudF;

    @EJB
    private HospitalFacade hospitalF;

    @EJB
    private UsuarioFacade usuarioF;

    // HU-2.4 Valida que el folio cumpla el formato del CETS
    public boolean validarFormatoFolio(String folioCETS) {
        return folioCETS != null && PATRON_FOLIO.matcher(normalizarFolio(folioCETS)).matches();
    }

    // HU-2.1 / HU-2.4 No debe existir otra solicitud ACTIVA con el mismo folio
    public boolean verificarFolioUnico(String folioCETS) {
        return !solicitudF.existeFolioActivo(normalizarFolio(folioCETS));
    }

    // HU-2.4 Formato válido y folio único
    public boolean validarFolio(String folioCETS) {
        return validarFormatoFolio(folioCETS) && verificarFolioUnico(folioCETS);
    }

    // HU-2.1 La vista llena la solicitud (folio, tipo, Rh, hospital, descripción)
    // y el controller asigna el usuario de la sesión. Regresa false si el folio no es válido.
    public boolean registrarSolicitud(SolicitudReposicion s) {
        s.setFolioCETS(normalizarFolio(s.getFolioCETS()));
        if (!validarFolio(s.getFolioCETS())) {
            return false;
        }
        s.setDescripcion(sanitizar(s.getDescripcion()));
        s.setHospitalid(hospitalF.find(s.getHospitalid().getId()));
        s.setUsuarioid(usuarioF.find(s.getUsuarioid().getId())); // creador, inmutable
        s.setEstatus("Activa");
        s.setFechaHoraRegistro(new Date());
        s.setUuidPublico(generarUuidPublico());
        solicitudF.create(s);
        return true;
    }

    // HU-2.3 Mis solicitudes, más recientes primero
    public List<SolicitudReposicion> listarMisSolicitudes(Usuario usuario) {
        return solicitudF.consultarSolicitudes(usuario);
    }

    // HU-2.2 Publicación por su UUID (null si no existe)
    public SolicitudReposicion obtenerPublicacion(String uuidPublico) {
        if (uuidPublico == null || uuidPublico.isBlank()) {
            return null;
        }
        return solicitudF.buscarPorUuid(uuidPublico.trim());
    }

    // HU-2.2 Genera el código QR (PNG) que apunta a la URL de la publicación
    public byte[] generarQR(String url) {
        QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
        int borde = 4;
        int modulos = qr.size + borde * 2;
        int escala = Math.max(1, (int) Math.ceil((double) TAMANO_QR / modulos));
        int lado = modulos * escala;
        BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < lado; y++) {
            for (int x = 0; x < lado; x++) {
                boolean negro = qr.getModule(x / escala - borde, y / escala - borde);
                img.setRGB(x, y, negro ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el código QR", e);
        }
    }

    // HU-2.1 Catálogo de hospitales para el selector
    public List<Hospital> listarHospitales() {
        return hospitalF.listarPorNombre();
    }

    // HU-2.2 URL no predecible
    private String generarUuidPublico() {
        return UUID.randomUUID().toString();
    }

    private String normalizarFolio(String folioCETS) {
        return folioCETS == null ? "" : folioCETS.trim().toUpperCase();
    }

    // HU-2.1 Sanitización de la descripción (sin etiquetas HTML, máx. 250)
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
