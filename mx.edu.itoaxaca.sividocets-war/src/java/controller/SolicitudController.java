package controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import ln.LnSolicitud;
import modelo.Hospital;
import modelo.SolicitudReposicion;

/**
 * Controller del paquete solicitudes (ControlSolicitud → LnSolicitud).
 * Vistas: capturarSolicitud (HU-2.1, HU-2.4), misSolicitudes (HU-2.3) y publicacion (HU-2.2, HU-1.4).
 * La vista llena la entidad: #{solicitudController.solicitud.campo}.
 *
 * @author Galilea
 */
@Named
@ViewScoped
public class SolicitudController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnSolicitud lnSolicitud;

    @Inject
    private SesionController sesion;

    // Entidad que llena la vista (y que recibe ?uuid= en la publicación)
    private SolicitudReposicion solicitud;

    // Campos propios de la vista (no existen en la entidad)
    private String estadoFolio = "";            // HU-2.4: "", "valido", "formato" o "duplicado"
    private String filtroEstatus = "Todas";     // HU-2.3: píldoras de estatus
    private String urlPublicacion;              // HU-2.2: URL completa que codifica el QR

    private List<Hospital> hospitales;
    private List<SolicitudReposicion> misSolicitudes;

    public SolicitudController() {
        solicitud = new SolicitudReposicion();
        solicitud.setHospitalid(new Hospital());
    }

    // HU-2.4 (1 ingresarFolio → 1.1 validarFolio), al escribir el folio (ajax)
    public void validarFolio() {
        if (solicitud.getFolioCETS() == null || solicitud.getFolioCETS().isBlank()) {
            estadoFolio = "";
        } else if (lnSolicitud.validarFolio(solicitud)) {
            estadoFolio = "valido";
        } else if (!lnSolicitud.validarFormatoFolio(solicitud)) {
            estadoFolio = "formato";
        } else {
            estadoFolio = "duplicado";
        }
    }

    // HU-2.1 El botón "Publicar" se habilita solo con los obligatorios completos
    public boolean isCompleto() {
        return "valido".equals(estadoFolio)
                && noVacio(solicitud.getTipoSanguineo())
                && noVacio(solicitud.getFactorRh())
                && solicitud.getHospitalid().getId() != null;
    }

    // HU-2.1 (2.1) registrarSolicitud. Navegación D9: capturarSolicitud → misSolicitudes
    public String registrarSolicitud() {
        solicitud.setUsuarioid(sesion.getUsuario());
        boolean valido = lnSolicitud.registrarSolicitud(solicitud);
        if (valido) {
            return "/vista/solicitudes/misSolicitudes?faces-redirect=true";
        }
        validarFolio();
        if ("duplicado".equals(estadoFolio)) {
            error("Ya existe una solicitud activa con ese folio");
        } else if ("valido".equals(estadoFolio)) {
            error("Elige el grupo sanguíneo, el factor Rh y un hospital de la lista");
        } else {
            error("El folio debe tener el formato CETS-OAX-AAAA-NNNN");
        }
        return null;
    }

    // HU-2.3 (2) listarMisSolicitudes (f:viewAction al abrir misSolicitudes)
    public void listarMisSolicitudes() throws IOException {
        // verificar() redirige si no hay sesión o si el rol no es "usuario" (p. ej. personal CETS)
        sesion.verificar();
        if (FacesContext.getCurrentInstance().getResponseComplete()) {
            return;
        }
        misSolicitudes = lnSolicitud.listarMisSolicitudes(sesion.getUsuario());
    }

    // HU-2.2 / HU-1.4 obtenerPublicacion (f:viewAction al abrir publicacion?uuid=)
    public void obtenerPublicacion() {
        SolicitudReposicion encontrada = lnSolicitud.obtenerPublicacion(solicitud);
        if (encontrada == null) {
            solicitud = null;
            return;
        }
        solicitud = encontrada;
        String ruta = lnSolicitud.generarPublicacion(solicitud);                   // HU-2.2 generarPublicacion
        urlPublicacion = ruta == null ? null : servidor() + ruta;
    }

    public List<SolicitudReposicion> getMisSolicitudesFiltradas() {
        if (misSolicitudes == null || "Todas".equals(filtroEstatus)) {
            return misSolicitudes;
        }
        return misSolicitudes.stream().filter(s -> filtroEstatus.equals(s.getEstatus())).toList();
    }

    public long contar(String estatus) {
        return misSolicitudes == null ? 0
                : misSolicitudes.stream().filter(s -> estatus.equals(s.getEstatus())).count();
    }

    public List<Hospital> getHospitales() {
        if (hospitales == null) {
            hospitales = lnSolicitud.listarHospitales();
        }
        return hospitales;
    }

    // Nombre del hospital elegido, para la vista previa
    public String getNombreHospital() {
        Integer id = solicitud.getHospitalid().getId();
        if (id == null) {
            return null;
        }
        return getHospitales().stream().filter(h -> h.getId().equals(id))
                .map(Hospital::getNombre).findFirst().orElse(null);
    }

    // http://servidor:puerto/contexto
    private String servidor() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        int puerto = ec.getRequestServerPort();
        return ec.getRequestScheme() + "://" + ec.getRequestServerName()
                + (puerto == 80 || puerto == 443 ? "" : ":" + puerto) + ec.getRequestContextPath();
    }

    private boolean noVacio(String s) {
        return s != null && !s.isBlank();
    }

    private void error(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }

    public SolicitudReposicion getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(SolicitudReposicion solicitud) {
        this.solicitud = solicitud;
    }

    public String getEstadoFolio() {
        return estadoFolio;
    }

    public String getFiltroEstatus() {
        return filtroEstatus;
    }

    public void setFiltroEstatus(String filtroEstatus) {
        this.filtroEstatus = filtroEstatus;
    }

    public String getUrlPublicacion() {
        return urlPublicacion;
    }

    public List<SolicitudReposicion> getMisSolicitudes() {
        return misSolicitudes;
    }

    public String getFormatoFolio() {
        return "CETS-OAX-AAAA-NNNN";
    }
}
