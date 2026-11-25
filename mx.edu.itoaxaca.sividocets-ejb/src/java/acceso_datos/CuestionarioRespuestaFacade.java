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

    // HU-4.1: guarda el cuestionario (con respuestas, por cascade) y regresa su id; el flush es necesario
    // porque la llave es IDENTITY y el id no existe hasta el INSERT.
    public int crearYObtenerId(CuestionarioRespuesta c) {
        create(c);
        getEntityManager().flush();
        return c.getId();
    }
    
}
