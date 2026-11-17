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
import modelo.RegistroDonacion;

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

    // HU-5.4: donaciones de un donador, más recientes primero
    public List<RegistroDonacion> consultarDonaciones(Usuario donador) {
        return getEntityManager()
                .createQuery("SELECT r FROM RegistroDonacion r WHERE r.usuarioid = :donador "
                        + "ORDER BY r.fechaDonacion DESC, r.id DESC", RegistroDonacion.class)
                .setParameter("donador", donador)
                .getResultList();
    }

    // HU-5.4: última donación registrada (null si nunca ha donado)
    public RegistroDonacion consultarUltimaDonacion(Usuario donador) {
        List<RegistroDonacion> r = consultarDonaciones(donador);
        return r.isEmpty() ? null : r.get(0);
    }
}
