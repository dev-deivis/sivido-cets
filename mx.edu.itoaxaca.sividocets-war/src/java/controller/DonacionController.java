package controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import ln.LnDonacion;
import modelo.HistorialExterno;
import modelo.PersonalCETS;
import modelo.RegistroDonacion;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Controller del paquete donaciones (ControlDonacion → LnDonacion).
 * Vistas: vista/cets/capturarDonacion (HU-5.1)
 * y vista/cets/donadores (HU-5.4 y HU-7.3).
 */
@Named
@ViewScoped
public class DonacionController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnDonacion lnDonacion;

    @Inject
    private SesionController sesion;

    // Búsqueda de donador (HU-5.1 y HU-5.4)
    private String criterio;
    private List<Usuario> resultados;
    private Integer donadorId;               // también puede llegar como ?donador=id

    // HU-5.1 Captura
    private Date fechaDonacion = new Date();
    private Integer solicitudId;
    private List<SolicitudReposicion> solicitudesActivas;

    // HU-5.4 / HU-7.3 Ficha del donador seleccionado
    private Usuario donador;
    private List<RegistroDonacion> historial;
    private List<HistorialExterno> historialExterno;
    private Date proximaFecha;
    private boolean elegible;

    // Si la URL trae ?donador=id, se precarga ese donador
    private boolean iniciado;

    private void iniciar() {
        if (iniciado) {
            return;
        }
        iniciado = true;
        String param = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("donador");
        if (param != null) {
            try {
                donadorId = Integer.valueOf(param);
                cargarDonador();
            } catch (NumberFormatException e) {
                donadorId = null;
            }
        }
    }

    // ===== Búsqueda =====
    public void buscar() {
        resultados = lnDonacion.buscarDonador(criterio);
        if (resultados.isEmpty()) {
            aviso("No se encontraron donadores con ese nombre, correo o id.");
        }
    }

    // ===== HU-5.4 / HU-7.3 =====
    public void seleccionar(Integer id) {
        donadorId = id;
        cargarDonador();
    }

    public void cargarDonador() {
        donador = lnDonacion.obtenerDonador(donadorId);
        if (donador == null) {
            return;
        }
        historial = lnDonacion.obtenerHistorial(donador);
        historialExterno = lnDonacion.obtenerHistorialExterno(donador);
        proximaFecha = lnDonacion.obtenerElegibilidad(donador);
        elegible = lnDonacion.esElegible(donador);
    }

    // ===== HU-5.1 =====
    public String registrarDonacion() {
        if (donadorId == null) {
            error("Selecciona al donador.");
            return null;
        }
        RegistroDonacion r = new RegistroDonacion();
        r.setUsuarioid(new Usuario(donadorId));
        r.setFechaDonacion(fechaDonacion);
        PersonalCETS personal = new PersonalCETS();
        personal.setUsuarioid(sesion.getUsuario().getId());
        r.setPersonalCETSUsuarioid(personal);
        if (solicitudId != null) {
            r.setSolicitudReposicionid(new SolicitudReposicion(solicitudId));
        }
        if (!lnDonacion.registrarDonacion(r)) {
            error("No se pudo registrar: revisa el donador y que la fecha no sea futura.");
            return null;
        }
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Donación registrada", null));
        return "/vista/cets/donadores?faces-redirect=true&donador=" + donadorId;
    }

    public List<SolicitudReposicion> getSolicitudesActivas() {
        if (solicitudesActivas == null) {
            solicitudesActivas = lnDonacion.listarSolicitudesActivas();
        }
        return solicitudesActivas;
    }

    public Date getHoy() {
        return new Date();
    }

    // HU-1.4 Datos de contacto enmascarados
    public String enmascararCorreo(String correo) {
        if (correo == null || !correo.contains("@")) {
            return "";
        }
        int arroba = correo.indexOf('@');
        return correo.charAt(0) + "•••" + correo.substring(arroba);
    }

    public String enmascararTelefono(String tel) {
        if (tel == null || tel.length() < 4) {
            return "•••";
        }
        return "••• ••• ••" + tel.substring(tel.length() - 2);
    }

    // Grupo sanguíneo del perfil clínico (o null si no lo ha capturado)
    public String grupo(Usuario u) {
        if (u == null || u.getPerfilClinico() == null) {
            return null;
        }
        return u.getPerfilClinico().getTipoSanguineo() + u.getPerfilClinico().getFactorRh();
    }

    public int getSemanasExclusion() {
        return LnDonacion.SEMANAS_EXCLUSION;
    }

    private void error(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }

    private void aviso(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, texto, null));
    }

    // ===== getters / setters =====
    public String getCriterio() { return criterio; }
    public void setCriterio(String criterio) { this.criterio = criterio; }
    public List<Usuario> getResultados() { return resultados; }
    public Integer getDonadorId() { iniciar(); return donadorId; }
    public void setDonadorId(Integer donadorId) { this.donadorId = donadorId; }
    public Date getFechaDonacion() { return fechaDonacion; }
    public void setFechaDonacion(Date fechaDonacion) { this.fechaDonacion = fechaDonacion; }
    public Integer getSolicitudId() { return solicitudId; }
    public void setSolicitudId(Integer solicitudId) { this.solicitudId = solicitudId; }
    public Usuario getDonador() { iniciar(); return donador; }
    public List<RegistroDonacion> getHistorial() { return historial; }
    public List<HistorialExterno> getHistorialExterno() { return historialExterno; }
    public Date getProximaFecha() { return proximaFecha; }
    public boolean isElegible() { return elegible; }
}
