/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import java.util.List;
import modelo.Usuario;
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
    public List<HistorialExterno> buscarPorUsuario(Usuario u) {
        return getEntityManager()
                .createQuery("SELECT h FROM HistorialExterno h WHERE h.usuarioid = :u "
                        + "ORDER BY h.fechaDonacion DESC", HistorialExterno.class)
                .setParameter("u", u)
                .getResultList();
    }
}
