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
        remoteSbmlService(net.biomodels.jummp.webapp.remote.RemoteSbmlService) {
            remoteSbmlAdapter = ref("remoteSbmlAdapterDBusImpl")
        }
        remoteMiriamService(net.biomodels.jummp.webapp.remote.RemoteMiriamService) {
            iMiriamService = ref("remoteMiriamAdapterDBusImpl")
        }
        remoteDiffDataService(net.biomodels.jummp.webapp.remote.RemoteDiffDataService) {
            remoteDiffDataAdapter = ref("remoteDiffDataAdapterDBusImpl")
        }
        remoteGeneOntologyTreeService(net.biomodels.jummp.webapp.remote.RemoteGeneOntologyTreeService) {
            remoteGeneOntologyTreeAdapter = ref("remoteGeneOntologyTreeDBusAdapterImpl")
        }
    } else {
        println("Using JMS")
        remoteUserService(net.biomodels.jummp.webapp.remote.RemoteUserService) {
            remoteUserAdapter = ref("userJmsRemoteAdapter")
        }
        remoteModelService(net.biomodels.jummp.webapp.remote.RemoteModelService) {
            remoteModelAdapter = ref("jmsModelAdapter")
        }
        remoteSbmlService(net.biomodels.jummp.webapp.remote.RemoteSbmlService) {
            remoteSbmlAdapter = ref("sbmlJmsRemoteAdapter")
        }
        remoteMiriamService(net.biomodels.jummp.webapp.remote.RemoteMiriamService) {
            iMiriamService = ref("miriamJmsRemoteAdapter")
        }
        remoteDiffDataService(net.biomodels.jummp.webapp.remote.RemoteDiffDataService) {
            remoteDiffDataAdapter = ref("diffDataJmsRemoteAdapter")
        }
    }

    remoteAuthenticationProvider(net.biomodels.jummp.webapp.RemoteAuthenticationProvider) {
        remoteJummpApplicationAdapter = ref("remoteJummpApplicationAdapter")
    }

    if (Environment.getCurrent() == Environment.DEVELOPMENT) {
        timingAspect(org.perf4j.log4j.aop.TimingAspect)
    }

    authenticationCheckFilter(net.biomodels.jummp.webapp.CheckAuthenticationFilter) {
        remoteJummpApplicationAdapter = ref("remoteJummpApplicationAdapter")
    }
}
