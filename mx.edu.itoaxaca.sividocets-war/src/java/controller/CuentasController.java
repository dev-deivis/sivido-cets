package controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import ln.LnCuentas;
import modelo.Usuario;

/**
 * Controller del paquete cuentasUsuario (ControlCuentas → LnCuentas).
 * La vista llena directamente la entidad: #{cuentasController.usuario.campo}.
 *
 * @author eldeivis
 */
@Named
@RequestScoped
public class CuentasController {

    @EJB
    private LnCuentas lnCuentas;

    // Entidad que llena la vista (usuario.passwordHash llega en texto plano)
    private Usuario usuario = new Usuario();

    // Campos propios de la vista: no son atributos de Usuario
    private String confirmacion;
    private boolean aceptaAviso;

    // HU-1.1 Alta inicial
    public String registrarUsuario() {
        if (!usuario.getPasswordHash().equals(confirmacion)) {
            error("Las contraseñas no coinciden");
            return null;
        }
        if (!aceptaAviso) {
            error("Debes aceptar el aviso de privacidad");
            return null;
        }
        boolean valido = lnCuentas.registrarUsuario(usuario);
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }

    public boolean isAceptaAviso() {
        return aceptaAviso;
    }

    public void setAceptaAviso(boolean aceptaAviso) {
        this.aceptaAviso = aceptaAviso;
    }
}
