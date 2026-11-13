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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
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
@Table(name = "SolicitudReposicion")
@NamedQueries({
    @NamedQuery(name = "SolicitudReposicion.findAll", query = "SELECT s FROM SolicitudReposicion s"),
    @NamedQuery(name = "SolicitudReposicion.findById", query = "SELECT s FROM SolicitudReposicion s WHERE s.id = :id"),
    @NamedQuery(name = "SolicitudReposicion.findByFolioCETS", query = "SELECT s FROM SolicitudReposicion s WHERE s.folioCETS = :folioCETS"),
    @NamedQuery(name = "SolicitudReposicion.findByTipoSanguineo", query = "SELECT s FROM SolicitudReposicion s WHERE s.tipoSanguineo = :tipoSanguineo"),
    @NamedQuery(name = "SolicitudReposicion.findByFactorRh", query = "SELECT s FROM SolicitudReposicion s WHERE s.factorRh = :factorRh"),
    @NamedQuery(name = "SolicitudReposicion.findByDescripcion", query = "SELECT s FROM SolicitudReposicion s WHERE s.descripcion = :descripcion"),
    @NamedQuery(name = "SolicitudReposicion.findByEstatus", query = "SELECT s FROM SolicitudReposicion s WHERE s.estatus = :estatus"),
    @NamedQuery(name = "SolicitudReposicion.findByFechaHoraRegistro", query = "SELECT s FROM SolicitudReposicion s WHERE s.fechaHoraRegistro = :fechaHoraRegistro"),
    @NamedQuery(name = "SolicitudReposicion.findByUuidPublico", query = "SELECT s FROM SolicitudReposicion s WHERE s.uuidPublico = :uuidPublico")})
public class SolicitudReposicion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "folioCETS")
    private String folioCETS;
    @Basic(optional = false)
    @Column(name = "tipoSanguineo")
    private String tipoSanguineo;
    @Basic(optional = false)
    @Column(name = "factorRh")
    private String factorRh;
    @Column(name = "descripcion")
    private String descripcion;
    @Basic(optional = false)
    @Column(name = "estatus")
    private String estatus;
    @Basic(optional = false)
    @Column(name = "fechaHoraRegistro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaHoraRegistro;
    @Basic(optional = false)
    @Column(name = "uuidPublico")
    private String uuidPublico;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "solicitudReposicionid")
    private List<HistorialEstatusSolicitud> historialEstatusSolicitudList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "solicitudReposicionid")
    private List<Notificacion> notificacionList;
    @OneToMany(mappedBy = "solicitudReposicionid")
    private List<CuestionarioRespuesta> cuestionarioRespuestaList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "solicitudReposicion")
    private List<Interes> interesList;
    @OneToMany(mappedBy = "solicitudReposicionid")
    private List<RegistroDonacion> registroDonacionList;
    @JoinColumn(name = "Hospitalid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Hospital hospitalid;
    @JoinColumn(name = "PersonalCETSUsuarioid", referencedColumnName = "Usuarioid")
    @ManyToOne
    private PersonalCETS personalCETSUsuarioid;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Usuario usuarioid;
    @OneToMany(mappedBy = "solicitudReposicionid")
    private List<UnidadSangre> unidadSangreList;

    public SolicitudReposicion() {
        // Estado inicial (DEFAULT 'Activa' en BD)
        this.estatus = "Activa";
    }

    public SolicitudReposicion(Integer id) {
        this.id = id;
    }

    public SolicitudReposicion(Integer id, String folioCETS, String tipoSanguineo, String factorRh, String estatus, Date fechaHoraRegistro, String uuidPublico) {
        this.id = id;
        this.folioCETS = folioCETS;
        this.tipoSanguineo = tipoSanguineo;
        this.factorRh = factorRh;
        this.estatus = estatus;
        this.fechaHoraRegistro = fechaHoraRegistro;
        this.uuidPublico = uuidPublico;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFolioCETS() {
        return folioCETS;
    }

    public void setFolioCETS(String folioCETS) {
        this.folioCETS = folioCETS;
    }

    public String getTipoSanguineo() {
        return tipoSanguineo;
    }

    public void setTipoSanguineo(String tipoSanguineo) {
        this.tipoSanguineo = tipoSanguineo;
    }

    public String getFactorRh() {
        return factorRh;
    }

    public void setFactorRh(String factorRh) {
        this.factorRh = factorRh;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public Date getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public void setFechaHoraRegistro(Date fechaHoraRegistro) {
        this.fechaHoraRegistro = fechaHoraRegistro;
    }

    public String getUuidPublico() {
        return uuidPublico;
    }

    public void setUuidPublico(String uuidPublico) {
        this.uuidPublico = uuidPublico;
    }

    public List<HistorialEstatusSolicitud> getHistorialEstatusSolicitudList() {
        return historialEstatusSolicitudList;
    }

    public void setHistorialEstatusSolicitudList(List<HistorialEstatusSolicitud> historialEstatusSolicitudList) {
        this.historialEstatusSolicitudList = historialEstatusSolicitudList;
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

    public Hospital getHospitalid() {
        return hospitalid;
    }

    public void setHospitalid(Hospital hospitalid) {
        this.hospitalid = hospitalid;
    }

    public PersonalCETS getPersonalCETSUsuarioid() {
        return personalCETSUsuarioid;
    }

    public void setPersonalCETSUsuarioid(PersonalCETS personalCETSUsuarioid) {
        this.personalCETSUsuarioid = personalCETSUsuarioid;
    }

    public Usuario getUsuarioid() {
        return usuarioid;
    }

    public void setUsuarioid(Usuario usuarioid) {
        this.usuarioid = usuarioid;
    }

    public List<UnidadSangre> getUnidadSangreList() {
        return unidadSangreList;
    }

    public void setUnidadSangreList(List<UnidadSangre> unidadSangreList) {
        this.unidadSangreList = unidadSangreList;
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
        if (!(object instanceof SolicitudReposicion)) {
            return false;
        }
        SolicitudReposicion other = (SolicitudReposicion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.SolicitudReposicion[ id=" + id + " ]";
    }
    
}
