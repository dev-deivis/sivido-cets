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
import java.io.Serializable;

/**
 *
 * @author eldeivis
 */
@Entity
@Table(name = "RespuestaCriterio")
@NamedQueries({
    @NamedQuery(name = "RespuestaCriterio.findAll", query = "SELECT r FROM RespuestaCriterio r"),
    @NamedQuery(name = "RespuestaCriterio.findById", query = "SELECT r FROM RespuestaCriterio r WHERE r.id = :id"),
    @NamedQuery(name = "RespuestaCriterio.findByPreguntaTexto", query = "SELECT r FROM RespuestaCriterio r WHERE r.preguntaTexto = :preguntaTexto"),
    @NamedQuery(name = "RespuestaCriterio.findByRespuesta", query = "SELECT r FROM RespuestaCriterio r WHERE r.respuesta = :respuesta"),
    @NamedQuery(name = "RespuestaCriterio.findByExcluye", query = "SELECT r FROM RespuestaCriterio r WHERE r.excluye = :excluye")})
public class RespuestaCriterio implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "preguntaTexto")
    private String preguntaTexto;
    @Basic(optional = false)
    @Column(name = "respuesta")
    private String respuesta;
    @Basic(optional = false)
    @Column(name = "excluye")
    private boolean excluye;
    @JoinColumn(name = "CriterioExclusionid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CriterioExclusion criterioExclusionid;
    @JoinColumn(name = "CuestionarioRespuestaid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CuestionarioRespuesta cuestionarioRespuestaid;

    public RespuestaCriterio() {
    }

    public RespuestaCriterio(Integer id) {
        this.id = id;
    }

    public RespuestaCriterio(Integer id, String preguntaTexto, String respuesta, boolean excluye) {
        this.id = id;
        this.preguntaTexto = preguntaTexto;
        this.respuesta = respuesta;
        this.excluye = excluye;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPreguntaTexto() {
        return preguntaTexto;
    }

    public void setPreguntaTexto(String preguntaTexto) {
        this.preguntaTexto = preguntaTexto;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public boolean getExcluye() {
        return excluye;
    }

    public void setExcluye(boolean excluye) {
        this.excluye = excluye;
    }

    public CriterioExclusion getCriterioExclusionid() {
        return criterioExclusionid;
    }

    public void setCriterioExclusionid(CriterioExclusion criterioExclusionid) {
        this.criterioExclusionid = criterioExclusionid;
    }

    public CuestionarioRespuesta getCuestionarioRespuestaid() {
        return cuestionarioRespuestaid;
    }

    public void setCuestionarioRespuestaid(CuestionarioRespuesta cuestionarioRespuestaid) {
        this.cuestionarioRespuestaid = cuestionarioRespuestaid;
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
        if (!(object instanceof RespuestaCriterio)) {
            return false;
        }
        RespuestaCriterio other = (RespuestaCriterio) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.RespuestaCriterio[ id=" + id + " ]";
    }
    
}
