package validadores;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Base de los validadores de captura (D20). La regla vive en el Ln; cada subclase solo la consulta y pone el mensaje.
 * Un valor null o vacío no se valida aquí (eso lo hace required); un texto de solo espacios sí llega a la subclase.
 */
public abstract class ValidadorBase implements Validator<Object> {

    @Override
    public final void validate(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException {
        if (valor == null || (valor instanceof String && ((String) valor).isEmpty())) {
            return;
        }
        validar(contexto, comp, valor);
    }

    protected abstract void validar(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException;

    // Conversión segura: el valor como texto sin espacios en los extremos
    protected String texto(Object valor) {
        return valor == null ? "" : valor.toString().trim();
    }

    // Mismo texto en resumen y detalle: p:message muestra el detalle y p:messages el resumen
    protected ValidatorException error(String mensaje) {
        return new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, mensaje));
    }
}
