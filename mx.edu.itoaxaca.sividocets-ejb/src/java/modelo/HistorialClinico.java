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
@Table(name = "HistorialClinico")
@NamedQueries({
    @NamedQuery(name = "HistorialClinico.findAll", query = "SELECT h FROM HistorialClinico h"),
    @NamedQuery(name = "HistorialClinico.findById", query = "SELECT h FROM HistorialClinico h WHERE h.id = :id"),
    @NamedQuery(name = "HistorialClinico.findByDiagnostico", query = "SELECT h FROM HistorialClinico h WHERE h.diagnostico = :diagnostico"),
    @NamedQuery(name = "HistorialClinico.findByFechaDeteccion", query = "SELECT h FROM HistorialClinico h WHERE h.fechaDeteccion = :fechaDeteccion"),
    @NamedQuery(name = "HistorialClinico.findByObservaciones", query = "SELECT h FROM HistorialClinico h WHERE h.observaciones = :observaciones")})
public class HistorialClinico implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "diagnostico")
    private String diagnostico;
    @Column(name = "fechaDeteccion")
    @Temporal(TemporalType.DATE)
    private Date fechaDeteccion;
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "CriterioExclusionid", referencedColumnName = "id")
    @ManyToOne
    private CriterioExclusion criterioExclusionid;
    @JoinColumn(name = "PersonalCETSUsuarioid", referencedColumnName = "Usuarioid")
    @ManyToOne(optional = false)
    private PersonalCETS personalCETSUsuarioid;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Usuario usuarioid;

    public HistorialClinico() {
    }

    public HistorialClinico(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public Date getFechaDeteccion() {
        return fechaDeteccion;
    }

    public void setFechaDeteccion(Date fechaDeteccion) {
        this.fechaDeteccion = fechaDeteccion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public CriterioExclusion getCriterioExclusionid() {
        return criterioExclusionid;
    }

    public void setCriterioExclusionid(CriterioExclusion criterioExclusionid) {
        this.criterioExclusionid = criterioExclusionid;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HistorialClinico)) {
            return false;
        }
        HistorialClinico other = (HistorialClinico) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.HistorialClinico[ id=" + id + " ]";
    }
    
}
