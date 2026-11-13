/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import modelo.Usuario;

/**
 *
 * @author eldeivis
 */
@Stateless
public class UsuarioFacade extends AbstractFacade<Usuario> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsuarioFacade() {
        super(Usuario.class);
    }

    public boolean existeCorreo(String correo) {
        return !getEntityManager()
                .createNamedQuery("Usuario.findByCorreo", Usuario.class)
                .setParameter("correo", correo)
                .getResultList()
                .isEmpty();
    }

    public Usuario buscarPorCorreo(String correo) {
        List<Usuario> r = getEntityManager()
                .createNamedQuery("Usuario.findByCorreo", Usuario.class)
                .setParameter("correo", correo)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }
}
