/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import modelo.HistorialClinico;
import modelo.Usuario;

/**
 *
 * @author eldeivis
 */
@Stateless
public class HistorialClinicoFacade extends AbstractFacade<HistorialClinico> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HistorialClinicoFacade() {
        super(HistorialClinico.class);
    }

    // Último registro clínico del usuario (el más reciente primero); null si no tiene
    public HistorialClinico buscarUltimoPorUsuario(Usuario u) {
        List<HistorialClinico> r = getEntityManager()
                .createQuery("SELECT h FROM HistorialClinico h WHERE h.usuarioid = :u "
                        + "ORDER BY h.fechaDeteccion DESC, h.id DESC", HistorialClinico.class)
                .setParameter("u", u)
                .setMaxResults(1)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }
    
}
