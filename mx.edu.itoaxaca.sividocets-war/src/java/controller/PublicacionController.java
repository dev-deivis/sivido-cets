package controller;

import jakarta.ejb.EJB;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Base64;
import ln.LnSolicitud;
import modelo.SolicitudReposicion;

/**
 * Publicación digital de una solicitud con código QR (HU-2.2).
 * Vista pública: vista/publico/publicacion.xhtml?id=UUID
 * Solo expone tipo de sangre, hospital, folio y descripción (sin datos del creador).
 */
@Named
@ViewScoped
public class PublicacionController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnSolicitud lnSolicitud;

    private String id;                       // UUID público (?id=...)
    private SolicitudReposicion solicitud;
    private String qrBase64;

    private boolean cargado;

    // Lee ?id= de la URL la primera vez que la vista pide datos
    private void cargar() {
        if (cargado) {
            return;
        }
        cargado = true;
        id = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");
        solicitud = lnSolicitud.obtenerPublicacion(id);
        if (solicitud != null) {
            qrBase64 = Base64.getEncoder().encodeToString(lnSolicitud.generarQR(getUrl()));
        }
    }

    // URL única y no predecible de la publicación
    public String getUrl() {
        cargar();
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        String puerto = (ec.getRequestServerPort() == 80 || ec.getRequestServerPort() == 443)
                ? "" : ":" + ec.getRequestServerPort();
        return ec.getRequestScheme() + "://" + ec.getRequestServerName() + puerto
                + ec.getRequestContextPath() + "/faces/vista/publico/publicacion.xhtml?id="
                + solicitud.getUuidPublico();
    }

    // Imagen PNG del QR como data URI (para <img> y para descargar)
    public String getQrDataUri() {
        cargar();
        return qrBase64 == null ? "" : "data:image/png;base64," + qrBase64;
    }

    public String getNombreArchivoQr() {
        return "SIVIDO-" + solicitud.getFolioCETS() + ".png";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SolicitudReposicion getSolicitud() {
        cargar();
        return solicitud;
    }
}
