/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 *
 * @author eldeivis
 */
@Embeddable
public class InteresPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "Usuarioid")
    private int usuarioid;
    @Basic(optional = false)
    @Column(name = "SolicitudReposicionid")
    private int solicitudReposicionid;

    public InteresPK() {
    }

    public InteresPK(int usuarioid, int solicitudReposicionid) {
        this.usuarioid = usuarioid;
        this.solicitudReposicionid = solicitudReposicionid;
    }

    public int getUsuarioid() {
        return usuarioid;
    }

    public void setUsuarioid(int usuarioid) {
        this.usuarioid = usuarioid;
    }

    public int getSolicitudReposicionid() {
        return solicitudReposicionid;
    }

    public void setSolicitudReposicionid(int solicitudReposicionid) {
        this.solicitudReposicionid = solicitudReposicionid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) usuarioid;
        hash += (int) solicitudReposicionid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InteresPK)) {
            return false;
        }
        InteresPK other = (InteresPK) object;
        if (this.usuarioid != other.usuarioid) {
            return false;
        }
        if (this.solicitudReposicionid != other.solicitudReposicionid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.InteresPK[ usuarioid=" + usuarioid + ", solicitudReposicionid=" + solicitudReposicionid + " ]";
    }
    
}
