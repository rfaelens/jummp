package net.biomodels.jummp.core.annotation

/**
 * @author Tung
 */
class EnvironmentAwareAnnotationRender {
    final String DDMORE_TEMPLATE_NAME = "ddmore.anno"
    final String BIOMODELS_TEMPLATE_NAME = "biomodels.anno"
    final String DEFAULT_TEMPLATE_NAME = "default.anno"

    def grailsApplication

    String getTemplate() {
        final String strategy = grailsApplication.config.jummp.metadata.strategy
        switch (strategy.toLowerCase()) {
            case "ddmore": return DDMORE_TEMPLATE_NAME
            case "biomodels": return BIOMODELS_TEMPLATE_NAME
            default: return DEFAULT_TEMPLATE_NAME
        }
    }
}
