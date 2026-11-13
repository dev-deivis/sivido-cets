package mx.edu.itoaxaca.sivido.servicio;

import jakarta.ejb.Stateless;

/**
 * EJB mínimo del módulo EJB de SIVIDO.
 * Sin al menos un EJB, Payara no despliega el módulo como EJB.
 */
@Stateless
public class InicioFacade {

    /**
     * Devuelve el mensaje de bienvenida del sistema.
     */
    public String getMensajeBienvenida() {
        return "Bienvenido a SIVIDO";
    }
}
