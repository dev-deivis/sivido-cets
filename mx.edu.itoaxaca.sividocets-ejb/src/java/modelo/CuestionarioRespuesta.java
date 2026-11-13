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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eldeivis
 */
@Entity
@Table(name = "CuestionarioRespuesta")
@NamedQueries({
    @NamedQuery(name = "CuestionarioRespuesta.findAll", query = "SELECT c FROM CuestionarioRespuesta c"),
    @NamedQuery(name = "CuestionarioRespuesta.findById", query = "SELECT c FROM CuestionarioRespuesta c WHERE c.id = :id"),
    @NamedQuery(name = "CuestionarioRespuesta.findByResultado", query = "SELECT c FROM CuestionarioRespuesta c WHERE c.resultado = :resultado"),
    @NamedQuery(name = "CuestionarioRespuesta.findByFechaRespuesta", query = "SELECT c FROM CuestionarioRespuesta c WHERE c.fechaRespuesta = :fechaRespuesta"),
    @NamedQuery(name = "CuestionarioRespuesta.findByTiempoEsperaDias", query = "SELECT c FROM CuestionarioRespuesta c WHERE c.tiempoEsperaDias = :tiempoEsperaDias")})
public class CuestionarioRespuesta implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "resultado")
    private String resultado;
    @Basic(optional = false)
    @Column(name = "fechaRespuesta")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRespuesta;
    @Column(name = "tiempoEsperaDias")
    private Integer tiempoEsperaDias;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cuestionarioRespuestaid")
    private List<RespuestaCriterio> respuestaCriterioList;
    @JoinColumn(name = "SolicitudReposicionid", referencedColumnName = "id")
    @ManyToOne
    private SolicitudReposicion solicitudReposicionid;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Usuario usuarioid;

    public CuestionarioRespuesta() {
    }

    public CuestionarioRespuesta(Integer id) {
        this.id = id;
    }

    public CuestionarioRespuesta(Integer id, String resultado, Date fechaRespuesta) {
        this.id = id;
        this.resultado = resultado;
        this.fechaRespuesta = fechaRespuesta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public Date getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(Date fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public Integer getTiempoEsperaDias() {
        return tiempoEsperaDias;
    }

    public void setTiempoEsperaDias(Integer tiempoEsperaDias) {
        this.tiempoEsperaDias = tiempoEsperaDias;
    }

    public List<RespuestaCriterio> getRespuestaCriterioList() {
        return respuestaCriterioList;
    }

    public void setRespuestaCriterioList(List<RespuestaCriterio> respuestaCriterioList) {
        this.respuestaCriterioList = respuestaCriterioList;
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
        if (!(object instanceof CuestionarioRespuesta)) {
            return false;
        }
        CuestionarioRespuesta other = (CuestionarioRespuesta) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.CuestionarioRespuesta[ id=" + id + " ]";
    }
    
}
