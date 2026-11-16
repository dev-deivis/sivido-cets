package controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ln.LnPanelCETS;
import modelo.PersonalCETS;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Controller del paquete panelCETS (ControlPanelCETS → LnPanelCETS).
 * La vista llena la entidad:
 *   login   → #{panelCetsController.personal.correoInstitucional} y .personal.usuario.passwordHash
 *   filtros → #{panelCetsController.filtro.estatus / tipoSanguineo / factorRh}
 *   detalle → #{panelCetsController.solicitud.id} (f:viewParam)
 *
 * @author eldeivis
 */
@Named
@ViewScoped
public class PanelCetsController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnPanelCETS lnPanelCETS;

    @Inject
    private SesionController sesion;

    // HU-6.1: entidad que llena la vista (usuario.passwordHash llega en texto plano)
    private PersonalCETS personal;

    // HU-6.2: filtro (la entidad nueva ya trae estatus "Activa") y resultado
    private SolicitudReposicion filtro;
    private List<SolicitudReposicion> solicitudes;
    // Donadores interesados por solicitud (id de solicitud → cantidad), para la tabla
    private Map<Integer, Integer> interesados = new HashMap<>();

    // HU-6.3: solicitud seleccionada (el id llega por f:viewParam) y su cantidad de interesados
    private SolicitudReposicion solicitud;
    private int cantidadInteresados;

    public PanelCetsController() {
        personal = new PersonalCETS();
        personal.setUsuario(new Usuario());
        filtro = new SolicitudReposicion();
        solicitud = new SolicitudReposicion();
    }

    // HU-6.1 Login institucional
    public String iniciarSesionInstitucional() {
        PersonalCETS autenticado = lnPanelCETS.iniciarSesionInstitucional(personal);
        // Al ser @ViewScoped, no se deja la contraseña en texto plano viva en la vista
        personal.getUsuario().setPasswordHash(null);
        if (autenticado != null) {
            sesion.iniciar(autenticado.getUsuario(), "CETS");
            return "/vista/cets/solicitudesActivas?faces-redirect=true";
        }
        error("Credenciales institucionales incorrectas");
        return null;
    }

    // HU-6.2 Visualizar solicitudes activas (f:viewAction al abrir solicitudesActivas)
    public void listarSolicitudesActivas() throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        cargarSolicitudes(lnPanelCETS.listarSolicitudesActivas());
    }

    // HU-6.2 Filtrar (botón "Refrescar")
    public void filtrarSolicitudes() throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        cargarSolicitudes(lnPanelCETS.filtrarSolicitudes(filtro));
    }

    // HU-6.3 Detalle de la solicitud seleccionada (f:viewAction al abrir detalleSolicitud)
    public void obtenerDetalle() throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        SolicitudReposicion encontrada = lnPanelCETS.obtenerDetalle(solicitud);
        if (encontrada == null) {
            solicitud = new SolicitudReposicion();
            error("La solicitud no existe");
            return;
        }
        solicitud = encontrada;
        cantidadInteresados = lnPanelCETS.obtenerCantidadInteresados(solicitud);
    }

    private void cargarSolicitudes(List<SolicitudReposicion> lista) {
        solicitudes = lista;
        interesados = new HashMap<>();
        for (SolicitudReposicion s : lista) {
            interesados.put(s.getId(), lnPanelCETS.obtenerCantidadInteresados(s));
        }
    }

    // Los f:viewAction corren antes que el preRenderView de la plantilla: se verifica la sesión aquí
    // para no consultar la BD si no es personal CETS (verificarCETS redirige al login institucional).
    private boolean sesionCETSValida() throws IOException {
        sesion.verificarCETS();
        return !FacesContext.getCurrentInstance().getResponseComplete();
    }

    private void error(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }

    // Tarjetas de resumen de solicitudesActivas (se calculan sobre las solicitudes mostradas)
    public int getTotalActivas() {
        int total = 0;
        if (solicitudes != null) {
            for (SolicitudReposicion s : solicitudes) {
                if ("Activa".equals(s.getEstatus())) {
                    total++;
                }
            }
        }
        return total;
    }

    public int getTotalInteresados() {
        int total = 0;
        for (int cantidad : interesados.values()) {
            total += cantidad;
        }
        return total;
    }

    public int getTotalSinInteresados() {
        int total = 0;
        for (int cantidad : interesados.values()) {
            if (cantidad == 0) {
                total++;
            }
        }
        return total;
    }

    public Date getAhora() {
        return new Date();
    }

    public PersonalCETS getPersonal() {
        return personal;
    }

    public void setPersonal(PersonalCETS personal) {
        this.personal = personal;
    }

    public SolicitudReposicion getFiltro() {
        return filtro;
    }

    public void setFiltro(SolicitudReposicion filtro) {
        this.filtro = filtro;
    }

    public List<SolicitudReposicion> getSolicitudes() {
        return solicitudes;
    }

    public Map<Integer, Integer> getInteresados() {
        return interesados;
    }

    public SolicitudReposicion getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(SolicitudReposicion solicitud) {
        this.solicitud = solicitud;
    }

    public int getCantidadInteresados() {
        return cantidadInteresados;
    }
}
