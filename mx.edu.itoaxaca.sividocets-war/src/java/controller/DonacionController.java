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
import java.util.List;
import ln.LnDonacion;
import ln.LnPanelCETS;
import modelo.HistorialExterno;
import modelo.PersonalCETS;
import modelo.RegistroDonacion;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Controller del paquete donaciones (ControlDonacion → LnDonacion).
 * Vistas: cets/capturarDonacion (HU-5.1) y cets/donadores (HU-5.4, HU-7.3).
 * La vista llena las entidades: #{donacionController.registro.campo} y #{donacionController.criterio.nombreCompleto}.
 *
 * @author Galilea
 */
@Named
@ViewScoped
public class DonacionController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnDonacion lnDonacion;

    @EJB
    private LnPanelCETS lnPanelCETS;          // HU-5.1: combo de solicitudes activas ([DAVID] HU-6.2)

    @Inject
    private SesionController sesion;

    // HU-5.1 Entidad que llena capturarDonacion (?donador= llena registro.usuarioid.id)
    private RegistroDonacion registro;

    // HU-5.4 Criterio de búsqueda (nombreCompleto = texto) y donador seleccionado (?id= llena donador.id)
    private Usuario criterio;
    private Usuario donador;

    private List<Usuario> resultados;
    private List<HistorialExterno> historialExterno;
    private List<SolicitudReposicion> solicitudesActivas;

    public DonacionController() {
        registro = new RegistroDonacion();
        registro.setUsuarioid(new Usuario());
        registro.setSolicitudReposicionid(new SolicitudReposicion());
        registro.setFechaDonacion(new Date());
        criterio = new Usuario();
        donador = new Usuario();
    }

    // HU-5.4 (1 buscar → 1.1 buscarDonador)
    public void buscarDonador() throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        resultados = lnDonacion.buscarDonador(criterio);
        if (resultados.isEmpty()) {
            aviso("No se encontraron donadores con ese nombre o correo.");
        }
    }

    // HU-5.4 (2.1 mostrar) + HU-7.3 (1.1 obtenerHistorialExterno). f:viewAction de donadores?id=
    public void seleccionarDonador() throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        donador = lnDonacion.obtenerDonador(donador);
        if (donador == null) {
            donador = new Usuario();
            return;
        }
        historialExterno = lnDonacion.obtenerHistorialExterno(donador);
        // TODO HU-5.4 (3 y 4): historial y elegibilidad con los métodos de [Diego]
        // (lnDonacion.obtenerHistorial / obtenerElegibilidad / verificarExclusion) cuando los suba.
    }

    // HU-5.1 f:viewAction de capturarDonacion?donador=: muestra el nombre del donador elegido
    public void cargarDonadorCaptura() throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        if (registro.getUsuarioid().getId() != null) {
            donador = lnDonacion.obtenerDonador(registro.getUsuarioid());
            if (donador == null) {
                donador = new Usuario();
                registro.getUsuarioid().setId(null);
            }
        }
    }

    // HU-5.1 Elegir al donador desde los resultados de la búsqueda en capturarDonacion
    public void elegirDonador(Usuario u) {
        registro.getUsuarioid().setId(u.getId());
        donador = u;
        resultados = null;
    }

    // HU-5.1 (1.1) registrarDonacion. Navegación: capturarDonacion → donadores (con la ficha del donador)
    public String registrarDonacion() throws IOException {
        if (!sesionCETSValida()) {
            return null;
        }
        Integer donadorId = registro.getUsuarioid().getId();
        if (donadorId == null) {
            error("Selecciona al donador.");
            return null;
        }
        PersonalCETS personal = sesion.getUsuario().getPersonalCETS();
        registro.setPersonalCETSUsuarioid(personal != null ? personal : new PersonalCETS(sesion.getUsuario().getId()));
        boolean valido = lnDonacion.registrarDonacion(registro);
        if (!valido) {
            error("No se pudo registrar: revisa el donador y que la fecha no sea futura.");
            return null;
        }
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Donación registrada", null));
        return "/vista/cets/donadores?faces-redirect=true&id=" + donadorId;
    }

    public List<SolicitudReposicion> getSolicitudesActivas() {
        if (solicitudesActivas == null) {
            solicitudesActivas = lnPanelCETS.listarSolicitudesActivas();
        }
        return solicitudesActivas;
    }

    public Date getHoy() {
        return new Date();
    }

    // HU-1.4 Contacto enmascarado en el panel
    public String enmascararCorreo(String correo) {
        if (correo == null || !correo.contains("@")) {
            return "";
        }
        return correo.charAt(0) + "•••" + correo.substring(correo.indexOf('@'));
    }

    public String enmascararTelefono(String tel) {
        if (tel == null || tel.length() < 4) {
            return "•••";
        }
        return "••• ••• ••" + tel.substring(tel.length() - 2);
    }

    // Los f:viewAction corren antes que el preRenderView de plantillaInstitucional: se verifica la sesión CETS aquí
    // para no consultar la BD sin ella (verificarCETS redirige al login institucional), igual que PanelCetsController.
    private boolean sesionCETSValida() throws IOException {
        sesion.verificarCETS();
        return !FacesContext.getCurrentInstance().getResponseComplete();
    }

    private void error(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }

    private void aviso(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, texto, null));
    }

    public RegistroDonacion getRegistro() {
        return registro;
    }

    public void setRegistro(RegistroDonacion registro) {
        this.registro = registro;
    }

    public Usuario getCriterio() {
        return criterio;
    }

    public void setCriterio(Usuario criterio) {
        this.criterio = criterio;
    }

    public Usuario getDonador() {
        return donador;
    }

    public void setDonador(Usuario donador) {
        this.donador = donador;
    }

    public List<Usuario> getResultados() {
        return resultados;
    }

    public List<HistorialExterno> getHistorialExterno() {
        return historialExterno;
    }
}
