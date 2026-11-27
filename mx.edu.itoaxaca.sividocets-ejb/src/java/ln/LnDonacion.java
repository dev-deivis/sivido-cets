package ln;

import acceso_datos.HistorialClinicoFacade;
import acceso_datos.HistorialExternoFacade;
import acceso_datos.PerfilClinicoFacade;
import acceso_datos.PersonalCETSFacade;
import acceso_datos.RegistroDonacionFacade;
import acceso_datos.SolicitudReposicionFacade;
import acceso_datos.UsuarioFacade;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import modelo.HistorialClinico;
import modelo.HistorialExterno;
import modelo.PerfilClinico;
import modelo.PersonalCETS;
import modelo.RegistroDonacion;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 * Lógica de negocio del paquete donaciones (ControlDonacion → LnDonacion).
 * Galilea: registro y búsqueda (HU-5.1, HU-5.4) e historial externo (HU-7.3).
 * Diego: obtenerHistorial, obtenerElegibilidad, verificarExclusion y esElegible (HU-5.2, HU-5.3).
 */
@Stateless
@LocalBean
public class LnDonacion {

    // HU-5.1 Periodo mínimo entre donaciones: 56 días (8 semanas)
    private static final int DIAS_EXCLUSION = 56;
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    @EJB
    private RegistroDonacionFacade registroF;

    @EJB
    private UsuarioFacade usuarioF;

    @EJB
    private PersonalCETSFacade personalF;

    @EJB
    private SolicitudReposicionFacade solicitudF;

    @EJB
    private HistorialExternoFacade historialExternoF;

    // #13: motivo y fecha de la exclusión permanente (HistorialClinico)
    @EJB
    private HistorialClinicoFacade historialClinicoF;

    // HU-5.1 Perfil y compatibilidad ABO/Rh vía LnCuentas (solo usa facades: sin ciclo)
    @EJB
    private PerfilClinicoFacade perfilF;

    @EJB
    private LnCuentas lnCuentas;

    // #12: la donación ligada pasa la solicitud a Atendida con su renglón de bitácora
    // (LnPanelCETS no depende de LnDonacion: sin ciclo).
    @EJB
    private LnPanelCETS lnPanelCETS;


    // HU-5.1 (1.1) registrarDonacion. r trae usuarioid, personalCETSUsuarioid (sesión), fechaDonacion y,
    // opcional, solicitudReposicionid (vacío = donación general; si viene, debe estar Activa).
    public boolean registrarDonacion(RegistroDonacion r) {
        if (r.getUsuarioid() == null || r.getUsuarioid().getId() == null
                || r.getPersonalCETSUsuarioid() == null || r.getPersonalCETSUsuarioid().getUsuarioid() == null
                || !fechaDonacionValida(r.getFechaDonacion())) {
            return false;
        }
        Usuario donador = usuarioF.find(r.getUsuarioid().getId());
        PersonalCETS personal = personalF.find(r.getPersonalCETSUsuarioid().getUsuarioid());
        if (donador == null || personal == null || !personal.getActivo()) {
            return false;                                  // solo el personal CETS activo registra donaciones
        }
        // D18: exclusión permanente, mismo día, intervalo de 56 días y compatibilidad con la solicitud
        if (motivoRechazoDonacion(r) != null) {
            return false;
        }
        // La solicitud es opcional (donación general); si viene, debe existir y estar Activa
        SolicitudReposicion s = r.getSolicitudReposicionid();
        SolicitudReposicion solicitud = null;
        if (s != null && s.getId() != null) {
            solicitud = solicitudF.find(s.getId());
            if (solicitud == null || !"Activa".equals(solicitud.getEstatus())) {
                return false;
            }
        }
        r.setUsuarioid(donador);
        r.setPersonalCETSUsuarioid(personal);
        r.setSolicitudReposicionid(solicitud);
        r.setProximaFechaElegible(calcularProximaFecha(r.getFechaDonacion()));   // 1.1.1.2
        registroF.create(r);                                                     // 1.1.2
        donador.getRegistroDonacionList().add(r);        // relación bidireccional al día (caché de JPA)
        if (solicitud != null && solicitud.getRegistroDonacionList() != null) {
            solicitud.getRegistroDonacionList().add(r);  // #35: el otro lado de la solicitud
        }
        if (personal.getRegistroDonacionList() != null) {
            personal.getRegistroDonacionList().add(r);   // #35: el personal que la registró
        }
        if (solicitud != null) {
            lnPanelCETS.cambiarEstatus(solicitud, personal, "Atendida");   // #12: misma transacción
        }
        return true;
    }

    // D20 La fecha de la donación existe y no es posterior a hoy; se compara por día en America/Mexico_City
    // (una donación de hoy nunca es futura, sin importar la hora).
    public boolean fechaDonacionValida(Date fecha) {
        return fecha != null && !aLocalDate(fecha).isAfter(LocalDate.now(ZONA));
    }

    // Constancia de donación: solo si es del usuario en sesión; null si no existe o es ajena.
    public RegistroDonacion obtenerConstancia(RegistroDonacion r, Usuario u) {
        if (r == null || r.getId() == null || u == null || u.getId() == null) {
            return null;
        }
        RegistroDonacion donacion = registroF.find(r.getId());
        boolean propia = donacion != null && donacion.getUsuarioid().getId().equals(u.getId());
        return propia ? donacion : null;
    }

    // D18 / #42: ¿se puede registrar una donación de este donador para esta solicitud (null = general)? null si
    // sí; si no, el motivo. No revisa el mismo día (sin fecha): para eso, motivoRechazoDonacion(RegistroDonacion).
    public String motivoRechazoDonacion(Usuario donador, SolicitudReposicion solicitud) {
        return motivoRechazo(donador, solicitud, null);
    }

    // D18 / #42: igual, contra la fechaDonacion del registro: no puede ser futura ni repetir un día con donación,
    // y debe quedar a 56 días o más de CUALQUIER donación del donador (antes o después, por fechas pasadas).
    public String motivoRechazoDonacion(RegistroDonacion r) {
        return motivoRechazo(r.getUsuarioid(), r.getSolicitudReposicionid(), r.getFechaDonacion());
    }

    private String motivoRechazo(Usuario d, SolicitudReposicion s, Date fecha) {
        Usuario donador = d == null || d.getId() == null ? null : usuarioF.find(d.getId());
        if (donador == null) {
            return "El donador no existe.";
        }
        if (fecha != null && !fechaDonacionValida(fecha)) {
            return "La fecha de la donación no puede ser futura.";
        }
        if (verificarExclusion(donador)) {
            return "El donador tiene exclusión permanente" + detalleExclusion(donador) + " y no puede donar.";
        }
        if (fecha != null) {
            String intervalo = motivoIntervalo(donador, fecha);
            if (intervalo != null) {
                return intervalo;
            }
        } else if (!esElegible(donador)) {
            // Sin fecha (la vista aún no la tiene): se compara contra hoy
            Date proxima = obtenerProximaFecha(obtenerElegibilidad(donador));
            return "El donador está dentro del intervalo de " + DIAS_EXCLUSION + " días entre donaciones"
                    + (proxima == null ? "." : "; podrá donar a partir del " + formatear(aLocalDate(proxima)) + ".");
        }
        if (s == null || s.getId() == null) {
            return null;                                   // donación general: sin compatibilidad que revisar
        }
        SolicitudReposicion solicitud = solicitudF.find(s.getId());
        if (solicitud == null) {
            return "La solicitud no existe.";
        }
        if (!"Activa".equals(solicitud.getEstatus())) {
            return "La solicitud ya no está activa (estatus: " + solicitud.getEstatus() + ").";
        }
        PerfilClinico perfil = perfilF.find(donador.getId());
        if (perfil == null || perfil.getTipoSanguineo() == null || perfil.getFactorRh() == null) {
            return "El donador no tiene grupo sanguíneo y Rh capturados; no se puede validar la compatibilidad con la solicitud.";
        }
        if (!lnCuentas.receptoresCompatibles(perfil).contains(solicitud.getTipoSanguineo() + solicitud.getFactorRh())) {
            return "El grupo del donador (" + perfil.getTipoSanguineo() + perfil.getFactorRh()
                    + ") no es compatible con la solicitud (" + solicitud.getTipoSanguineo()
                    + solicitud.getFactorRh() + ").";
        }
        return null;
    }

    // #13: " (motivo: …, desde dd/MM/yyyy)" de la exclusión permanente; "" si no hay registro clínico
    private String detalleExclusion(Usuario donador) {
        HistorialClinico exclusion = obtenerExclusion(donador);
        if (exclusion == null || exclusion.getDiagnostico() == null) {
            return "";
        }
        String desde = exclusion.getFechaDeteccion() == null ? ""
                : ", desde " + formatear(aLocalDate(exclusion.getFechaDeteccion()));
        return " (motivo: " + exclusion.getDiagnostico() + desde + ")";
    }

    // #13: registro clínico que explica la exclusión permanente (motivo, fecha, personal); null si no está
    // excluido o no hay registro (p. ej. se marcó por SQL).
    public HistorialClinico obtenerExclusion(Usuario u) {
        if (u == null || u.getId() == null) {
            return null;
        }
        Usuario donador = usuarioF.find(u.getId());
        if (donador == null || !Boolean.TRUE.equals(donador.getExclusionPermanente())) {
            return null;
        }
        return historialClinicoF.buscarUltimoPorUsuario(donador);
    }

    // #42: motivo por el que la fecha no respeta el intervalo con las donaciones existentes (null si lo respeta):
    // primero el mismo día; después, mínimo 56 días de diferencia en ambos sentidos.
    private String motivoIntervalo(Usuario donador, Date fecha) {
        LocalDate nueva = aLocalDate(fecha);
        List<RegistroDonacion> previas = registroF.buscarPorDonador(donador);     // más reciente primero
        for (RegistroDonacion previa : previas) {
            if (aLocalDate(previa.getFechaDonacion()).equals(nueva)) {
                return "El donador ya tiene una donación registrada ese día.";
            }
        }
        for (RegistroDonacion previa : previas) {
            LocalDate dia = aLocalDate(previa.getFechaDonacion());
            if (Math.abs(java.time.temporal.ChronoUnit.DAYS.between(dia, nueva)) < DIAS_EXCLUSION) {
                if (dia.isBefore(nueva)) {
                    return "La fecha queda a menos de " + DIAS_EXCLUSION + " días de la donación del " + formatear(dia)
                            + "; el donador podrá donar a partir del " + formatear(dia.plusDays(DIAS_EXCLUSION)) + ".";
                }
                return "La fecha queda a menos de " + DIAS_EXCLUSION + " días de una donación posterior (" + formatear(dia)
                        + "); debe ser el " + formatear(dia.minusDays(DIAS_EXCLUSION)) + " o antes, o a partir del "
                        + formatear(dia.plusDays(DIAS_EXCLUSION)) + ".";
            }
        }
        return null;
    }

    private String formatear(LocalDate fecha) {
        return String.format("%02d/%02d/%d", fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear());
    }

    // HU-5.4 (1.1) buscarDonador: criterio.nombreCompleto = texto a buscar (nombre o correo)
    public List<Usuario> buscarDonador(Usuario criterio) {
        if (criterio.getNombreCompleto() == null || criterio.getNombreCompleto().isBlank()) {
            return List.of();
        }
        return usuarioF.buscarDonadores(criterio.getNombreCompleto().trim());
    }

    // HU-5.4 (2.1) UIFichaDonador.mostrar(donadorId): lee el donador seleccionado
    public Usuario obtenerDonador(Usuario u) {
        return u.getId() == null ? null : usuarioF.find(u.getId());
    }

    // HU-7.3 (1.1) obtenerHistorialExterno
    public List<HistorialExterno> obtenerHistorialExterno(Usuario donador) {
        return historialExternoF.buscarPorUsuario(donador);
    }

    // HU-5.1 (1.1.1.2) RegistroDonacion.calcularProximaFecha: fechaDonacion + 56 días
    private Date calcularProximaFecha(Date fechaDonacion) {
        LocalDate proxima = aLocalDate(fechaDonacion).plusDays(DIAS_EXCLUSION);
        return Date.from(proxima.atStartOfDay(ZONA).toInstant());
    }

    private LocalDate aLocalDate(Date fecha) {
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZONA).toLocalDate();
    }

    // HU-5.2 (2) obtenerHistorial
    public List<RegistroDonacion> obtenerHistorial(Usuario donador) {
        return registroF.buscarPorDonador(donador);
    }

    // HU-5.3 (1) obtenerElegibilidad: última donación, o null si nunca donó
    public RegistroDonacion obtenerElegibilidad(Usuario donador) {
        return registroF.buscarUltimaPorDonador(donador);
    }

    // HU-5.3 (1.2) próxima fecha: la guardada o, si falta, fechaDonacion + 56 días.
    // No se asigna a la entidad (está administrada por JPA y se guardaría en la BD).
    public Date obtenerProximaFecha(RegistroDonacion ultima) {
        if (ultima == null) {
            return null;
        }
        if (ultima.getProximaFechaElegible() != null) {
            return ultima.getProximaFechaElegible();
        }
        return calcularProximaFecha(ultima.getFechaDonacion());
    }

    // HU-5.3 (2) verificarExclusion. D10: se lee el campo, no los padecimientos. Se hace find()
    // porque el Usuario de la sesión puede estar desactualizado.
    public boolean verificarExclusion(Usuario donador) {
        Usuario u = usuarioF.find(donador.getId());
        return u != null && Boolean.TRUE.equals(u.getExclusionPermanente());
    }

    // Para HU-3.3 ([Alex]): sin exclusión permanente y con la fecha ya cumplida
    public boolean esElegible(Usuario donador) {
        if (verificarExclusion(donador)) {
            return false;
        }
        Date proxima = obtenerProximaFecha(obtenerElegibilidad(donador));
        return proxima == null || !aLocalDate(proxima).isAfter(LocalDate.now(ZONA));
    }

    // Datos para la tarjeta de recuperación (HU-5.3)
    public long diasDesdeUltimaDonacion(RegistroDonacion ultima) {
        if (ultima == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
                aLocalDate(ultima.getFechaDonacion()), LocalDate.now(ZONA));
    }

    public int porcentajeRecuperacion(RegistroDonacion ultima) {
        return (int) Math.max(0, Math.min(100, diasDesdeUltimaDonacion(ultima) * 100 / DIAS_EXCLUSION));
    }
    
}
