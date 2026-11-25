/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ln;

import acceso_datos.CriterioExclusionFacade;
import acceso_datos.CuestionarioRespuestaFacade;
import acceso_datos.SolicitudReposicionFacade;
import acceso_datos.UsuarioFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import modelo.CriterioExclusion;
import modelo.CuestionarioRespuesta;
import modelo.RespuestaCriterio;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * ControlCuestionario: HU-4.1 (responder cuestionario), HU-4.2 (resultado) y HU-4.3 (catálogo).
 *
 * @author Alex Torres
 */
@Stateless
@LocalBean
public class LnCuestionario {

    @EJB
    private CriterioExclusionFacade criterioF;

    @EJB
    private CuestionarioRespuestaFacade cuestionarioF;

    @EJB
    private UsuarioFacade usuarioF;

    @EJB
    private SolicitudReposicionFacade solicitudF;

    // Valores del CHECK de la BD (CuestionarioRespuesta y CriterioExclusion): D3 y D11 de TASKS.md
    public static final String APTO = "Apto";
    public static final String NO_APTO = "NoApto";
    private static final String SI = "Si";
    private static final String NO = "No";
    private static final String TEMPORAL = "Temporal";
    private static final String PERMANENTE = "Permanente";
    private static final List<String> TIPOS_RESPUESTA = List.of("SiNo", "SeleccionSimple");

    // HU-4.1 Criterios que entran al cuestionario: solo los activos
    public List<CriterioExclusion> obtenerCriterios() {
        return criterioF.buscarActivos();
    }

    // HU-4.1 c trae usuarioid (sesión), solicitud (opcional) y una respuesta "Si"/"No" por criterio activo.
    // Evalúa (HU-4.2) antes de guardar (resultado NOT NULL); regresa el id, o 0 si no es válido (nada se guarda).
    public int registrarRespuestas(CuestionarioRespuesta c) {
        boolean traeUsuario = c.getUsuarioid() != null && c.getUsuarioid().getId() != null;
        boolean traeRespuestas = c.getRespuestaCriterioList() != null && !c.getRespuestaCriterioList().isEmpty();
        if (!traeUsuario || !traeRespuestas) {
            return 0;
        }
        Usuario usuario = usuarioF.find(c.getUsuarioid().getId());
        if (usuario == null) {
            return 0;
        }
        SolicitudReposicion solicitud = null;
        if (c.getSolicitudReposicionid() != null && c.getSolicitudReposicionid().getId() != null) {
            solicitud = solicitudF.find(c.getSolicitudReposicionid().getId());
            if (solicitud == null || !"Activa".equals(solicitud.getEstatus())) {
                return 0;
            }
        }
        List<RespuestaCriterio> respuestas = armarRespuestas(c.getRespuestaCriterioList());
        if (respuestas == null) {
            return 0;
        }

        c.setId(null);
        c.setUsuarioid(usuario);
        c.setSolicitudReposicionid(solicitud);
        c.setFechaRespuesta(new Date());
        for (RespuestaCriterio r : respuestas) {
            r.setCuestionarioRespuestaid(c);
        }
        c.setRespuestaCriterioList(respuestas);
        c.setResultado(evaluarRespuestas(respuestas));
        c.setTiempoEsperaDias(NO_APTO.equals(c.getResultado()) ? calcularTiempoEspera(respuestas) : null);
        int id = cuestionarioF.crearYObtenerId(c);
        // Referencias inversas (#35)
        agregar(usuario.getCuestionarioRespuestaList(), c);
        if (solicitud != null) {
            agregar(solicitud.getCuestionarioRespuestaList(), c);
        }
        return id;
    }

    // Agrega al lado inverso de una relación bidireccional (null-safe)
    private <T> void agregar(List<T> lista, T elemento) {
        if (lista != null) {
            lista.add(elemento);
        }
    }

    // HU-4.2 Resultado ya evaluado. c.id y c.usuarioid obligatorios; debe ser del usuario (nadie lee
    // resultados ajenos). null si no existe.
    public CuestionarioRespuesta obtenerResultado(CuestionarioRespuesta c) {
        boolean traeUsuario = c != null && c.getUsuarioid() != null && c.getUsuarioid().getId() != null;
        if (!traeUsuario || c.getId() == null) {
            return null;
        }
        CuestionarioRespuesta encontrado = cuestionarioF.find(c.getId());
        boolean ajeno = encontrado != null
                && !encontrado.getUsuarioid().getId().equals(c.getUsuarioid().getId());
        return ajeno ? null : encontrado;
    }

    // HU-4.3 Catálogo completo del CETS, incluidos los inactivos (no está en el diagrama)
    public List<CriterioExclusion> obtenerCatalogo() {
        List<CriterioExclusion> catalogo = new ArrayList<>(criterioF.findAll());
        catalogo.sort((a, b) -> a.getId().compareTo(b.getId()));
        return catalogo;
    }

    // HU-4.3 Agrega un criterio (c trae criterio, tipoRespuesta, tipoExclusion, duracionDias y activo);
    // false si no es válido (ver validarCriterio).
    public boolean registrarCriterio(CriterioExclusion c) {
        if (!validarCriterio(c)) {
            return false;
        }
        c.setId(null);
        c.setCriterio(c.getCriterio().trim());
        criterioF.create(c);
        return true;
    }

    // HU-4.3 Edita un criterio (también activa o desactiva); c.id lleno. No se borra: en uso se desactiva.
    public boolean modificarCriterio(CriterioExclusion c) {
        if (c.getId() == null || !validarCriterio(c)) {
            return false;
        }
        CriterioExclusion existente = criterioF.find(c.getId());
        if (existente == null) {
            return false;
        }
        existente.setCriterio(c.getCriterio().trim());
        existente.setTipoRespuesta(c.getTipoRespuesta());
        existente.setTipoExclusion(c.getTipoExclusion());
        existente.setDuracionDias(c.getDuracionDias());
        existente.setActivo(c.getActivo());
        criterioF.edit(existente);
        return true;
    }

    // HU-4.3 validarCriterio (el diagrama lo deja en el Control): texto no vacío, tipoRespuesta SiNo o
    // SeleccionSimple, Temporal ⇒ duracionDias > 0, Permanente ⇒ sin duración (si viene, se descarta).
    private boolean validarCriterio(CriterioExclusion c) {
        if (c == null || c.getCriterio() == null || c.getCriterio().isBlank()) {
            return false;
        }
        if (c.getTipoRespuesta() == null || !TIPOS_RESPUESTA.contains(c.getTipoRespuesta())) {
            return false;
        }
        if (TEMPORAL.equals(c.getTipoExclusion())) {
            return c.getDuracionDias() != null && c.getDuracionDias() > 0;
        }
        if (PERMANENTE.equals(c.getTipoExclusion())) {
            c.setDuracionDias(null);
            return true;
        }
        return false;
    }

    // HU-4.1 Liga cada respuesta a su criterio: exactamente una por criterio activo, con "Si" o "No".
    // La pregunta se copia del catálogo (no se confía en la vista) y se calcula excluye; null si no cuadra.
    private List<RespuestaCriterio> armarRespuestas(List<RespuestaCriterio> recibidas) {
        List<CriterioExclusion> activos = criterioF.buscarActivos();
        Set<Integer> faltantes = new HashSet<>();
        for (CriterioExclusion activo : activos) {
            faltantes.add(activo.getId());
        }
        if (recibidas.size() != activos.size()) {
            return null;
        }
        List<RespuestaCriterio> armadas = new ArrayList<>();
        for (RespuestaCriterio recibida : recibidas) {
            boolean traeCriterio = recibida.getCriterioExclusionid() != null
                    && recibida.getCriterioExclusionid().getId() != null;
            boolean respuestaValida = SI.equals(recibida.getRespuesta()) || NO.equals(recibida.getRespuesta());
            if (!traeCriterio || !respuestaValida || !faltantes.remove(recibida.getCriterioExclusionid().getId())) {
                return null;
            }
            CriterioExclusion criterio = criterioF.find(recibida.getCriterioExclusionid().getId());
            RespuestaCriterio r = new RespuestaCriterio();
            r.setCriterioExclusionid(criterio);
            r.setPreguntaTexto(criterio.getCriterio());
            r.setRespuesta(recibida.getRespuesta());
            r.setExcluye(evaluarCriterio(criterio, recibida.getRespuesta()));
            armadas.add(r);
        }
        return armadas;
    }

    // HU-4.2 CriterioExclusion.evaluarCriterio: el criterio excluye si el donador contestó "Si"
    private boolean evaluarCriterio(CriterioExclusion criterio, String respuesta) {
        return SI.equals(respuesta);
    }

    // HU-4.2 evaluarRespuestas: "NoApto" si alguna respuesta excluye; si no, "Apto"
    private String evaluarRespuestas(List<RespuestaCriterio> respuestas) {
        for (RespuestaCriterio r : respuestas) {
            if (r.getExcluye()) {
                return NO_APTO;
            }
        }
        return APTO;
    }

    // HU-4.2 CuestionarioRespuesta.calcularTiempoEspera: mayor duracionDias de los criterios Temporales que
    // excluyen; null si excluye uno Permanente (sin fecha de regreso, Inconsistencias 10).
    private Integer calcularTiempoEspera(List<RespuestaCriterio> respuestas) {
        Integer espera = 0;
        for (RespuestaCriterio r : respuestas) {
            if (!r.getExcluye()) {
                continue;
            }
            CriterioExclusion criterio = r.getCriterioExclusionid();
            if (PERMANENTE.equals(criterio.getTipoExclusion())) {
                return null;
            }
            if (criterio.getDuracionDias() != null && criterio.getDuracionDias() > espera) {
                espera = criterio.getDuracionDias();
            }
        }
        return espera;
    }
}
