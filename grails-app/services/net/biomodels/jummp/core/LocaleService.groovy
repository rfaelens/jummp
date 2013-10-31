package net.biomodels.jummp.core
import grails.plugin.localevariant.LocaleVariantResolver
import javax.servlet.http.HttpServletRequest

/*
@author Raza Ali <raza.ali@ebi.ac.uk>
*/

class LocaleService implements LocaleVariantResolver {

    @SuppressWarnings("GrailsStatelessService")
    def grailsApplication		
	
    String resolveLocaleVariant(Locale locale, HttpServletRequest request) {
        grailsApplication.config.jummp.branding.deployment
    }

}


