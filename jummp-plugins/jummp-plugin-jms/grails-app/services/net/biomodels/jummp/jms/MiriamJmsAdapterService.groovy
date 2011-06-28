package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod

/**
 * @short Wrapper class around MiriamService exposed to JMS.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@JmsAdapter
class MiriamJmsAdapterService extends AbstractJmsAdapter {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpMiriamJms"
    static transactional = false
    IMiriamService miriamService

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[String, Boolean])
    def updateMiriamResources(def message) {
        miriamService.updateMiriamResources((String)message[1], (Boolean)message[2])
        return true
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[String])
    def miriamData(def message) {
        miriamService.miriamData((String)message[1])
    }
}
