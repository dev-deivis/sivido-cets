/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eldeivis
 */
@Entity
@Table(name = "Usuario")
@NamedQueries({
    @NamedQuery(name = "Usuario.findAll", query = "SELECT u FROM Usuario u"),
    @NamedQuery(name = "Usuario.findById", query = "SELECT u FROM Usuario u WHERE u.id = :id"),
    @NamedQuery(name = "Usuario.findByNombreCompleto", query = "SELECT u FROM Usuario u WHERE u.nombreCompleto = :nombreCompleto"),
    @NamedQuery(name = "Usuario.findByTelefono", query = "SELECT u FROM Usuario u WHERE u.telefono = :telefono"),
    @NamedQuery(name = "Usuario.findByCorreo", query = "SELECT u FROM Usuario u WHERE u.correo = :correo"),
    @NamedQuery(name = "Usuario.findByPasswordHash", query = "SELECT u FROM Usuario u WHERE u.passwordHash = :passwordHash"),
    @NamedQuery(name = "Usuario.findByFechaRegistro", query = "SELECT u FROM Usuario u WHERE u.fechaRegistro = :fechaRegistro"),
    @NamedQuery(name = "Usuario.findByConsentimientoContacto", query = "SELECT u FROM Usuario u WHERE u.consentimientoContacto = :consentimientoContacto"),
    @NamedQuery(name = "Usuario.findByExclusionPermanente", query = "SELECT u FROM Usuario u WHERE u.exclusionPermanente = :exclusionPermanente"),
    @NamedQuery(name = "Usuario.findByConsentimientoInteroperabilidad", query = "SELECT u FROM Usuario u WHERE u.consentimientoInteroperabilidad = :consentimientoInteroperabilidad"),
    @NamedQuery(name = "Usuario.findByFechaConsentimiento", query = "SELECT u FROM Usuario u WHERE u.fechaConsentimiento = :fechaConsentimiento")})
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombreCompleto")
    private String nombreCompleto;
    @Basic(optional = false)
    @Column(name = "telefono")
    private String telefono;
    @Basic(optional = false)
    @Column(name = "correo")
    private String correo;
    @Basic(optional = false)
    @Column(name = "passwordHash")
    private String passwordHash;
    @Column(name = "fechaRegistro")
    @Temporal(TemporalType.DATE)
    private Date fechaRegistro;
    @Basic(optional = false)
    @Column(name = "consentimientoContacto")
    private boolean consentimientoContacto;
    @Basic(optional = false)
    @Column(name = "exclusionPermanente")
    private boolean exclusionPermanente;
    @Basic(optional = false)
    @Column(name = "consentimientoInteroperabilidad")
    private boolean consentimientoInteroperabilidad;
    @Column(name = "fechaConsentimiento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaConsentimiento;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioid")
    private List<HistorialClinico> historialClinicoList;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "usuario")
    private PerfilClinico perfilClinico;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioid")
    private List<Notificacion> notificacionList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioid")
    private List<CuestionarioRespuesta> cuestionarioRespuestaList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuario")
    private List<Interes> interesList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioid")
    private List<RegistroDonacion> registroDonacionList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioid")
    private List<IntercambioExterno> intercambioExternoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioid")
    private List<HistorialExterno> historialExternoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioid")
    private List<SolicitudReposicion> solicitudReposicionList;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "usuario")
    private PersonalCETS personalCETS;

    public Usuario() {
    }

    public Usuario(Integer id) {
        this.id = id;
    }

    public Usuario(Integer id, String nombreCompleto, String telefono, String correo, String passwordHash, boolean consentimientoContacto, boolean exclusionPermanente, boolean consentimientoInteroperabilidad) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.consentimientoContacto = consentimientoContacto;
        this.exclusionPermanente = exclusionPermanente;
        this.consentimientoInteroperabilidad = consentimientoInteroperabilidad;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean getConsentimientoContacto() {
        return consentimientoContacto;
    }

    public void setConsentimientoContacto(boolean consentimientoContacto) {
        this.consentimientoContacto = consentimientoContacto;
    }

    public boolean getExclusionPermanente() {
        return exclusionPermanente;
    }

    public void setExclusionPermanente(boolean exclusionPermanente) {
        this.exclusionPermanente = exclusionPermanente;
    }

    public boolean getConsentimientoInteroperabilidad() {
        return consentimientoInteroperabilidad;
    }

    public void setConsentimientoInteroperabilidad(boolean consentimientoInteroperabilidad) {
        this.consentimientoInteroperabilidad = consentimientoInteroperabilidad;
    }

    public Date getFechaConsentimiento() {
        return fechaConsentimiento;
    }

    public void setFechaConsentimiento(Date fechaConsentimiento) {
        this.fechaConsentimiento = fechaConsentimiento;
    }

    public List<HistorialClinico> getHistorialClinicoList() {
        return historialClinicoList;
    }

    public void setHistorialClinicoList(List<HistorialClinico> historialClinicoList) {
        this.historialClinicoList = historialClinicoList;
    }

    public PerfilClinico getPerfilClinico() {
        return perfilClinico;
    }

    public void setPerfilClinico(PerfilClinico perfilClinico) {
        this.perfilClinico = perfilClinico;
    }

    public List<Notificacion> getNotificacionList() {
        return notificacionList;
    }

    public void setNotificacionList(List<Notificacion> notificacionList) {
        this.notificacionList = notificacionList;
    }

    public List<CuestionarioRespuesta> getCuestionarioRespuestaList() {
        return cuestionarioRespuestaList;
    }

    public void setCuestionarioRespuestaList(List<CuestionarioRespuesta> cuestionarioRespuestaList) {
        this.cuestionarioRespuestaList = cuestionarioRespuestaList;
    }

    public List<Interes> getInteresList() {
        return interesList;
    }

    public void setInteresList(List<Interes> interesList) {
        this.interesList = interesList;
    }

    public List<RegistroDonacion> getRegistroDonacionList() {
        return registroDonacionList;
    }

    public void setRegistroDonacionList(List<RegistroDonacion> registroDonacionList) {
        this.registroDonacionList = registroDonacionList;
    }

    public List<IntercambioExterno> getIntercambioExternoList() {
        return intercambioExternoList;
    }

    public void setIntercambioExternoList(List<IntercambioExterno> intercambioExternoList) {
        this.intercambioExternoList = intercambioExternoList;
    }

    public List<HistorialExterno> getHistorialExternoList() {
        return historialExternoList;
    }

    public void setHistorialExternoList(List<HistorialExterno> historialExternoList) {
        this.historialExternoList = historialExternoList;
    }

    public List<SolicitudReposicion> getSolicitudReposicionList() {
        return solicitudReposicionList;
    }

    public void setSolicitudReposicionList(List<SolicitudReposicion> solicitudReposicionList) {
        this.solicitudReposicionList = solicitudReposicionList;
    }

    public PersonalCETS getPersonalCETS() {
        return personalCETS;
    }

    public void setPersonalCETS(PersonalCETS personalCETS) {
        this.personalCETS = personalCETS;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Usuario[ id=" + id + " ]";
    }
    
}
