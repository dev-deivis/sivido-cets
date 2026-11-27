package validadores;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.ValidatorException;
import ln.LnCuestionario;

/**
 * Días de espera de un criterio. La regla es de LnCuestionario.duracionValida. El tipo de exclusión llega por
 * <f:attribute name="tipo" value="#{componenteDelRadio}"/>; un campo vacío con tipo Temporal lo rechaza el required
 * condicional de la vista (la base no valida los vacíos).
 */
@Dependent
@FacesValidator(value = "diasEsperaValidator", managed = true)
public class DiasEsperaValidator extends ValidadorBase {

    @EJB
    private LnCuestionario lnCuestionario;

    @Override
    protected void validar(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException {
        Integer dias = null;
        try {
            dias = valor instanceof Number ? Integer.valueOf(((Number) valor).intValue()) : Integer.valueOf(texto(valor));
        } catch (NumberFormatException e) {
            // no es un número: cuenta como sin días
        }
        if (!lnCuestionario.duracionValida(tipo(comp), dias)) {
            throw error("Indica los días de espera (mayor que 0) para una exclusión temporal");
        }
    }

    // Valor del radio del tipo: el enviado si aún no se validó; si no, el valor local ya validado
    private String tipo(UIComponent comp) {
        Object radio = comp.getAttributes().get("tipo");
        if (!(radio instanceof UIInput)) {
            return null;
        }
        UIInput campo = (UIInput) radio;
        Object tipo = campo.getSubmittedValue() != null ? campo.getSubmittedValue() : campo.getValue();
        return tipo == null ? null : tipo.toString();
    }
}
