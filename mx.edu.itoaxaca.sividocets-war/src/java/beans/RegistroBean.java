/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beans;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import ln.LnCuentas;

/**
 *
 * @author eldeivis
 */

@Named
@RequestScoped
public class RegistroBean {

    @EJB
    private LnCuentas lnCuentas;

    private String nombreCompleto;
    private String telefono;
    private String correo;
    private String password;
    private String confirmacion;
    private boolean aceptaAviso;

    // HU-1.1: 1 llenarFormulario(...)
    public String llenarFormulario() {
        if (!password.equals(confirmacion)) {
            error("Las contraseñas no coinciden");
            return null;
        }
        if (!aceptaAviso) {
            error("Debes aceptar el aviso de privacidad");
            return null;
        }
        boolean valido = lnCuentas.registrarUsuario(nombreCompleto, telefono, correo, password);
        if (valido) {
            return "/vista/iniciarSesion?faces-redirect=true";
        }
        error("Ese correo ya está registrado");
        return null;
    }

    private void error(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String v) {
        nombreCompleto = v;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String v) {
        telefono = v;
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

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String v) {
        confirmacion = v;
    }

    public boolean isAceptaAviso() {
        return aceptaAviso;
    }

    public void setAceptaAviso(boolean v) {
        aceptaAviso = v;
    }
}
