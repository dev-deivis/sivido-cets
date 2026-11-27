/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ln;

import acceso_datos.PerfilClinicoFacade;
import acceso_datos.UsuarioFacade;
import modelo.PerfilClinico;
import modelo.Usuario;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eldeivis
 */
@Stateless
@LocalBean
public class LnCuentas {

    @EJB
    private UsuarioFacade usuarioF;

    @EJB
    private PerfilClinicoFacade perfilF;

    // Valores que acepta la BD (CHECK de PerfilClinico)
    private static final List<String> TIPOS_SANGUINEOS = List.of("A", "B", "AB", "O");
    private static final List<String> FACTORES_RH = List.of("+", "-");
    private static final List<Integer> RADIOS_KM = List.of(3, 5, 10);

    // Receptores de glóbulos rojos por cada donador ("tipo" + "Rh")
    private static final Map<String, List<String>> RECEPTORES = Map.of(
            "O-", List.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"),
            "O+", List.of("O+", "A+", "B+", "AB+"),
            "A-", List.of("A-", "A+", "AB-", "AB+"),
            "A+", List.of("A+", "AB+"),
            "B-", List.of("B-", "B+", "AB-", "AB+"),
            "B+", List.of("B+", "AB+"),
            "AB-", List.of("AB-", "AB+"),
            "AB+", List.of("AB+"));

    // D20 Solo el formato del correo (no revisa si ya está registrado): algo@dominio.ext sin espacios
    public boolean correoValido(String correo) {
        return correo != null && correo.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

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

    // Perfil clínico del usuario (con su Usuario actualizado desde la BD); si no tiene, uno nuevo ligado
    // al usuario (PK compartida: usuarioid se pone al guardar).
    public PerfilClinico obtenerPerfilClinico(Usuario u) {
        Usuario usuario = usuarioF.find(u.getId());
        PerfilClinico perfil = perfilF.find(usuario.getId());
        if (perfil == null) {
            perfil = new PerfilClinico();
            perfil.setUsuario(usuario);
        }
        return perfil;
    }

    // HU-1.3 p trae tipoSanguineo, factorRh, latitud/longitud (opcionales) y p.usuario.consentimientoContacto.
    // Crea o actualiza (el radio solo con configurarRadio); false si algo no es válido, sin guardar nada.
    public boolean guardarPerfilClinico(PerfilClinico p) {
        boolean valido = validarTipoSangre(p) && validarUbicacion(p)
                && p.getUsuario() != null && p.getUsuario().getId() != null;
        if (!valido) {
            return false;
        }
        Usuario usuario = usuarioF.find(p.getUsuario().getId());
        if (usuario == null) {
            return false;
        }
        PerfilClinico perfil = perfilF.find(usuario.getId());
        if (perfil == null && !validarRadio(p.getRadioNotificacion())) {
            return false;
        }

        usuario.setConsentimientoContacto(p.getUsuario().getConsentimientoContacto());
        usuarioF.edit(usuario);

        boolean nuevo = perfil == null;
        if (nuevo) {
            perfil = new PerfilClinico();
            perfil.setUsuarioid(usuario.getId());
            perfil.setUsuario(usuario);
            perfil.setFechaRegistroPerfil(new Date());
            perfil.setRadioNotificacion(p.getRadioNotificacion());
        }
        perfil.setTipoSanguineo(p.getTipoSanguineo());
        perfil.setFactorRh(p.getFactorRh());
        if (p.getLatitud() != null) {
            // Sin coordenadas en p no se borra la ubicación ya guardada
            perfil.setLatitud(p.getLatitud().setScale(6, RoundingMode.HALF_UP));
            perfil.setLongitud(p.getLongitud().setScale(6, RoundingMode.HALF_UP));
        }
        if (nuevo) {
            perfilF.create(perfil);
        } else {
            perfilF.edit(perfil);
        }
        // Referencia inversa (Usuario.perfilClinico es mappedBy): sin ella el caché de EclipseLink deja perfilClinico
        // = null y el CETS no ve el grupo hasta redesplegar. El consentimiento no la necesita (campo del Usuario).
        usuario.setPerfilClinico(perfil);
        return true;
    }

    // HU-3.4 Radio actual del donador; el de un PerfilClinico nuevo si aún no tiene perfil
    public int obtenerRadio(Usuario u) {
        PerfilClinico perfil = u.getId() == null ? null : perfilF.find(u.getId());
        if (perfil == null) {
            return new PerfilClinico().getRadioNotificacion();
        }
        return perfil.getRadioNotificacion();
    }

    // HU-3.4 Configurar radio. p trae radioNotificacion y p.usuario. false si el radio no es 3, 5 o 10,
    // o si el usuario todavía no tiene perfil clínico.
    public boolean configurarRadio(PerfilClinico p) {
        boolean valido = validarRadio(p.getRadioNotificacion())
                && p.getUsuario() != null && p.getUsuario().getId() != null;
        if (!valido) {
            return false;
        }
        PerfilClinico perfil = perfilF.find(p.getUsuario().getId());
        if (perfil == null) {
            return false;
        }
        perfil.setRadioNotificacion(p.getRadioNotificacion());
        perfilF.edit(perfil);
        return true;
    }

    // A qué tipos de receptor puede donar glóbulos rojos el donador del perfil p (vacío si el
    // tipo o el Rh no están capturados)
    public List<String> receptoresCompatibles(PerfilClinico p) {
        if (p.getTipoSanguineo() == null || p.getFactorRh() == null) {
            return List.of();
        }
        return RECEPTORES.getOrDefault(p.getTipoSanguineo() + p.getFactorRh(), List.of());
    }

    private boolean validarTipoSangre(PerfilClinico p) {
        return p.getTipoSanguineo() != null && TIPOS_SANGUINEOS.contains(p.getTipoSanguineo())
                && p.getFactorRh() != null && FACTORES_RH.contains(p.getFactorRh());
    }

    // Latitud y longitud van juntas (o ninguna) y dentro de rango
    private boolean validarUbicacion(PerfilClinico p) {
        BigDecimal latitud = p.getLatitud();
        BigDecimal longitud = p.getLongitud();
        if (latitud == null && longitud == null) {
            return true;
        }
        if (latitud == null || longitud == null) {
            return false;
        }
        return latitud.abs().compareTo(new BigDecimal("90")) <= 0
                && longitud.abs().compareTo(new BigDecimal("180")) <= 0;
    }

    private boolean validarRadio(int radio) {
        return RADIOS_KM.contains(radio);
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
