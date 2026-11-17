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
import ln.LnCuestionario;
import ln.LnNotificacion;
import modelo.CriterioExclusion;
import modelo.CuestionarioRespuesta;
import modelo.RespuestaCriterio;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Controller de las vistas notificaciones/cuestionario (HU-4.1), notificaciones/resultado (HU-4.2) y
 * cets/criteriosExclusion (HU-4.3). Usa LnCuestionario (ControlCuestionario del diagrama).
 * La vista llena la entidad completa:
 *   cuestionario:      #{cuestionarioController.cuestionario.solicitudReposicionid.id} (f:viewParam "solicitud")
 *                      y una RespuestaCriterio por criterio en #{cuestionarioController.respuestas}
 *   resultado:         #{cuestionarioController.cuestionario.id} (f:viewParam "id")
 *   criteriosExclusion: #{cuestionarioController.criterio.*}
 *
 * @author Alex Torres
 */
@Named
@ViewScoped
public class CuestionarioController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LnCuestionario lnCuestionario;

    @EJB
    private LnNotificacion lnNotificacion;

    @Inject
    private SesionController sesion;

    // HU-4.1 / HU-4.2
    private CuestionarioRespuesta cuestionario;
    private List<RespuestaCriterio> respuestas = new ArrayList<>();
    private SolicitudReposicion solicitud;
    private boolean encontrado;

    // HU-4.3
    private CriterioExclusion criterio;
    private List<CriterioExclusion> catalogo = new ArrayList<>();

    public CuestionarioController() {   // sin @PostConstruct: jakarta.annotation no está en el classpath del war
        cuestionario = new CuestionarioRespuesta();
        cuestionario.setSolicitudReposicionid(new SolicitudReposicion());
        cuestionario.setUsuarioid(new Usuario());
        criterio = new CriterioExclusion();
        criterio.setTipoExclusion("Temporal");
    }

    // ---------------------------------------------------------------- HU-4.1 Responder cuestionario

    // Se ejecuta al abrir cuestionario (f:viewAction). Arma una respuesta vacía por cada criterio activo.
    public void cargarCuestionario() throws IOException {
        if (!sesionValida()) {
            return;
        }
        solicitud = lnNotificacion.obtenerSolicitud(cuestionario.getSolicitudReposicionid());
        if (respuestas.isEmpty()) {
            for (CriterioExclusion c : lnCuestionario.obtenerCriterios()) {
                RespuestaCriterio r = new RespuestaCriterio();
                r.setCriterioExclusionid(c);
                respuestas.add(r);
            }
        }
    }

    // HU-4.1 Guardar las respuestas y mostrar el resultado
    public String registrarRespuestas() throws IOException {
        if (!sesionValida()) {
            return null;
        }
        cuestionario.setUsuarioid(sesion.getUsuario());
        cuestionario.setRespuestaCriterioList(respuestas);
        int id = lnCuestionario.registrarRespuestas(cuestionario);
        if (id == 0) {
            error("No se pudo guardar el cuestionario: contesta todas las preguntas y verifica que la solicitud siga activa");
            return null;
        }
        return "/vista/notificaciones/resultado?faces-redirect=true&id=" + id;
    }

    // ---------------------------------------------------------------- HU-4.2 Ver resultado

    // Se ejecuta al abrir resultado (f:viewAction); cuestionario.id viene del f:viewParam "id".
    // Solo se muestra el cuestionario del propio donador.
    public void cargarResultado() throws IOException {
        if (!sesionValida()) {
            return;
        }
        cuestionario.setUsuarioid(sesion.getUsuario());
        CuestionarioRespuesta c = lnCuestionario.obtenerResultado(cuestionario);
        encontrado = c != null;
        if (encontrado) {
            cuestionario = c;
        }
    }

    // ---------------------------------------------------------------- HU-4.3 Catálogo de criterios (CETS)

    // Se ejecuta al abrir criteriosExclusion (f:viewAction)
    public void cargarCatalogo() throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        catalogo = lnCuestionario.obtenerCatalogo();
    }

    // Agregar (criterio.id vacío) o guardar la edición (criterio.id lleno)
    public void guardarCriterio() throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        boolean edicion = criterio.getId() != null;
        boolean guardado = edicion ? lnCuestionario.modificarCriterio(criterio)
                : lnCuestionario.registrarCriterio(criterio);
        if (guardado) {
            info(edicion ? "El criterio se actualizó" : "El criterio se agregó al catálogo");
            nuevoCriterio();
        } else {
            error("No se pudo guardar: escribe la pregunta, elige el tipo de respuesta y, si la exclusión es "
                    + "temporal, indica los días de espera (mayor que 0)");
        }
        catalogo = lnCuestionario.obtenerCatalogo();
    }

    // Botón lápiz: carga una copia del criterio en el formulario
    public void editarCriterio(CriterioExclusion c) throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        criterio = copiar(c);
    }

    // Activar o desactivar un criterio (no se borra: un criterio en uso se desactiva)
    public void cambiarActivo(CriterioExclusion c) throws IOException {
        if (!sesionCETSValida()) {
            return;
        }
        CriterioExclusion cambio = copiar(c);
        cambio.setActivo(!c.getActivo());
        if (lnCuestionario.modificarCriterio(cambio)) {
            info(cambio.getActivo() ? "El criterio se activó" : "El criterio se desactivó");
        } else {
            error("No se pudo cambiar el estado del criterio");
        }
        catalogo = lnCuestionario.obtenerCatalogo();
    }

    // Botón Cancelar: limpia el formulario
    public void nuevoCriterio() {
        criterio = new CriterioExclusion();
        criterio.setTipoExclusion("Temporal");
    }

    private CriterioExclusion copiar(CriterioExclusion c) {
        CriterioExclusion copia = new CriterioExclusion();
        copia.setId(c.getId());
        copia.setCriterio(c.getCriterio());
        copia.setTipoRespuesta(c.getTipoRespuesta());
        copia.setTipoExclusion(c.getTipoExclusion());
        copia.setDuracionDias(c.getDuracionDias());
        copia.setActivo(c.getActivo());
        return copia;
    }

    // ---------------------------------------------------------------- Sesión y mensajes

    // Los f:viewAction corren antes que el preRenderView de las plantillas: se verifica aquí para no
    // consultar la BD si no hay sesión (verificar / verificarCETS redirigen al login).
    private boolean sesionValida() throws IOException {
        sesion.verificar();
        return !FacesContext.getCurrentInstance().getResponseComplete();
    }

    private boolean sesionCETSValida() throws IOException {
        sesion.verificarCETS();
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

    // ---------------------------------------------------------------- Apoyo para las vistas

    public boolean isApto() {
        return encontrado && LnCuestionario.APTO.equals(cuestionario.getResultado());
    }

    // true si el resultado es NoApto y ningún criterio excluyente es temporal con fecha de regreso
    public boolean isExclusionPermanente() {
        return encontrado && LnCuestionario.NO_APTO.equals(cuestionario.getResultado())
                && cuestionario.getTiempoEsperaDias() == null;
    }

    public int getTotalCatalogo() {
        return catalogo.size();
    }

    public long getTotalTemporales() {
        return catalogo.stream().filter(c -> "Temporal".equals(c.getTipoExclusion())).count();
    }

    public long getTotalPermanentes() {
        return catalogo.stream().filter(c -> "Permanente".equals(c.getTipoExclusion())).count();
    }

    public long getTotalActivos() {
        return catalogo.stream().filter(CriterioExclusion::getActivo).count();
    }

    public long getTotalInactivos() {
        return catalogo.stream().filter(c -> !c.getActivo()).count();
    }

    public CuestionarioRespuesta getCuestionario() {
        return cuestionario;
    }

    public void setCuestionario(CuestionarioRespuesta cuestionario) {
        this.cuestionario = cuestionario;
    }

    public List<RespuestaCriterio> getRespuestas() {
        return respuestas;
    }

    public SolicitudReposicion getSolicitud() {
        return solicitud;
    }

    public boolean isEncontrado() {
        return encontrado;
    }

    public CriterioExclusion getCriterio() {
        return criterio;
    }

    public void setCriterio(CriterioExclusion criterio) {
        this.criterio = criterio;
    }

    public List<CriterioExclusion> getCatalogo() {
        return catalogo;
    }
}
