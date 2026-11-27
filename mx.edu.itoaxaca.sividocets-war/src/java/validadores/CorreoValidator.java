package validadores;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.ValidatorException;
import ln.LnCuentas;

/** Solo formato del correo (sin revisar duplicados ni credenciales). La regla es de LnCuentas.correoValido. */
@Dependent
@FacesValidator(value = "correoValidator", managed = true)
public class CorreoValidator extends ValidadorBase {

    @EJB
    private LnCuentas lnCuentas;

    @Override
    protected void validar(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException {
        if (!lnCuentas.correoValido(texto(valor))) {
            throw error("El correo no tiene un formato válido");
        }
    }
}
