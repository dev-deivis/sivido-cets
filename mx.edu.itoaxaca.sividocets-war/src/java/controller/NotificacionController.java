package controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import ln.LnCuentas;
import ln.LnNotificacion;
import modelo.Interes;
import modelo.Notificacion;
import modelo.PerfilClinico;
import modelo.SolicitudReposicion;

/**
 * Controller de las vistas notificaciones/notificaciones (HU-3.1) y notificaciones/confirmarInteres (HU-3.3).
 * Usa LnNotificacion (ControlNotificacion del diagrama). Nunca expone latitud/longitud (HU-3.2).
 *
 * confirmarInteres.xhtml llena la entidad completa:
 *   #{notificacionController.interes.solicitudReposicion.id}   (f:viewParam "solicitud")
 *
 * @author Alex Torres
 */
@Named
@ViewScoped
public class NotificacionController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnNotificacion lnNotificacion;

    @EJB
    private LnCuentas lnCuentas;

    @Inject
    private SesionController sesion;

    // HU-3.1: avisos del donador en sesión
    private List<Notificacion> notificaciones = new ArrayList<>();

    // Perfil clínico del donador (grupo, Rh y radio). Las coordenadas no se muestran en ninguna vista.
    private PerfilClinico perfil;
    private int radio;

    // HU-3.3: entidad que llena la vista confirmarInteres. El usuario lo pone el controller desde la sesión.
    private Interes interes;
    private SolicitudReposicion solicitud;
    private Notificacion aviso;
    private boolean compatible;
    private String motivoRechazo;   // null = puede confirmar; si no, por qué no (lo decide LnNotificacion)

    // Campo propio de la vista (no existe en la entidad): casilla de aceptación de la vista confirmarInteres
    private boolean aceptaContacto;

    public NotificacionController() {   // sin @PostConstruct: jakarta.annotation no está en el classpath del war
        interes = new Interes();
        interes.setSolicitudReposicion(new SolicitudReposicion());
    }

    // HU-3.1 Se ejecuta al abrir notificaciones (f:viewAction)
    public void cargarNotificaciones() throws IOException {
        if (!sesionValida()) {
            return;
        }
        notificaciones = lnNotificacion.listarMisNotificaciones(sesion.getUsuario());
        cargarPerfil();
    }

    // HU-3.3 Se ejecuta al abrir confirmarInteres (f:viewAction); interes.solicitudReposicion.id viene del f:viewParam
    public void cargarSolicitud() throws IOException {
        if (!sesionValida()) {
            return;
        }
        solicitud = lnNotificacion.obtenerSolicitud(interes.getSolicitudReposicion());
        if (solicitud == null) {
            return;
        }
        cargarPerfil();
        compatible = lnCuentas.receptoresCompatibles(perfil)
                .contains(solicitud.getTipoSanguineo() + solicitud.getFactorRh());
        Notificacion n = new Notificacion();
        n.setUsuarioid(sesion.getUsuario());
        n.setSolicitudReposicionid(solicitud);
        aviso = lnNotificacion.obtenerNotificacion(n);
        motivoRechazo = lnNotificacion.motivoRechazoInteres(sesion.getUsuario(), solicitud);
    }

    // HU-3.3 Confirmar interés: solo si el donador aceptó compartir su contacto y es elegible (lo decide el Ln)
    public String confirmarInteres() throws IOException {
        if (!sesionValida()) {
            return null;
        }
        if (!aceptaContacto) {
            error("Marca la casilla de aceptación para confirmar tu interés");
            return null;
        }
        interes.setUsuario(sesion.getUsuario());
        SolicitudReposicion registrada = lnNotificacion.registrarInteres(interes);
        if (registrada == null) {
            String motivo = lnNotificacion.motivoRechazoInteres(sesion.getUsuario(), interes.getSolicitudReposicion());
            error("No se pudo registrar tu interés. " + (motivo == null ? "" : motivo));
            return null;
        }
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        info("Tu interés en la solicitud " + registrada.getFolioCETS() + " quedó registrado");
        return "/vista/notificaciones/notificaciones?faces-redirect=true";
    }

    private void cargarPerfil() {
        perfil = lnCuentas.obtenerPerfilClinico(sesion.getUsuario());
        radio = lnCuentas.obtenerRadio(sesion.getUsuario());
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

    // ¿El donador ya tiene grupo, Rh y ubicación? (sin ubicación no se le notifica, D1)
    public boolean isPerfilCompleto() {
        return perfil != null && perfil.getUsuarioid() != null;
    }

    public boolean isConUbicacion() {
        return perfil != null && perfil.getLatitud() != null && perfil.getLongitud() != null;
    }

    public List<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public PerfilClinico getPerfil() {
        return perfil;
    }

    public int getRadio() {
        return radio;
    }

    public Interes getInteres() {
        return interes;
    }

    public void setInteres(Interes interes) {
        this.interes = interes;
    }

    public SolicitudReposicion getSolicitud() {
        return solicitud;
    }

    public Notificacion getAviso() {
        return aviso;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public boolean isPuedeConfirmar() {
        return solicitud != null && motivoRechazo == null;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public boolean isAceptaContacto() {
        return aceptaContacto;
    }

    public void setAceptaContacto(boolean aceptaContacto) {
        this.aceptaContacto = aceptaContacto;
    }
}
