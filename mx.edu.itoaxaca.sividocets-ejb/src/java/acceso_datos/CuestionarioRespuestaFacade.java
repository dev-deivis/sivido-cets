/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acceso_datos;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import modelo.CuestionarioRespuesta;

/**
 *
 * @author eldeivis
 */
@Stateless
public class CuestionarioRespuestaFacade extends AbstractFacade<CuestionarioRespuesta> {

    @PersistenceContext(unitName = "mx.edu.itoaxaca.sividocets-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CuestionarioRespuestaFacade() {
        super(CuestionarioRespuesta.class);
    }
    
}
