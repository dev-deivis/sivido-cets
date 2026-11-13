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
@Table(name = "HistorialEstatusSolicitud")
@NamedQueries({
    @NamedQuery(name = "HistorialEstatusSolicitud.findAll", query = "SELECT h FROM HistorialEstatusSolicitud h"),
    @NamedQuery(name = "HistorialEstatusSolicitud.findById", query = "SELECT h FROM HistorialEstatusSolicitud h WHERE h.id = :id"),
    @NamedQuery(name = "HistorialEstatusSolicitud.findByEstatusAnterior", query = "SELECT h FROM HistorialEstatusSolicitud h WHERE h.estatusAnterior = :estatusAnterior"),
    @NamedQuery(name = "HistorialEstatusSolicitud.findByEstatusNuevo", query = "SELECT h FROM HistorialEstatusSolicitud h WHERE h.estatusNuevo = :estatusNuevo"),
    @NamedQuery(name = "HistorialEstatusSolicitud.findByFechaCambio", query = "SELECT h FROM HistorialEstatusSolicitud h WHERE h.fechaCambio = :fechaCambio")})
public class HistorialEstatusSolicitud implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "estatusAnterior")
    private String estatusAnterior;
    @Basic(optional = false)
    @Column(name = "estatusNuevo")
    private String estatusNuevo;
    @Basic(optional = false)
    @Column(name = "fechaCambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;
    @JoinColumn(name = "PersonalCETSUsuarioid", referencedColumnName = "Usuarioid")
    @ManyToOne(optional = false)
    private PersonalCETS personalCETSUsuarioid;
    @JoinColumn(name = "SolicitudReposicionid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private SolicitudReposicion solicitudReposicionid;

    public HistorialEstatusSolicitud() {
    }

    public HistorialEstatusSolicitud(Integer id) {
        this.id = id;
    }

    public HistorialEstatusSolicitud(Integer id, String estatusAnterior, String estatusNuevo, Date fechaCambio) {
        this.id = id;
        this.estatusAnterior = estatusAnterior;
        this.estatusNuevo = estatusNuevo;
        this.fechaCambio = fechaCambio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEstatusAnterior() {
        return estatusAnterior;
    }

    public void setEstatusAnterior(String estatusAnterior) {
        this.estatusAnterior = estatusAnterior;
    }

    public String getEstatusNuevo() {
        return estatusNuevo;
    }

    public void setEstatusNuevo(String estatusNuevo) {
        this.estatusNuevo = estatusNuevo;
    }

    public Date getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(Date fechaCambio) {
        this.fechaCambio = fechaCambio;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HistorialEstatusSolicitud)) {
            return false;
        }
        HistorialEstatusSolicitud other = (HistorialEstatusSolicitud) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.HistorialEstatusSolicitud[ id=" + id + " ]";
    }
    
}
