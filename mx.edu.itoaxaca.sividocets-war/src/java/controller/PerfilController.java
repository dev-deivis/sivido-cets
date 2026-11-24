package controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import ln.LnCuentas;
import ln.LnDonacion;
import modelo.HistorialClinico;
import modelo.PerfilClinico;
import modelo.Usuario;

/**
 * Controller de la vista cuentas/miPerfil (HU-1.3, HU-3.4 y consentimiento de contacto, HU-1.4).
 * Usa LnCuentas (ControlCuentas del diagrama). Es @ViewScoped y no parte de CuentasController
 * (@RequestScoped) porque el perfil debe conservarse entre los clics de la vista.
 * La vista llena la entidad completa:
 *   #{perfilController.perfil.tipoSanguineo / factorRh / radioNotificacion / latitud / longitud}
 *   #{perfilController.perfil.usuario.consentimientoContacto}
 *
 * @author eldeivis
 */
@Named
@ViewScoped
public class PerfilController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnCuentas lnCuentas;

    @EJB
    private LnDonacion lnDonacion;

    // #13: motivo y fecha de la exclusión permanente del usuario (null si no está excluido)
    private HistorialClinico exclusion;

    @Inject
    private SesionController sesion;

    // Entidad que llena la vista. Si el usuario todavía no tiene perfil, es un PerfilClinico nuevo
    // ligado al Usuario de la sesión (PK compartida: el Ln pone usuarioid al guardarlo).
    private PerfilClinico perfil;

    // Radio que está guardado en la BD (el selector puede mostrar uno todavía sin guardar)
    private int radioGuardado;

    public PerfilController() {
        perfil = new PerfilClinico();
        perfil.setUsuario(new Usuario());
    }

    // Se ejecuta al abrir la vista (f:viewAction). plantillaUsuario también verifica la sesión,
    // pero en preRenderView, que corre después.
    public void cargarPerfil() throws IOException {
        if (!sesionValida()) {
            return;
        }
        recargar();
    }

    // HU-1.3 Guardar perfil clínico (grupo, Rh, ubicación y consentimiento de contacto)
    public void guardarPerfil() throws IOException {
        if (!sesionValida()) {
            return;
        }
        boolean guardado = lnCuentas.guardarPerfilClinico(perfil);
        if (guardado) {
            info("Tu perfil se guardó");
        } else {
            error("No se pudo guardar: elige tu grupo sanguíneo y tu factor Rh, y revisa que la ubicación sea válida");
        }
        recargar();
    }

    // HU-3.4 Guardar radio de notificación
    public void guardarRadio() throws IOException {
        if (!sesionValida()) {
            return;
        }
        boolean sinPerfil = perfil.getUsuarioid() == null;
        int radio = perfil.getRadioNotificacion();
        boolean guardado = lnCuentas.configurarRadio(perfil);
        if (guardado) {
            info("Tu radio de notificación ahora es de " + radio + " km");
        } else if (sinPerfil) {
            error("Primero completa tu perfil clínico");
        } else {
            error("El radio debe ser de 3, 5 o 10 km");
        }
        recargar();
    }

    // Vuelve a leer de la BD, así la vista siempre muestra lo que realmente está guardado
    private void recargar() {
        Usuario usuario = sesion.getUsuario();
        perfil = lnCuentas.obtenerPerfilClinico(usuario);
        radioGuardado = lnCuentas.obtenerRadio(usuario);
        exclusion = lnDonacion.obtenerExclusion(usuario);
        // Otras vistas leen el perfil desde la sesión (p. ej. el radio en notificaciones)
        usuario.setPerfilClinico(perfil.getUsuarioid() == null ? null : perfil);
    }

    // Los f:viewAction corren antes que el preRenderView de plantillaUsuario: se verifica aquí para no
    // consultar la BD si no hay sesión de usuario (verificar redirige a iniciarSesion).
    private boolean sesionValida() throws IOException {
        sesion.verificar();
        return !FacesContext.getCurrentInstance().getResponseComplete();
    }

    private void info(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, texto, null));
    }

    private void error(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }

    // Texto para la vista: "O+, A+, B+ y AB+" (vacío si el perfil no tiene tipo y Rh)
    public String getReceptores() {
        List<String> receptores = lnCuentas.receptoresCompatibles(perfil);
        if (receptores.size() < 2) {
            return String.join("", receptores);
        }
        String ultimo = receptores.get(receptores.size() - 1);
        return String.join(", ", receptores.subList(0, receptores.size() - 1)) + " y " + ultimo;
    }

    public boolean isPerfilCompleto() {
        return perfil.getUsuarioid() != null;
    }

    public boolean isConUbicacion() {
        return perfil.getLatitud() != null && perfil.getLongitud() != null;
    }

    public PerfilClinico getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilClinico perfil) {
        this.perfil = perfil;
    }

    public HistorialClinico getExclusion() {
        return exclusion;
    }

    public int getRadioGuardado() {
        return radioGuardado;
    }
}
