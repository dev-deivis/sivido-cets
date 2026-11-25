/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import modelo.Notificacion;
import modelo.SolicitudReposicion;
import modelo.Usuario;

/**
 *
 * @author eldeivis
 */
@Stateless
public class NotificacionFacade extends AbstractFacade<Notificacion> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public NotificacionFacade() {
        super(Notificacion.class);
    }

    // HU-3.1: avisos del donador, el más reciente primero
    public List<Notificacion> buscarPorUsuario(Usuario u) {
        return getEntityManager()
                .createQuery("SELECT n FROM Notificacion n WHERE n.usuarioid = :u ORDER BY n.fechaEnvio DESC", Notificacion.class)
                .setParameter("u", u)
                .getResultList();
    }

    // HU-3.1 / HU-3.3: aviso de ese donador para esa solicitud (la BD permite uno por par), o null
    public Notificacion buscarPorUsuarioYSolicitud(Usuario u, SolicitudReposicion s) {
        List<Notificacion> r = getEntityManager()
                .createQuery("SELECT n FROM Notificacion n WHERE n.usuarioid = :u AND n.solicitudReposicionid = :s", Notificacion.class)
                .setParameter("u", u)
                .setParameter("s", s)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }
    
}
