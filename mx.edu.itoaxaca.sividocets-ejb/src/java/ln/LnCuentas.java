/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ln;

import acceso_datos.UsuarioFacade;
import modelo.Usuario;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;

/**
 *
 * @author eldeivis
 */
@Stateless
@LocalBean
public class LnCuentas {

    @EJB
    private UsuarioFacade usuarioF;

    // HU-1.1 Alta inicial: la vista llena el Usuario completo;
    // passwordHash llega en texto plano y aquí se reemplaza por su hash.
    public boolean registrarUsuario(Usuario u) {
        boolean valido = !usuarioF.existeCorreo(u.getCorreo());
        if (!valido) {
            return false;
        }
        u.setPasswordHash(hash(u.getPasswordHash()));
        u.setFechaRegistro(new Date());
        usuarioF.create(u);
        return true;
    }
    // HU-1.2 Autenticación: u trae correo y passwordHash (texto plano) desde la vista.
    // Regresa el Usuario autenticado o null.
    public Usuario iniciarSesion(Usuario u) {
        Usuario usuario = usuarioF.buscarPorCorreo(u.getCorreo());
        boolean autenticado = autenticarse(usuario, u.getPasswordHash());
        return autenticado ? usuario : null;
    }

    // Compara el hash de la contraseña en texto plano con el guardado.
    // Público porque LnPanelCETS lo reutiliza (hash() vive solo aquí).
    public boolean autenticarse(Usuario usuario, String password) {
        if (usuario == null) {
            return false;
        }
        return usuario.getPasswordHash().equals(hash(password));
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
