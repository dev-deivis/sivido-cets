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
@Table(name = "IntercambioExterno")
@NamedQueries({
    @NamedQuery(name = "IntercambioExterno.findAll", query = "SELECT i FROM IntercambioExterno i"),
    @NamedQuery(name = "IntercambioExterno.findById", query = "SELECT i FROM IntercambioExterno i WHERE i.id = :id"),
    @NamedQuery(name = "IntercambioExterno.findByFechaIntercambio", query = "SELECT i FROM IntercambioExterno i WHERE i.fechaIntercambio = :fechaIntercambio"),
    @NamedQuery(name = "IntercambioExterno.findByDireccion", query = "SELECT i FROM IntercambioExterno i WHERE i.direccion = :direccion"),
    @NamedQuery(name = "IntercambioExterno.findByEstatus", query = "SELECT i FROM IntercambioExterno i WHERE i.estatus = :estatus")})
public class IntercambioExterno implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "fechaIntercambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaIntercambio;
    @Basic(optional = false)
    @Column(name = "direccion")
    private String direccion;
    @Basic(optional = false)
    @Column(name = "estatus")
    private String estatus;
    @JoinColumn(name = "InstitucionExternaid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private InstitucionExterna institucionExternaid;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Usuario usuarioid;

    public IntercambioExterno() {
    }

    public IntercambioExterno(Integer id) {
        this.id = id;
    }

    public IntercambioExterno(Integer id, Date fechaIntercambio, String direccion, String estatus) {
        this.id = id;
        this.fechaIntercambio = fechaIntercambio;
        this.direccion = direccion;
        this.estatus = estatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechaIntercambio() {
        return fechaIntercambio;
    }

    public void setFechaIntercambio(Date fechaIntercambio) {
        this.fechaIntercambio = fechaIntercambio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
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
        if (!(object instanceof IntercambioExterno)) {
            return false;
        }
        IntercambioExterno other = (IntercambioExterno) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.IntercambioExterno[ id=" + id + " ]";
    }
    
}
