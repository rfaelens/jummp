package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating Remote settings (currently only JMS).
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130705
 */
@Validateable
class RemoteCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String jummpRemote
    Boolean jummpExportJms

    static constraints = {
        //Expand as the list of remotes grows
        jummpRemote(nullable: true, blank: false, inList: ['jms'])
        jummpExportJms(blank: false)
    }
}
