import grails.util.Environment
import org.codehaus.groovy.grails.commons.ApplicationHolder

// Place your Spring DSL code here
beans = {
    xmlns aop: "http://www.springframework.org/schema/aop"
    def grailsApplication = ApplicationHolder.application
    if (grailsApplication.config.net.biomodels.jummp.webapp.remote == "dbus") {
        println("Using DBus")
        remoteUserService(net.biomodels.jummp.webapp.remote.RemoteUserService) {
            remoteUserAdapter = ref("remoteUserAdapterDBusImpl")
        }
        remoteModelService(net.biomodels.jummp.webapp.remote.RemoteModelService) {
            remoteModelAdapter = ref("remoteModelAdapterDBusImpl")
        }
    } else {
        println("Using JMS")
        remoteJummpApplicationAdapter(net.biomodels.jummp.jms.remote.RemoteJummpApplicationAdapterJmsImpl) {
            jmsSynchronousService = ref("jmsSynchronousService")
        }
        userJmsRemoteAdapter(net.biomodels.jummp.jms.remote.RemoteUserAdapterJmsImpl) {
            jmsSynchronousService = ref("jmsSynchronousService")
        }
        remoteUserService(net.biomodels.jummp.webapp.remote.RemoteUserService) {
            remoteUserAdapter = userJmsRemoteAdapter
        }
        jmsModelAdapter(net.biomodels.jummp.jms.remote.RemoteModelAdapterJmsImpl) {
            jmsSynchronousService = ref("jmsSynchronousService")
        }
        remoteModelService(net.biomodels.jummp.webapp.remote.RemoteModelService) {
            remoteModelAdapter = jmsModelAdapter
        }
    }

    remoteAuthenticationProvider(net.biomodels.jummp.webapp.RemoteAuthenticationProvider) {
        remoteJummpApplicationAdapter = ref("remoteJummpApplicationAdapter")
    }

    if (Environment.getCurrent() == Environment.DEVELOPMENT) {
        timingAspect(org.perf4j.log4j.aop.TimingAspect)
    }
}
