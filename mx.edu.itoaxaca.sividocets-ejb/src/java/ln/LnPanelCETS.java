/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ln;

import acceso_datos.HistorialEstatusSolicitudFacade;
import acceso_datos.InteresFacade;
import acceso_datos.PersonalCETSFacade;
import acceso_datos.SolicitudReposicionFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.util.Date;
import java.util.List;
import modelo.HistorialEstatusSolicitud;
import modelo.PersonalCETS;
import modelo.SolicitudReposicion;


/**
 *
 * @author eldeivis
 */
@Stateless
@LocalBean
public class LnPanelCETS {

    @EJB
    private PersonalCETSFacade personalF;

    @EJB
    private SolicitudReposicionFacade solicitudF;

    @EJB
    private InteresFacade interesF;

    @EJB
    private HistorialEstatusSolicitudFacade historialEstatusF;

    @EJB
    private LnCuentas lnCuentas;

    // HU-6.4: estatus que el personal del CETS puede asignar. Fuente: diagrama de clases, nota del atributo
    // SolicitudReposicion.estatus: "Activa al crearse; el personal del CETS la pasa a Atendida, Cerrada o Cancelada".
    private static final List<String> ESTATUS_ASIGNABLES = List.of("Atendida", "Cerrada", "Cancelada");

    // HU-6.1 Iniciar sesión como personal institucional.
    // p trae correoInstitucional y usuario.passwordHash (texto plano) desde la vista.
    // Regresa el PersonalCETS autenticado o null.
    public PersonalCETS iniciarSesionInstitucional(PersonalCETS p) {
        PersonalCETS personal = personalF.buscarPorCorreoInstitucional(p.getCorreoInstitucional());
        boolean autenticado = autenticarse(personal, p.getUsuario().getPasswordHash());
        return autenticado ? personal : null;
    }

    private boolean autenticarse(PersonalCETS personal, String password) {
        if (personal == null || !personal.getActivo()) {
            return false;
        }
        return lnCuentas.autenticarse(personal.getUsuario(), password);
    }

    // HU-6.2 Solicitudes activas. Un SolicitudReposicion nuevo ya trae estatus "Activa"
    // (constructor de la entidad), así que sirve como filtro por defecto.
    public List<SolicitudReposicion> listarSolicitudesActivas() {
        return filtrarSolicitudes(new SolicitudReposicion());
    }

    // HU-6.2 Filtrar. filtro trae estatus, tipoSanguineo y factorRh desde la vista;
    // vacío o null significa "todos".
    public List<SolicitudReposicion> filtrarSolicitudes(SolicitudReposicion filtro) {
        SolicitudReposicion f = new SolicitudReposicion();
        f.setEstatus(vacioANull(filtro.getEstatus()));
        f.setTipoSanguineo(vacioANull(filtro.getTipoSanguineo()));
        f.setFactorRh(vacioANull(filtro.getFactorRh()));
        return solicitudF.buscarFiltradas(f);
    }

    // HU-6.3 Detalle. s trae el id (de f:viewParam); regresa la solicitud completa o null.
    public SolicitudReposicion obtenerDetalle(SolicitudReposicion s) {
        if (s.getId() == null) {
            return null;
        }
        return solicitudF.find(s.getId());
    }

    // HU-6.3 Cantidad de donadores que confirmaron interés (solo el número, nunca su ubicación).
    public int obtenerCantidadInteresados(SolicitudReposicion s) {
        if (s.getId() == null) {
            return 0;
        }
        return interesF.contarPorSolicitud(s);
    }

    // HU-6.4 Actualizar estatus. s trae el id, el nuevo estatus y el personal CETS en sesión
    // (s.personalCETSUsuarioid, lo pone el controller). Regresa false si no se puede cambiar.
    // La solicitud y su bitácora se guardan en la misma transacción.
    public boolean actualizarEstatus(SolicitudReposicion s) {
        if (s.getId() == null || s.getPersonalCETSUsuarioid() == null
                || s.getPersonalCETSUsuarioid().getUsuarioid() == null) {
            return false;
        }
        SolicitudReposicion solicitud = solicitudF.find(s.getId());
        PersonalCETS personal = personalF.find(s.getPersonalCETSUsuarioid().getUsuarioid());
        if (solicitud == null || personal == null || !personal.getActivo()) {
            return false;
        }
        boolean valido = validarEstatus(solicitud.getEstatus(), s.getEstatus());
        if (!valido) {
            return false;
        }

        cambiarEstatus(solicitud, personal, s.getEstatus());
        return true;
    }

    // Cambio de estatus + renglón de bitácora, en la transacción de quien lo llame. Lo usan actualizarEstatus
    // (HU-6.4) y LnDonacion.registrarDonacion (#12: la donación ligada pasa la solicitud a Atendida).
    // solicitud y personal ya son las entidades administradas; no valida (eso lo hace quien llama).
    public void cambiarEstatus(SolicitudReposicion solicitud, PersonalCETS personal, String nuevoEstatus) {
        HistorialEstatusSolicitud historial = new HistorialEstatusSolicitud();
        historial.setSolicitudReposicionid(solicitud);
        historial.setPersonalCETSUsuarioid(personal);
        historial.setEstatusAnterior(solicitud.getEstatus());
        historial.setEstatusNuevo(nuevoEstatus);
        historial.setFechaCambio(new Date());

        solicitud.setEstatus(nuevoEstatus);
        solicitud.setPersonalCETSUsuarioid(personal);
        solicitudF.edit(solicitud);
        historialEstatusF.create(historial);
        // Referencias inversas (#35): sin esto el caché compartido deja las listas desactualizadas
        if (solicitud.getHistorialEstatusSolicitudList() != null) {
            solicitud.getHistorialEstatusSolicitudList().add(historial);
        }
        if (personal.getHistorialEstatusSolicitudList() != null) {
            personal.getHistorialEstatusSolicitudList().add(historial);
        }
    }

    // Bitácora de la solicitud (HU-6.4), del cambio más reciente al más antiguo.
    public List<HistorialEstatusSolicitud> obtenerHistorialEstatus(SolicitudReposicion s) {
        if (s == null || s.getId() == null) {
            return List.of();
        }
        return historialEstatusF.buscarPorSolicitud(s);
    }

    // El nuevo estatus debe ser uno de los asignables (nunca "Activa": solo es el estado al crearse)
    // y distinto del actual.
    private boolean validarEstatus(String actual, String nuevo) {
        return nuevo != null && ESTATUS_ASIGNABLES.contains(nuevo) && !nuevo.equals(actual);
    }

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
