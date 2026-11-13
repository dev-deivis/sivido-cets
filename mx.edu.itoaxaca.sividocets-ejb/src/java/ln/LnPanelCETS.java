/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ln;

import acceso_datos.PersonalCETSFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import modelo.PersonalCETS;


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
}
