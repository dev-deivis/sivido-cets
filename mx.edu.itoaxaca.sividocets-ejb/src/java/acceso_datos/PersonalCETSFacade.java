/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import modelo.PersonalCETS;

/**
 *
 * @author eldeivis
 */
@Stateless
public class PersonalCETSFacade extends AbstractFacade<PersonalCETS> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PersonalCETSFacade() {
        super(PersonalCETS.class);
    }

    public PersonalCETS buscarPorCorreoInstitucional(String correo) {
        List<PersonalCETS> r = getEntityManager()
                .createNamedQuery("PersonalCETS.findByCorreoInstitucional", PersonalCETS.class)
                .setParameter("correoInstitucional", correo)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }
}
