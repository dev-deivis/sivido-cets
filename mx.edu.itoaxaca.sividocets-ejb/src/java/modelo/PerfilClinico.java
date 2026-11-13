/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
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
@Table(name = "PerfilClinico")
@NamedQueries({
    @NamedQuery(name = "PerfilClinico.findAll", query = "SELECT p FROM PerfilClinico p"),
    @NamedQuery(name = "PerfilClinico.findByUsuarioid", query = "SELECT p FROM PerfilClinico p WHERE p.usuarioid = :usuarioid"),
    @NamedQuery(name = "PerfilClinico.findByTipoSanguineo", query = "SELECT p FROM PerfilClinico p WHERE p.tipoSanguineo = :tipoSanguineo"),
    @NamedQuery(name = "PerfilClinico.findByFactorRh", query = "SELECT p FROM PerfilClinico p WHERE p.factorRh = :factorRh"),
    @NamedQuery(name = "PerfilClinico.findByFechaRegistroPerfil", query = "SELECT p FROM PerfilClinico p WHERE p.fechaRegistroPerfil = :fechaRegistroPerfil"),
    @NamedQuery(name = "PerfilClinico.findByRadioNotificacion", query = "SELECT p FROM PerfilClinico p WHERE p.radioNotificacion = :radioNotificacion"),
    @NamedQuery(name = "PerfilClinico.findByLatitud", query = "SELECT p FROM PerfilClinico p WHERE p.latitud = :latitud"),
    @NamedQuery(name = "PerfilClinico.findByLongitud", query = "SELECT p FROM PerfilClinico p WHERE p.longitud = :longitud")})
public class PerfilClinico implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "Usuarioid")
    private Integer usuarioid;
    @Column(name = "tipoSanguineo")
    private String tipoSanguineo;
    @Column(name = "factorRh")
    private String factorRh;
    @Column(name = "fechaRegistroPerfil")
    @Temporal(TemporalType.DATE)
    private Date fechaRegistroPerfil;
    @Basic(optional = false)
    @Column(name = "radioNotificacion")
    private int radioNotificacion;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "latitud")
    private BigDecimal latitud;
    @Column(name = "longitud")
    private BigDecimal longitud;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Usuario usuario;

    public PerfilClinico() {
    }

    public PerfilClinico(Integer usuarioid) {
        this.usuarioid = usuarioid;
    }

    public PerfilClinico(Integer usuarioid, int radioNotificacion) {
        this.usuarioid = usuarioid;
        this.radioNotificacion = radioNotificacion;
    }

    public Integer getUsuarioid() {
        return usuarioid;
    }

    public void setUsuarioid(Integer usuarioid) {
        this.usuarioid = usuarioid;
    }

    public String getTipoSanguineo() {
        return tipoSanguineo;
    }

    public void setTipoSanguineo(String tipoSanguineo) {
        this.tipoSanguineo = tipoSanguineo;
    }

    public String getFactorRh() {
        return factorRh;
    }

    public void setFactorRh(String factorRh) {
        this.factorRh = factorRh;
    }

    public Date getFechaRegistroPerfil() {
        return fechaRegistroPerfil;
    }

    public void setFechaRegistroPerfil(Date fechaRegistroPerfil) {
        this.fechaRegistroPerfil = fechaRegistroPerfil;
    }

    public int getRadioNotificacion() {
        return radioNotificacion;
    }

    public void setRadioNotificacion(int radioNotificacion) {
        this.radioNotificacion = radioNotificacion;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (usuarioid != null ? usuarioid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PerfilClinico)) {
            return false;
        }
        PerfilClinico other = (PerfilClinico) object;
        if ((this.usuarioid == null && other.usuarioid != null) || (this.usuarioid != null && !this.usuarioid.equals(other.usuarioid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.PerfilClinico[ usuarioid=" + usuarioid + " ]";
    }
    
}
