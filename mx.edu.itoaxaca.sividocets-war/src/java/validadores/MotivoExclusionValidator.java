package validadores;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.ValidatorException;
import ln.LnPanelCETS;

/** Motivo de la exclusión permanente (no vacío, máximo 200). La regla es de LnPanelCETS.motivoExclusionValido. */
@Dependent
@FacesValidator(value = "motivoExclusionValidator", managed = true)
public class MotivoExclusionValidator extends ValidadorBase {

    @EJB
    private LnPanelCETS lnPanelCETS;

    @Override
    protected void validar(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException {
        if (!lnPanelCETS.motivoExclusionValido(texto(valor))) {
            throw error("El motivo debe tener entre 1 y 200 caracteres");
        }
    }
}
