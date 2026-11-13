package controller;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import modelo.Usuario;

/**
 * Sesión del usuario autenticado (antes SesionBean).
 * Guarda la entidad Usuario completa + el rol ("usuario" o "CETS").
 *
 * @author eldeivis
 */
@Named
@SessionScoped
public class SesionController implements Serializable {

    private static final long serialVersionUID = 1L;

    private Usuario usuario;
    private String rol;

    public void iniciar(Usuario usuario, String rol) {
        this.usuario = usuario;
        this.rol = rol;
    }

    public String cerrarSesion() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/vista/index?faces-redirect=true";
    }

    public String cerrarSesionCETS() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/vista/loginInstitucional?faces-redirect=true";
    }

    public boolean isAutenticado() {
        return usuario != null;
    }

    // Iniciales para el avatar (ej. "David Almaraz" → "DA")
    public String getIniciales() {
        if (usuario == null || usuario.getNombreCompleto() == null
                || usuario.getNombreCompleto().isBlank()) {
            return "";
        }
        String[] p = usuario.getNombreCompleto().trim().split("\\s+");
        String ini = p[0].substring(0, 1);
        if (p.length > 1) {
            ini += p[1].substring(0, 1);
        }
        return ini.toUpperCase();
    }

    // Vistas de usuario: requiere sesión con rol "usuario"
    public void verificar() throws IOException {
        if (!isAutenticado() || !"usuario".equals(rol)) {
            redirigir("/faces/vista/iniciarSesion.xhtml");
        }
    }

    // Vistas del Panel CETS: requiere sesión con rol "CETS" (HU-6.1)
    public void verificarCETS() throws IOException {
        if (!isAutenticado() || !"CETS".equals(rol)) {
            redirigir("/faces/vista/loginInstitucional.xhtml");
        }
    }

    private void redirigir(String ruta) throws IOException {
        var ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(ec.getRequestContextPath() + ruta);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }
}
