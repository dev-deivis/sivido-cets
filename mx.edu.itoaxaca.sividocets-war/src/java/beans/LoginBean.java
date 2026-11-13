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
import ln.LnCuentas;
import modelo.Usuario;

/**
 *
 * @author eldeivis
 */
@Named
@RequestScoped
public class LoginBean {

    @EJB
    private LnCuentas lnCuentas;

    @Inject
    private SesionBean sesion;

    private String correo;
    private String password;

    // HU-1.2: 1 ingresarCredenciales(correo, password)
    public String ingresarCredenciales() {
        Usuario usuario = lnCuentas.iniciarSesion(correo, password);
        boolean autenticado = (usuario != null);
        if (autenticado) {
            sesion.iniciar(usuario, "usuario");
            return "/vista/notificaciones?faces-redirect=true";
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Correo o contraseña incorrectos", null));
        return null;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String v) {
        correo = v;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String v) {
        password = v;
    }
}
