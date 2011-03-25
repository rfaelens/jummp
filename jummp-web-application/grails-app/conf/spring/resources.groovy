import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import grails.util.Environment
import org.codehaus.groovy.grails.commons.ApplicationHolder

// Place your Spring DSL code here
beans = {
    def grailsApplication = ApplicationHolder.application
    // TODO: move the JummpApplicationJmsRemoteAdapter to the JMS part
    jummpApplicationJmsRemoteAdapter(net.biomodels.jummp.plugins.jms.JummpApplicationJmsRemoteAdapter) {
        jmsSynchronousService = ref("jmsSynchronousService")
    }
    if (grailsApplication.config.net.biomodels.jummp.webapp.remote == "dbus") {
        println("Using DBus")
        remoteJummpApplicationAdapterDBusImpl(net.biomodels.jummp.dbus.remote.RemoteJummpApplicationAdapterDBusImpl)
        remoteAuthenticationProvider(net.biomodels.jummp.webapp.RemoteAuthenticationProvider) {
            remoteJummpApplicationAdapter = remoteJummpApplicationAdapterDBusImpl
        }
        remoteUserAdapterDBusImpl(net.biomodels.jummp.dbus.remote.RemoteUserAdapterDBusImpl)
        remoteUserService(net.biomodels.jummp.webapp.remote.RemoteUserService) {
            remoteUserAdapter = remoteUserAdapterDBusImpl
        }
    } else {
        println("Using JMS")
        remoteAuthenticationProvider(net.biomodels.jummp.webapp.RemoteAuthenticationProvider) {
            remoteJummpApplicationAdapter = jummpApplicationJmsRemoteAdapter
        }
        userJmsRemoteAdapter(net.biomodels.jummp.plugins.jms.UserJmsRemoteAdapter) {
            jmsSynchronousService = ref("jmsSynchronousService")
        }
        remoteUserService(net.biomodels.jummp.webapp.remote.RemoteUserService) {
            remoteUserAdapter = userJmsRemoteAdapter
        }
    }

    // TODO: move inot the JMS section
    jmsConnectionFactory(SingleConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'tcp://localhost:61616'
        }
    }
    if (Environment.getCurrent() == Environment.DEVELOPMENT) {
        timingAspect(org.perf4j.log4j.aop.TimingAspect)
    }
}
