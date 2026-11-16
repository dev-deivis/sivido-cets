/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import modelo.SolicitudReposicion;

/**
 *
 * @author eldeivis
 */
@Stateless
public class SolicitudReposicionFacade extends AbstractFacade<SolicitudReposicion> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public SolicitudReposicionFacade() {
        super(SolicitudReposicion.class);
    }

    // HU-6.2: solicitudes que cumplen el filtro (estatus, tipoSanguineo, factorRh).
    // Un campo null en el filtro significa "todos". Más recientes primero.
    public List<SolicitudReposicion> buscarFiltradas(SolicitudReposicion filtro) {
        StringBuilder jpql = new StringBuilder("SELECT s FROM SolicitudReposicion s WHERE 1 = 1");
        if (filtro.getEstatus() != null) {
            jpql.append(" AND s.estatus = :estatus");
        }
        if (filtro.getTipoSanguineo() != null) {
            jpql.append(" AND s.tipoSanguineo = :tipo");
        }
        if (filtro.getFactorRh() != null) {
            jpql.append(" AND s.factorRh = :rh");
        }
        jpql.append(" ORDER BY s.fechaHoraRegistro DESC");

        TypedQuery<SolicitudReposicion> q = getEntityManager().createQuery(jpql.toString(), SolicitudReposicion.class);
        if (filtro.getEstatus() != null) {
            q.setParameter("estatus", filtro.getEstatus());
        }
        if (filtro.getTipoSanguineo() != null) {
            q.setParameter("tipo", filtro.getTipoSanguineo());
        }
        if (filtro.getFactorRh() != null) {
            q.setParameter("rh", filtro.getFactorRh());
        }
        return q.getResultList();
    }
}
