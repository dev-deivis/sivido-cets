/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import modelo.Interes;
import modelo.SolicitudReposicion;

/**
 *
 * @author eldeivis
 */
@Stateless
public class InteresFacade extends AbstractFacade<Interes> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public InteresFacade() {
        super(Interes.class);
    }

    // #17: los donadores que confirmaron interés en la solicitud, el más reciente primero
    public List<Interes> buscarPorSolicitud(SolicitudReposicion s) {
        return getEntityManager()
                .createQuery("SELECT i FROM Interes i WHERE i.solicitudReposicion = :s ORDER BY i.fechaInteres DESC",
                        Interes.class)
                .setParameter("s", s)
                .getResultList();
    }

    // HU-6.3: cuántos donadores confirmaron interés en la solicitud
    public int contarPorSolicitud(SolicitudReposicion s) {
        Long total = getEntityManager()
                .createQuery("SELECT COUNT(i) FROM Interes i WHERE i.solicitudReposicion = :s", Long.class)
                .setParameter("s", s)
                .getSingleResult();
        return total.intValue();
    }
}
