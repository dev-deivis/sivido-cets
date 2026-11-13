/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ln;

import acceso_datos.PersonalCETSFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
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

    // HU-6.1 Iniciar sesión como personal institucional
    public PersonalCETS iniciarSesionInstitucional(String correoInstitucional, String password) {
        PersonalCETS personal = personalF.buscarPorCorreoInstitucional(correoInstitucional);
        boolean autenticado = autenticarse(personal, password);
        return autenticado ? personal : null;
    }

    private boolean autenticarse(PersonalCETS personal, String password) {
        if (personal == null || !personal.getActivo()) {
            return false;
        }
        return personal.getUsuario().getPasswordHash().equals(hash(password));
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
