/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Table(name = "Interes")
@NamedQueries({
    @NamedQuery(name = "Interes.findAll", query = "SELECT i FROM Interes i"),
    @NamedQuery(name = "Interes.findByUsuarioid", query = "SELECT i FROM Interes i WHERE i.interesPK.usuarioid = :usuarioid"),
    @NamedQuery(name = "Interes.findBySolicitudReposicionid", query = "SELECT i FROM Interes i WHERE i.interesPK.solicitudReposicionid = :solicitudReposicionid"),
    @NamedQuery(name = "Interes.findByFechaInteres", query = "SELECT i FROM Interes i WHERE i.fechaInteres = :fechaInteres")})
public class Interes implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected InteresPK interesPK;
    @Basic(optional = false)
    @Column(name = "fechaInteres")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInteres;
    @JoinColumn(name = "SolicitudReposicionid", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private SolicitudReposicion solicitudReposicion;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;

    public Interes() {
    }

    public Interes(InteresPK interesPK) {
        this.interesPK = interesPK;
    }

    public Interes(InteresPK interesPK, Date fechaInteres) {
        this.interesPK = interesPK;
        this.fechaInteres = fechaInteres;
    }

    public Interes(int usuarioid, int solicitudReposicionid) {
        this.interesPK = new InteresPK(usuarioid, solicitudReposicionid);
    }

    public InteresPK getInteresPK() {
        return interesPK;
    }

    public void setInteresPK(InteresPK interesPK) {
        this.interesPK = interesPK;
    }

    public Date getFechaInteres() {
        return fechaInteres;
    }

    public void setFechaInteres(Date fechaInteres) {
        this.fechaInteres = fechaInteres;
    }

    public SolicitudReposicion getSolicitudReposicion() {
        return solicitudReposicion;
    }

    public void setSolicitudReposicion(SolicitudReposicion solicitudReposicion) {
        this.solicitudReposicion = solicitudReposicion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (interesPK != null ? interesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Interes)) {
            return false;
        }
        Interes other = (Interes) object;
        if ((this.interesPK == null && other.interesPK != null) || (this.interesPK != null && !this.interesPK.equals(other.interesPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Interes[ interesPK=" + interesPK + " ]";
    }
    
}
