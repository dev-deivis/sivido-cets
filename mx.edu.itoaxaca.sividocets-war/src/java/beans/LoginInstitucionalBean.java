/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beans;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ln.LnPanelCETS;
import modelo.PersonalCETS;

/**
 *
 * @author eldeivis
 */
@Named
@RequestScoped
public class LoginInstitucionalBean {

    @EJB
    private LnPanelCETS lnPanelCETS;

    @Inject
    private SesionBean sesion;

    private String correoInstitucional;
    private String password;

    public String ingresarCredenciales() {
        PersonalCETS personal = lnPanelCETS.iniciarSesionInstitucional(correoInstitucional, password);
        boolean autenticado = (personal != null);
        if (autenticado) {
            sesion.iniciar(personal.getUsuario(), "CETS");
            return "/vista/solicitudesActivas?faces-redirect=true";
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales institucionales incorrectas", null));
        return null;
    }

    public String getCorreoInstitucional() { return correoInstitucional; }
    public void setCorreoInstitucional(String v) { correoInstitucional = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
}
