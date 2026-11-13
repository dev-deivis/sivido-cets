/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author eldeivis
 */
@Entity
@Table(name = "PersonalCETS")
@NamedQueries({
    @NamedQuery(name = "PersonalCETS.findAll", query = "SELECT p FROM PersonalCETS p"),
    @NamedQuery(name = "PersonalCETS.findByUsuarioid", query = "SELECT p FROM PersonalCETS p WHERE p.usuarioid = :usuarioid"),
    @NamedQuery(name = "PersonalCETS.findByRol", query = "SELECT p FROM PersonalCETS p WHERE p.rol = :rol"),
    @NamedQuery(name = "PersonalCETS.findByCorreoInstitucional", query = "SELECT p FROM PersonalCETS p WHERE p.correoInstitucional = :correoInstitucional"),
    @NamedQuery(name = "PersonalCETS.findByActivo", query = "SELECT p FROM PersonalCETS p WHERE p.activo = :activo")})
public class PersonalCETS implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "Usuarioid")
    private Integer usuarioid;
    @Basic(optional = false)
    @Column(name = "rol")
    private String rol;
    @Basic(optional = false)
    @Column(name = "correoInstitucional")
    private String correoInstitucional;
    @Basic(optional = false)
    @Column(name = "activo")
    private boolean activo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personalCETSUsuarioid")
    private List<HistorialClinico> historialClinicoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personalCETSUsuarioid")
    private List<HistorialEstatusSolicitud> historialEstatusSolicitudList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personalCETSUsuarioid")
    private List<RegistroDonacion> registroDonacionList;
    @OneToMany(mappedBy = "personalCETSUsuarioid")
    private List<SolicitudReposicion> solicitudReposicionList;
    @JoinColumn(name = "Usuarioid", referencedColumnName = "id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Usuario usuario;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personalCETSUsuarioid")
    private List<UnidadSangre> unidadSangreList;

    public PersonalCETS() {
        // Estado inicial (DEFAULT TRUE en BD)
        this.activo = true;
    }

    public PersonalCETS(Integer usuarioid) {
        this.usuarioid = usuarioid;
    }

    public PersonalCETS(Integer usuarioid, String rol, String correoInstitucional, boolean activo) {
        this.usuarioid = usuarioid;
        this.rol = rol;
        this.correoInstitucional = correoInstitucional;
        this.activo = activo;
    }

    public Integer getUsuarioid() {
        return usuarioid;
    }

    public void setUsuarioid(Integer usuarioid) {
        this.usuarioid = usuarioid;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public boolean getActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<HistorialClinico> getHistorialClinicoList() {
        return historialClinicoList;
    }

    public void setHistorialClinicoList(List<HistorialClinico> historialClinicoList) {
        this.historialClinicoList = historialClinicoList;
    }

    public List<HistorialEstatusSolicitud> getHistorialEstatusSolicitudList() {
        return historialEstatusSolicitudList;
    }

    public void setHistorialEstatusSolicitudList(List<HistorialEstatusSolicitud> historialEstatusSolicitudList) {
        this.historialEstatusSolicitudList = historialEstatusSolicitudList;
    }

    public List<RegistroDonacion> getRegistroDonacionList() {
        return registroDonacionList;
    }

    public void setRegistroDonacionList(List<RegistroDonacion> registroDonacionList) {
        this.registroDonacionList = registroDonacionList;
    }

    public List<SolicitudReposicion> getSolicitudReposicionList() {
        return solicitudReposicionList;
    }

    public void setSolicitudReposicionList(List<SolicitudReposicion> solicitudReposicionList) {
        this.solicitudReposicionList = solicitudReposicionList;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<UnidadSangre> getUnidadSangreList() {
        return unidadSangreList;
    }

    public void setUnidadSangreList(List<UnidadSangre> unidadSangreList) {
        this.unidadSangreList = unidadSangreList;
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
        if (!(object instanceof PersonalCETS)) {
            return false;
        }
        PersonalCETS other = (PersonalCETS) object;
        if ((this.usuarioid == null && other.usuarioid != null) || (this.usuarioid != null && !this.usuarioid.equals(other.usuarioid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.PersonalCETS[ usuarioid=" + usuarioid + " ]";
    }
    
}
