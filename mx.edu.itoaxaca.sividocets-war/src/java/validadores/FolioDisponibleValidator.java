package validadores;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.ValidatorException;
import ln.LnSolicitud;

/** El folio no pertenece a otra solicitud Activa. La regla es de LnSolicitud.folioDisponible. */
@Dependent
@FacesValidator(value = "folioDisponibleValidator", managed = true)
public class FolioDisponibleValidator extends ValidadorBase {

    @EJB
    private LnSolicitud lnSolicitud;

    @Override
    protected void validar(FacesContext contexto, UIComponent comp, Object valor) throws ValidatorException {
        if (!lnSolicitud.folioDisponible(texto(valor))) {
            throw error("Ya existe una solicitud activa con ese folio");
        }
    }
}
