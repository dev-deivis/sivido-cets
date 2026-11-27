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
@Table(name = "HistorialExterno")
@NamedQueries({
    @NamedQuery(name = "HistorialExterno.findAll", query = "SELECT h FROM HistorialExterno h"),
    @NamedQuery(name = "HistorialExterno.findById", query = "SELECT h FROM HistorialExterno h WHERE h.id = :id"),
    @NamedQuery(name = "HistorialExterno.findByFechaDonacion", query = "SELECT h FROM HistorialExterno h WHERE h.fechaDonacion = :fechaDonacion"),
    @NamedQuery(name = "HistorialExterno.findByObservaciones", query = "SELECT h FROM HistorialExterno h WHERE h.observaciones = :observaciones")})
public class HistorialExterno implements Serializable {

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
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "InstitucionExternaid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private InstitucionExterna institucionExternaid;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Usuario usuarioid;

    public HistorialExterno() {
    }

    public HistorialExterno(Integer id) {
        this.id = id;
    }

    public HistorialExterno(Integer id, Date fechaDonacion) {
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public InstitucionExterna getInstitucionExternaid() {
        return institucionExternaid;
    }

    public void setInstitucionExternaid(InstitucionExterna institucionExternaid) {
        this.institucionExternaid = institucionExternaid;
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
        if (!(object instanceof HistorialExterno)) {
            return false;
        }
        HistorialExterno other = (HistorialExterno) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.HistorialExterno[ id=" + id + " ]";
    }
    
}
