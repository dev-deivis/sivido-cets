/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ln;

import acceso_datos.InteresFacade;
import acceso_datos.PersonalCETSFacade;
import acceso_datos.SolicitudReposicionFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.util.List;
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
    private LnCuentas lnCuentas;

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

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
