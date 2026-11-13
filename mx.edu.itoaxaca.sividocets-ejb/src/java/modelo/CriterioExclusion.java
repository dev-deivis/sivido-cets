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
@Table(name = "CriterioExclusion")
@NamedQueries({
    @NamedQuery(name = "CriterioExclusion.findAll", query = "SELECT c FROM CriterioExclusion c"),
    @NamedQuery(name = "CriterioExclusion.findById", query = "SELECT c FROM CriterioExclusion c WHERE c.id = :id"),
    @NamedQuery(name = "CriterioExclusion.findByCriterio", query = "SELECT c FROM CriterioExclusion c WHERE c.criterio = :criterio"),
    @NamedQuery(name = "CriterioExclusion.findByTipoRespuesta", query = "SELECT c FROM CriterioExclusion c WHERE c.tipoRespuesta = :tipoRespuesta"),
    @NamedQuery(name = "CriterioExclusion.findByActivo", query = "SELECT c FROM CriterioExclusion c WHERE c.activo = :activo"),
    @NamedQuery(name = "CriterioExclusion.findByTipoExclusion", query = "SELECT c FROM CriterioExclusion c WHERE c.tipoExclusion = :tipoExclusion"),
    @NamedQuery(name = "CriterioExclusion.findByDuracionDias", query = "SELECT c FROM CriterioExclusion c WHERE c.duracionDias = :duracionDias")})
public class CriterioExclusion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "criterio")
    private String criterio;
    @Basic(optional = false)
    @Column(name = "tipoRespuesta")
    private String tipoRespuesta;
    @Basic(optional = false)
    @Column(name = "activo")
    private boolean activo;
    @Basic(optional = false)
    @Column(name = "tipoExclusion")
    private String tipoExclusion;
    @Column(name = "duracionDias")
    private Integer duracionDias;
    @OneToMany(mappedBy = "criterioExclusionid")
    private List<HistorialClinico> historialClinicoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criterioExclusionid")
    private List<RespuestaCriterio> respuestaCriterioList;

    public CriterioExclusion() {
    }

    public CriterioExclusion(Integer id) {
        this.id = id;
    }

    public CriterioExclusion(Integer id, String criterio, String tipoRespuesta, boolean activo, String tipoExclusion) {
        this.id = id;
        this.criterio = criterio;
        this.tipoRespuesta = tipoRespuesta;
        this.activo = activo;
        this.tipoExclusion = tipoExclusion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCriterio() {
        return criterio;
    }

    public void setCriterio(String criterio) {
        this.criterio = criterio;
    }

    public String getTipoRespuesta() {
        return tipoRespuesta;
    }

    public void setTipoRespuesta(String tipoRespuesta) {
        this.tipoRespuesta = tipoRespuesta;
    }

    public boolean getActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getTipoExclusion() {
        return tipoExclusion;
    }

    public void setTipoExclusion(String tipoExclusion) {
        this.tipoExclusion = tipoExclusion;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }

    public List<HistorialClinico> getHistorialClinicoList() {
        return historialClinicoList;
    }

    public void setHistorialClinicoList(List<HistorialClinico> historialClinicoList) {
        this.historialClinicoList = historialClinicoList;
    }

    public List<RespuestaCriterio> getRespuestaCriterioList() {
        return respuestaCriterioList;
    }

    public void setRespuestaCriterioList(List<RespuestaCriterio> respuestaCriterioList) {
        this.respuestaCriterioList = respuestaCriterioList;
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
        if (!(object instanceof CriterioExclusion)) {
            return false;
        }
        CriterioExclusion other = (CriterioExclusion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.CriterioExclusion[ id=" + id + " ]";
    }
    
}
