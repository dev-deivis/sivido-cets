/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import modelo.Usuario;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import modelo.HistorialExterno;

/**
 *
 * @author eldeivis
 */
@Stateless
public class HistorialExternoFacade extends AbstractFacade<HistorialExterno> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HistorialExternoFacade() {
        super(HistorialExterno.class);
    }

    // HU-7.3: historial que instituciones externas compartieron del donador
    public List<HistorialExterno> consultarHistorialExterno(Usuario donador) {
        return getEntityManager()
                .createQuery("SELECT h FROM HistorialExterno h WHERE h.usuarioid = :donador "
                        + "ORDER BY h.fechaDonacion DESC", HistorialExterno.class)
                .setParameter("donador", donador)
                .getResultList();
    }
}
