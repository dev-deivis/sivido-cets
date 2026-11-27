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
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author eldeivis
 */
@Entity
@Table(name = "Hospital")
@NamedQueries({
    @NamedQuery(name = "Hospital.findAll", query = "SELECT h FROM Hospital h"),
    @NamedQuery(name = "Hospital.findById", query = "SELECT h FROM Hospital h WHERE h.id = :id"),
    @NamedQuery(name = "Hospital.findByNombre", query = "SELECT h FROM Hospital h WHERE h.nombre = :nombre"),
    @NamedQuery(name = "Hospital.findByLatitud", query = "SELECT h FROM Hospital h WHERE h.latitud = :latitud"),
    @NamedQuery(name = "Hospital.findByLongitud", query = "SELECT h FROM Hospital h WHERE h.longitud = :longitud")})
public class Hospital implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String nombre;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @Column(name = "latitud")
    private BigDecimal latitud;
    @Basic(optional = false)
    @Column(name = "longitud")
    private BigDecimal longitud;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hospitalid")
    private List<SolicitudReposicion> solicitudReposicionList;

    public Hospital() {
    }

    public Hospital(Integer id) {
        this.id = id;
    }

    public Hospital(Integer id, String nombre, BigDecimal latitud, BigDecimal longitud) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public List<SolicitudReposicion> getSolicitudReposicionList() {
        return solicitudReposicionList;
    }

    public void setSolicitudReposicionList(List<SolicitudReposicion> solicitudReposicionList) {
        this.solicitudReposicionList = solicitudReposicionList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Hospital)) {
            return false;
        }
        Hospital other = (Hospital) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Hospital[ id=" + id + " ]";
    }
    
}
