/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import modelo.CriterioExclusion;

/**
 *
 * @author eldeivis
 */
@Stateless
public class CriterioExclusionFacade extends AbstractFacade<CriterioExclusion> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CriterioExclusionFacade() {
        super(CriterioExclusion.class);
    }

    // HU-4.1: criterios del cuestionario (los inactivos solo se ven en el catálogo del CETS, HU-4.3)
    public List<CriterioExclusion> buscarActivos() {
        return getEntityManager()
                .createQuery("SELECT c FROM CriterioExclusion c WHERE c.activo = true ORDER BY c.id", CriterioExclusion.class)
                .getResultList();
    }
    
}
