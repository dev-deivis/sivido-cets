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

    // HU-5.4 / HU-5.1: busca donadores (no personal del CETS) por nombre, correo o id
    public List<Usuario> buscarDonadores(String criterio) {
        String texto = "%" + criterio.trim().toLowerCase() + "%";
        Integer id = null;
        try {
            id = Integer.valueOf(criterio.trim());
        } catch (NumberFormatException e) {
            // no es un id numérico
        }
        return getEntityManager()
                .createQuery("SELECT u FROM Usuario u LEFT JOIN u.personalCETS p WHERE p IS NULL AND ("
                        + "LOWER(u.nombreCompleto) LIKE :texto OR LOWER(u.correo) LIKE :texto OR u.id = :id) "
                        + "ORDER BY u.nombreCompleto", Usuario.class)
                .setParameter("texto", texto)
                .setParameter("id", id == null ? -1 : id)
                .setMaxResults(20)
                .getResultList();
    }
}
