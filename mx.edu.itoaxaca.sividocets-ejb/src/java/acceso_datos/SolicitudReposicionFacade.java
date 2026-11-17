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

    // HU-2.1 / HU-2.4: ¿ya existe una solicitud ACTIVA con ese folio?
    public boolean existeFolioActivo(String folioCETS) {
        return !getEntityManager()
                .createQuery("SELECT s.id FROM SolicitudReposicion s "
                        + "WHERE UPPER(s.folioCETS) = :folio AND s.estatus = 'Activa'")
                .setParameter("folio", folioCETS.toUpperCase())
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    // HU-2.2: publicación por su UUID público (null si no existe)
    public SolicitudReposicion buscarPorUuid(String uuidPublico) {
        List<SolicitudReposicion> r = getEntityManager()
                .createNamedQuery("SolicitudReposicion.findByUuidPublico", SolicitudReposicion.class)
                .setParameter("uuidPublico", uuidPublico)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    // HU-2.3: solicitudes del usuario, más recientes primero
    public List<SolicitudReposicion> consultarSolicitudes(Usuario usuario) {
        return getEntityManager()
                .createQuery("SELECT s FROM SolicitudReposicion s WHERE s.usuarioid = :usuario "
                        + "ORDER BY s.fechaHoraRegistro DESC", SolicitudReposicion.class)
                .setParameter("usuario", usuario)
                .getResultList();
    }

    // HU-5.1: solicitudes activas para vincular una donación
    public List<SolicitudReposicion> listarActivas() {
        return getEntityManager()
                .createQuery("SELECT s FROM SolicitudReposicion s WHERE s.estatus = 'Activa' "
                        + "ORDER BY s.fechaHoraRegistro DESC", SolicitudReposicion.class)
                .getResultList();
    }
}
