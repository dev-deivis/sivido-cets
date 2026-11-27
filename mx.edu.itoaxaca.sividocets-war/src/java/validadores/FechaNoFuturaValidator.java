package validadores;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.ValidatorException;
import java.util.Date;
import ln.LnDonacion;

/** Fecha de la donación: no futura. La regla es de LnDonacion.fechaDonacionValida (por día, America/Mexico_City). */
@Dependent
@FacesValidator(value = "fechaNoFuturaValidator", managed = true)
public class FechaNoFuturaValidator extends ValidadorBase {

    @EJB
    private LnDonacion lnDonacion;

    @Override
    protected void validar(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException {
        if (valor instanceof Date && !lnDonacion.fechaDonacionValida((Date) valor)) {
            throw error("La fecha de la donación no puede ser futura");
        }
    }
}
