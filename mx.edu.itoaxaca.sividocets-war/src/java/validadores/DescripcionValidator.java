package validadores;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.ValidatorException;
import ln.LnSolicitud;

/** Descripción de la solicitud (máximo 250 una vez limpia). La regla es de LnSolicitud.descripcionValida. */
@Dependent
@FacesValidator(value = "descripcionValidator", managed = true)
public class DescripcionValidator extends ValidadorBase {

    @EJB
    private LnSolicitud lnSolicitud;

    @Override
    protected void validar(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException {
        if (!lnSolicitud.descripcionValida(texto(valor))) {
            throw error("La descripción no puede pasar de 250 caracteres");
        }
    }
}
