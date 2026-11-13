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
import java.util.List;

/**
 *
 * @author eldeivis
 */
@Stateless
@LocalBean
public class LnCuentas {

    @EJB
    private UsuarioFacade usuarioF;

    // HU-1.1 Alta inicial
    public boolean registrarUsuario(String nombreCompleto, String telefono,
            String correo, String password) {
        boolean valido = validarDatos(correo);
        if (!valido) {
            return false;
        }
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setTelefono(telefono);
        usuario.setCorreo(correo);
        usuario.setPasswordHash(hash(password));
        usuario.setFechaRegistro(new Date());
        guardarUsuario(usuario);
        return true;
    }
    // HU-1.2 Autenticación

    public Usuario iniciarSesion(String correo, String password) {
        Usuario usuario = usuarioF.buscarPorCorreo(correo);
        boolean autenticado = autenticarse(usuario, password);
        return autenticado ? usuario : null;
    }

    private boolean autenticarse(Usuario usuario, String password) {
        if (usuario == null) {
            return false;
        }
        return usuario.getPasswordHash().equals(hash(password));
    }

    private boolean validarDatos(String correo) {
        boolean existe = usuarioF.existeCorreo(correo);
        return !existe;
    }

    private void guardarUsuario(Usuario usuario) {
        usuarioF.create(usuario);
    }

    public List<Usuario> lista() {
        return usuarioF.findAll();
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
