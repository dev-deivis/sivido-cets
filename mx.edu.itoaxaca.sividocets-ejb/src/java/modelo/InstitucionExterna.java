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
import java.util.List;

/**
 *
 * @author eldeivis
 */
@Entity
@Table(name = "InstitucionExterna")
@NamedQueries({
    @NamedQuery(name = "InstitucionExterna.findAll", query = "SELECT i FROM InstitucionExterna i"),
    @NamedQuery(name = "InstitucionExterna.findById", query = "SELECT i FROM InstitucionExterna i WHERE i.id = :id"),
    @NamedQuery(name = "InstitucionExterna.findByNombreInstitucion", query = "SELECT i FROM InstitucionExterna i WHERE i.nombreInstitucion = :nombreInstitucion"),
    @NamedQuery(name = "InstitucionExterna.findByTipoConexion", query = "SELECT i FROM InstitucionExterna i WHERE i.tipoConexion = :tipoConexion"),
    @NamedQuery(name = "InstitucionExterna.findByContactoTecnico", query = "SELECT i FROM InstitucionExterna i WHERE i.contactoTecnico = :contactoTecnico"),
    @NamedQuery(name = "InstitucionExterna.findByActiva", query = "SELECT i FROM InstitucionExterna i WHERE i.activa = :activa")})
public class InstitucionExterna implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "nombreInstitucion")
    private String nombreInstitucion;
    @Column(name = "tipoConexion")
    private String tipoConexion;
    @Column(name = "contactoTecnico")
    private String contactoTecnico;
    @Basic(optional = false)
    @Column(name = "activa")
    private boolean activa;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "institucionExternaid")
    private List<IntercambioExterno> intercambioExternoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "institucionExternaid")
    private List<HistorialExterno> historialExternoList;

    public InstitucionExterna() {
        // Estado inicial (DEFAULT TRUE en BD)
        this.activa = true;
    }

    public InstitucionExterna(Integer id) {
        this.id = id;
    }

    public InstitucionExterna(Integer id, boolean activa) {
        this.id = id;
        this.activa = activa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreInstitucion() {
        return nombreInstitucion;
    }

    public void setNombreInstitucion(String nombreInstitucion) {
        this.nombreInstitucion = nombreInstitucion;
    }

    public String getTipoConexion() {
        return tipoConexion;
    }

    public void setTipoConexion(String tipoConexion) {
        this.tipoConexion = tipoConexion;
    }

    public String getContactoTecnico() {
        return contactoTecnico;
    }

    public void setContactoTecnico(String contactoTecnico) {
        this.contactoTecnico = contactoTecnico;
    }

    public boolean getActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public List<IntercambioExterno> getIntercambioExternoList() {
        return intercambioExternoList;
    }

    public void setIntercambioExternoList(List<IntercambioExterno> intercambioExternoList) {
        this.intercambioExternoList = intercambioExternoList;
    }

    public List<HistorialExterno> getHistorialExternoList() {
        return historialExternoList;
    }

    public void setHistorialExternoList(List<HistorialExterno> historialExternoList) {
        this.historialExternoList = historialExternoList;
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
        if (!(object instanceof InstitucionExterna)) {
            return false;
        }
        InstitucionExterna other = (InstitucionExterna) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.InstitucionExterna[ id=" + id + " ]";
    }
    
}
