package controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import ln.LnSolicitud;
import modelo.Hospital;
import modelo.SolicitudReposicion;

/**
 * Controller del paquete solicitudes (ControlSolicitud → LnSolicitud).
 * Vistas: vista/solicitudes/capturarSolicitud (HU-2.1, HU-2.4)
 * y vista/solicitudes/misSolicitudes (HU-2.3).
 */
@Named
@ViewScoped
public class SolicitudController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnSolicitud lnSolicitud;

    @Inject
    private SesionController sesion;

    // Entidad que llena la vista: #{solicitudController.solicitud.campo}
    private SolicitudReposicion solicitud = new SolicitudReposicion();
    private Integer hospitalId;

    // HU-2.4 Resultado de validar el folio: "", "valido", "formato" o "duplicado"
    private String estadoFolio = "";

    private List<Hospital> hospitales;
    private List<SolicitudReposicion> misSolicitudes;
    private String filtroEstatus = "Todas";

    // HU-2.4 Se ejecuta al escribir el folio (ajax)
    public void validarFolio() {
        String folio = solicitud.getFolioCETS();
        if (folio == null || folio.isBlank()) {
            estadoFolio = "";
        } else if (!lnSolicitud.validarFormatoFolio(folio)) {
            estadoFolio = "formato";
        } else if (!lnSolicitud.verificarFolioUnico(folio)) {
            estadoFolio = "duplicado";
        } else {
            estadoFolio = "valido";
        }
    }

    // HU-2.1 El botón Publicar se habilita solo con los campos obligatorios completos
    public boolean isCompleto() {
        return "valido".equals(estadoFolio)
                && noVacio(solicitud.getTipoSanguineo())
                && noVacio(solicitud.getFactorRh())
                && hospitalId != null;
    }

    // HU-2.1 Publicar
    public String publicar() {
        validarFolio();
        if (!isCompleto()) {
            error("Revisa los campos obligatorios y el folio.");
            return null;
        }
        solicitud.setHospitalid(new Hospital(hospitalId));
        solicitud.setUsuarioid(sesion.getUsuario());
        if (!lnSolicitud.registrarSolicitud(solicitud)) {
            validarFolio();
            error("El folio no es válido o ya tiene una solicitud activa.");
            return null;
        }
        // HU-2.2 Al publicar se muestra la publicación con su QR
        return "/vista/publico/publicacion?faces-redirect=true&id=" + solicitud.getUuidPublico();
    }

    // HU-2.3
    public List<SolicitudReposicion> getMisSolicitudes() {
        if (misSolicitudes == null) {
            misSolicitudes = lnSolicitud.listarMisSolicitudes(sesion.getUsuario());
        }
        return misSolicitudes;
    }

    public List<SolicitudReposicion> getMisSolicitudesFiltradas() {
        if ("Todas".equals(filtroEstatus)) {
            return getMisSolicitudes();
        }
        return getMisSolicitudes().stream()
                .filter(s -> filtroEstatus.equals(s.getEstatus()))
                .toList();
    }

    public long contar(String estatus) {
        return getMisSolicitudes().stream().filter(s -> estatus.equals(s.getEstatus())).count();
    }

    public List<Hospital> getHospitales() {
        if (hospitales == null) {
            hospitales = lnSolicitud.listarHospitales();
        }
        return hospitales;
    }

    // Nombre del hospital elegido, para la vista previa
    public String getNombreHospital() {
        if (hospitalId == null) {
            return null;
        }
        return getHospitales().stream().filter(h -> h.getId().equals(hospitalId))
                .map(Hospital::getNombre).findFirst().orElse(null);
    }

    public String getFormatoFolio() {
        return LnSolicitud.FORMATO_FOLIO;
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

    public Integer getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Integer hospitalId) {
        this.hospitalId = hospitalId;
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
}
