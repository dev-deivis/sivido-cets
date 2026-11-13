/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import modelo.Usuario;

/**
 *
 * @author eldeivis
 */
@Named
@SessionScoped
public class SesionBean implements Serializable {

    private Integer usuarioId;
    private String nombre;
    private String rol;
    private boolean autenticado;

    public void iniciar(Usuario u, String rol) {
        this.usuarioId = u.getId();
        this.nombre = u.getNombreCompleto();
        this.rol = rol;
        this.autenticado = true;
    }

    public String cerrarSesion() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/vista/index?faces-redirect=true";
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    // Iniciales para el avatar (ej. "David Almaraz" → "DA")
    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) {
            return "";
        }
        String[] p = nombre.trim().split("\\s+");
        String ini = p[0].substring(0, 1);
        if (p.length > 1) {
            ini += p[1].substring(0, 1);
        }
        return ini.toUpperCase();
    }

    // Vistas de usuario: requiere sesión con rol "usuario"
    public void verificar() throws java.io.IOException {
        if (!autenticado || !"usuario".equals(rol)) {
            redirigir("/faces/vista/iniciarSesion.xhtml");
        }
    }

// Vistas del Panel CETS: requiere sesión con rol "CETS" (HU-6.1)
    public void verificarCETS() throws java.io.IOException {
        if (!autenticado || !"CETS".equals(rol)) {
            redirigir("/faces/vista/loginInstitucional.xhtml");
        }
    }

    public String cerrarSesionCETS() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/vista/loginInstitucional?faces-redirect=true";
    }

    private void redirigir(String ruta) throws java.io.IOException {
        var ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(ec.getRequestContextPath() + ruta);
    }
}
