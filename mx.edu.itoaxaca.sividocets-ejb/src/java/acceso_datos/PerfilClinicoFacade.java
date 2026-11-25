/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import modelo.PerfilClinico;

/**
 *
 * @author eldeivis
 */
@Stateless
public class PerfilClinicoFacade extends AbstractFacade<PerfilClinico> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PerfilClinicoFacade() {
        super(PerfilClinico.class);
    }

    // HU-3.1: donadores a quienes se puede avisar: con grupo, Rh y ubicación (D1: sin coordenadas
    // no se notifica) y sin exclusión permanente (D10).
    public List<PerfilClinico> buscarDonadoresActivos() {
        return getEntityManager()
                .createQuery("SELECT p FROM PerfilClinico p "
                        + "WHERE p.tipoSanguineo IS NOT NULL AND p.factorRh IS NOT NULL "
                        + "AND p.latitud IS NOT NULL AND p.longitud IS NOT NULL "
                        + "AND p.usuario.exclusionPermanente = false", PerfilClinico.class)
                .getResultList();
    }
    
}
