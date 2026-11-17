/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import modelo.RegistroDonacion;
import modelo.Usuario;

/**
 *
 * @author eldeivis
 */
@Stateless
public class RegistroDonacionFacade extends AbstractFacade<RegistroDonacion> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RegistroDonacionFacade() {
        super(RegistroDonacion.class);
    }
        // HU-5.2 (2.1) consultarDonaciones(donadorId)
    public List<RegistroDonacion> buscarPorDonador(Usuario u) {
        return getEntityManager()
                .createQuery("SELECT r FROM RegistroDonacion r WHERE r.usuarioid = :u "
                           + "ORDER BY r.fechaDonacion DESC", RegistroDonacion.class)
                .setParameter("u", u)
                .getResultList();
    }

    // HU-5.3 (1.1) consultarUltimaDonacion(donadorId); null si nunca donó
    public RegistroDonacion buscarUltimaPorDonador(Usuario u) {
        List<RegistroDonacion> lista = getEntityManager()
                .createQuery("SELECT r FROM RegistroDonacion r WHERE r.usuarioid = :u "
                           + "ORDER BY r.fechaDonacion DESC", RegistroDonacion.class)
                .setParameter("u", u)
                .setMaxResults(1)
                .getResultList();
        return lista.isEmpty() ? null : lista.get(0);
    }
}
