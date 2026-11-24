/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import modelo.HistorialEstatusSolicitud;
import modelo.SolicitudReposicion;

/**
 *
 * @author eldeivis
 */
@Stateless
public class HistorialEstatusSolicitudFacade extends AbstractFacade<HistorialEstatusSolicitud> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HistorialEstatusSolicitudFacade() {
        super(HistorialEstatusSolicitud.class);
    }

    // Bitácora de una solicitud: del cambio más reciente al más antiguo
    public List<HistorialEstatusSolicitud> buscarPorSolicitud(SolicitudReposicion s) {
        return getEntityManager()
                .createQuery("SELECT h FROM HistorialEstatusSolicitud h WHERE h.solicitudReposicionid = :s "
                        + "ORDER BY h.fechaCambio DESC, h.id DESC", HistorialEstatusSolicitud.class)
                .setParameter("s", s)
                .getResultList();
    }
    
}
