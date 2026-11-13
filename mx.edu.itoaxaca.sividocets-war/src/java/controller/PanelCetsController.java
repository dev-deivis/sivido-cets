package controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ln.LnPanelCETS;
import modelo.PersonalCETS;
import modelo.Usuario;

/**
 * Controller del paquete panelCETS (ControlPanelCETS → LnPanelCETS).
 * La vista llena la entidad: #{panelCetsController.personal.correoInstitucional}
 * y #{panelCetsController.personal.usuario.passwordHash}.
 *
 * @author eldeivis
 */
@Named
@RequestScoped
public class PanelCetsController {

    @EJB
    private LnPanelCETS lnPanelCETS;

    @Inject
    private SesionController sesion;

    // Entidad que llena la vista (usuario.passwordHash llega en texto plano)
    private PersonalCETS personal;

    public PanelCetsController() {
        personal = new PersonalCETS();
        personal.setUsuario(new Usuario());
    }

    // HU-6.1 Login institucional
    public String iniciarSesionInstitucional() {
        PersonalCETS autenticado = lnPanelCETS.iniciarSesionInstitucional(personal);
        if (autenticado != null) {
            sesion.iniciar(autenticado.getUsuario(), "CETS");
            return "/vista/solicitudesActivas?faces-redirect=true";
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales institucionales incorrectas", null));
        return null;
    }

    public PersonalCETS getPersonal() {
        return personal;
    }

    public void setPersonal(PersonalCETS personal) {
        this.personal = personal;
    }
}
