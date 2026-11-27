package validadores;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.ValidatorException;
import ln.LnSolicitud;

/** Formato del folio CETS. La regla es de LnSolicitud.folioConFormatoValido. */
@Dependent
@FacesValidator(value = "folioValidator", managed = true)
public class FolioValidator extends ValidadorBase {

    @EJB
    private LnSolicitud lnSolicitud;

    @Override
    protected void validar(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException {
        if (!lnSolicitud.folioConFormatoValido(texto(valor))) {
            throw error("El folio debe tener el formato CETS-OAX-AAAA-NNNN");
        }
    }
}
