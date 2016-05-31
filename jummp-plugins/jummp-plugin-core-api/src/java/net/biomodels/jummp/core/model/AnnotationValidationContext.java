package net.biomodels.jummp.core.model;

import java.io.Serializable;
import java.util.List;

import static net.biomodels.jummp.core.model.ValidationState.*;

/**
 * Created by sarala on 27/05/2016.
 */
public class AnnotationValidationContext implements Serializable {
    ValidationState validationLevel;

    String validationReport;

    public void setValidationLevel(ValidationState validationLevel) {
        this.validationLevel = validationLevel;
    }

    public void setValidationReport(String validationReport) {
        this.validationReport = validationReport;
    }
}
