package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating Weceem security policy settings.
 * @author Stefan Borufka <s.borufka@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 07/04/2013
 */
@Validateable
class CmsCommand implements Serializable {

    private static final long serialVersionUID = 1L

    String policyFile

    static constraints = {
        policyFile(nullable: false, blank: true)
    }
}
