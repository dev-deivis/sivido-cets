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
@Table(name = "RegistroDonacion")
@NamedQueries({
    @NamedQuery(name = "RegistroDonacion.findAll", query = "SELECT r FROM RegistroDonacion r"),
    @NamedQuery(name = "RegistroDonacion.findById", query = "SELECT r FROM RegistroDonacion r WHERE r.id = :id"),
    @NamedQuery(name = "RegistroDonacion.findByFechaDonacion", query = "SELECT r FROM RegistroDonacion r WHERE r.fechaDonacion = :fechaDonacion"),
    @NamedQuery(name = "RegistroDonacion.findByProximaFechaElegible", query = "SELECT r FROM RegistroDonacion r WHERE r.proximaFechaElegible = :proximaFechaElegible")})
public class RegistroDonacion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "fechaDonacion")
    @Temporal(TemporalType.DATE)
    private Date fechaDonacion;
    @Column(name = "proximaFechaElegible")
    @Temporal(TemporalType.DATE)
    private Date proximaFechaElegible;
    @JoinColumn(name = "PersonalCETSUsuarioid", referencedColumnName = "Usuarioid")
    @ManyToOne(optional = false)
    private PersonalCETS personalCETSUsuarioid;
    @JoinColumn(name = "SolicitudReposicionid", referencedColumnName = "id")
    @ManyToOne
    private SolicitudReposicion solicitudReposicionid;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Usuario usuarioid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "registroDonacionid")
    private List<UnidadSangre> unidadSangreList;

    public RegistroDonacion() {
    }

    public RegistroDonacion(Integer id) {
        this.id = id;
    }

    public RegistroDonacion(Integer id, Date fechaDonacion) {
        this.id = id;
        this.fechaDonacion = fechaDonacion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechaDonacion() {
        return fechaDonacion;
    }

    public void setFechaDonacion(Date fechaDonacion) {
        this.fechaDonacion = fechaDonacion;
    }

    public Date getProximaFechaElegible() {
        return proximaFechaElegible;
    }

    public void setProximaFechaElegible(Date proximaFechaElegible) {
        this.proximaFechaElegible = proximaFechaElegible;
    }

    public PersonalCETS getPersonalCETSUsuarioid() {
        return personalCETSUsuarioid;
    }

    public void setPersonalCETSUsuarioid(PersonalCETS personalCETSUsuarioid) {
        this.personalCETSUsuarioid = personalCETSUsuarioid;
    }

    public SolicitudReposicion getSolicitudReposicionid() {
        return solicitudReposicionid;
    }

    public void setSolicitudReposicionid(SolicitudReposicion solicitudReposicionid) {
        this.solicitudReposicionid = solicitudReposicionid;
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
        if (!(object instanceof RegistroDonacion)) {
            return false;
        }
        RegistroDonacion other = (RegistroDonacion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.RegistroDonacion[ id=" + id + " ]";
    }
    
}
