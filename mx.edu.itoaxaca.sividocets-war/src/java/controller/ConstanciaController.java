package controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import ln.LnDonacion;
import modelo.RegistroDonacion;

/**
 * Controller de historial/constancia: comprobante imprimible de una donación del usuario en sesión.
 * La vista llena registro.id (f:viewParam "id"). Solo consulta, vía LnDonacion.
 */
@Named
@RequestScoped
public class ConstanciaController {

    @EJB
    private LnDonacion lnDonacion;

    @Inject
    private SesionController sesion;

    private RegistroDonacion registro = new RegistroDonacion();
    private RegistroDonacion donacion;

    // f:viewAction de la vista; verifica la sesión aquí porque corre antes que el preRenderView
    // de la plantilla (igual que HistorialController).
    public void cargar() throws IOException {
        sesion.verificar();
        if (FacesContext.getCurrentInstance().getResponseComplete()) {
            return;
        }
        donacion = lnDonacion.obtenerConstancia(registro, sesion.getUsuario());
        if (donacion == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Constancia no disponible", null));
        }
    }

    public RegistroDonacion getRegistro() { return registro; }
    public void setRegistro(RegistroDonacion registro) { this.registro = registro; }
    public RegistroDonacion getDonacion() { return donacion; }
}
