/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author eldeivis
 */
@Entity
@Table(name = "UnidadSangre")
@NamedQueries({
    @NamedQuery(name = "UnidadSangre.findAll", query = "SELECT u FROM UnidadSangre u"),
    @NamedQuery(name = "UnidadSangre.findById", query = "SELECT u FROM UnidadSangre u WHERE u.id = :id"),
    @NamedQuery(name = "UnidadSangre.findByTipoSanguineo", query = "SELECT u FROM UnidadSangre u WHERE u.tipoSanguineo = :tipoSanguineo"),
    @NamedQuery(name = "UnidadSangre.findByFactorRh", query = "SELECT u FROM UnidadSangre u WHERE u.factorRh = :factorRh"),
    @NamedQuery(name = "UnidadSangre.findByComponente", query = "SELECT u FROM UnidadSangre u WHERE u.componente = :componente"),
    @NamedQuery(name = "UnidadSangre.findByFechaExtraccion", query = "SELECT u FROM UnidadSangre u WHERE u.fechaExtraccion = :fechaExtraccion"),
    @NamedQuery(name = "UnidadSangre.findByFechaCaducidad", query = "SELECT u FROM UnidadSangre u WHERE u.fechaCaducidad = :fechaCaducidad"),
    @NamedQuery(name = "UnidadSangre.findByEstatus", query = "SELECT u FROM UnidadSangre u WHERE u.estatus = :estatus")})
public class UnidadSangre implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "tipoSanguineo")
    private String tipoSanguineo;
    @Column(name = "factorRh")
    private String factorRh;
    @Column(name = "componente")
    private String componente;
    @Column(name = "fechaExtraccion")
    @Temporal(TemporalType.DATE)
    private Date fechaExtraccion;
    @Column(name = "fechaCaducidad")
    @Temporal(TemporalType.DATE)
    private Date fechaCaducidad;
    @Column(name = "estatus")
    private String estatus;
    @JoinColumn(name = "PersonalCETSUsuarioid", referencedColumnName = "Usuarioid")
    @ManyToOne(optional = false)
    private PersonalCETS personalCETSUsuarioid;
    @JoinColumn(name = "RegistroDonacionid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RegistroDonacion registroDonacionid;
    @JoinColumn(name = "SolicitudReposicionid", referencedColumnName = "id")
    @ManyToOne
    private SolicitudReposicion solicitudReposicionid;

    public UnidadSangre() {
    }

    public UnidadSangre(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getComponente() {
        return componente;
    }

    public void setComponente(String componente) {
        this.componente = componente;
    }

    public Date getFechaExtraccion() {
        return fechaExtraccion;
    }

    public void setFechaExtraccion(Date fechaExtraccion) {
        this.fechaExtraccion = fechaExtraccion;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public PersonalCETS getPersonalCETSUsuarioid() {
        return personalCETSUsuarioid;
    }

    public void setPersonalCETSUsuarioid(PersonalCETS personalCETSUsuarioid) {
        this.personalCETSUsuarioid = personalCETSUsuarioid;
    }

    public RegistroDonacion getRegistroDonacionid() {
        return registroDonacionid;
    }

    public void setRegistroDonacionid(RegistroDonacion registroDonacionid) {
        this.registroDonacionid = registroDonacionid;
    }

    public SolicitudReposicion getSolicitudReposicionid() {
        return solicitudReposicionid;
    }

    public void setSolicitudReposicionid(SolicitudReposicion solicitudReposicionid) {
        this.solicitudReposicionid = solicitudReposicionid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UnidadSangre)) {
            return false;
        }
        UnidadSangre other = (UnidadSangre) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.UnidadSangre[ id=" + id + " ]";
    }
    
}
