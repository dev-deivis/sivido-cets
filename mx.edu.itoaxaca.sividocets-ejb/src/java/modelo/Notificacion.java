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
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author eldeivis
 */
@Entity
@Table(name = "Notificacion")
@NamedQueries({
    @NamedQuery(name = "Notificacion.findAll", query = "SELECT n FROM Notificacion n"),
    @NamedQuery(name = "Notificacion.findById", query = "SELECT n FROM Notificacion n WHERE n.id = :id"),
    @NamedQuery(name = "Notificacion.findByDistanciaAproximada", query = "SELECT n FROM Notificacion n WHERE n.distanciaAproximada = :distanciaAproximada"),
    @NamedQuery(name = "Notificacion.findByEstatus", query = "SELECT n FROM Notificacion n WHERE n.estatus = :estatus"),
    @NamedQuery(name = "Notificacion.findByFechaEnvio", query = "SELECT n FROM Notificacion n WHERE n.fechaEnvio = :fechaEnvio")})
public class Notificacion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "distanciaAproximada")
    private BigDecimal distanciaAproximada;
    @Column(name = "estatus")
    private String estatus;
    @Basic(optional = false)
    @Column(name = "fechaEnvio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEnvio;
    @JoinColumn(name = "SolicitudReposicionid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private SolicitudReposicion solicitudReposicionid;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Usuario usuarioid;

    public Notificacion() {
    }

    public Notificacion(Integer id) {
        this.id = id;
    }

    public Notificacion(Integer id, Date fechaEnvio) {
        this.id = id;
        this.fechaEnvio = fechaEnvio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getDistanciaAproximada() {
        return distanciaAproximada;
    }

    public void setDistanciaAproximada(BigDecimal distanciaAproximada) {
        this.distanciaAproximada = distanciaAproximada;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Notificacion)) {
            return false;
        }
        Notificacion other = (Notificacion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Notificacion[ id=" + id + " ]";
    }
    
}
