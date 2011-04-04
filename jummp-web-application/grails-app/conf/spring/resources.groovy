import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import grails.util.Environment
import org.codehaus.groovy.grails.commons.ApplicationHolder

// Place your Spring DSL code here
beans = {
    xmlns aop: "http://www.springframework.org/schema/aop"
    def grailsApplication = ApplicationHolder.application
    if (grailsApplication.config.net.biomodels.jummp.webapp.remote == "dbus") {
        println("Using DBus")
        remoteJummpApplicationAdapter(net.biomodels.jummp.dbus.remote.RemoteJummpApplicationAdapterDBusImpl)
        remoteUserAdapterDBusImpl(net.biomodels.jummp.dbus.remote.RemoteUserAdapterDBusImpl)
        remoteUserService(net.biomodels.jummp.webapp.remote.RemoteUserService) {
            remoteUserAdapter = remoteUserAdapterDBusImpl
        }
        remoteModelAdapterDBusImpl(net.biomodels.jummp.dbus.remote.RemoteModelAdapterDBusImpl)
        remoteModelService(net.biomodels.jummp.webapp.remote.RemoteModelService) {
            remoteModelAdapter = remoteModelAdapterDBusImpl
        }
        aop.config {
            pointcut(id: "dbusExceptionPointcut", expression: "execution(public * net.biomodels.jummp.dbus.remote.*.*(..))")
            advisor('pointcut-ref': "dbusExceptionPointcut", 'advice-ref': "dbusExceptionAdvice")
        }
        dbusExceptionAdvice(net.biomodels.jummp.dbus.remote.DBusExceptionAdvice)
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
        jmsConnectionFactory(SingleConnectionFactory) {
            targetConnectionFactory = { ActiveMQConnectionFactory cf ->
                brokerURL = 'tcp://localhost:61616'
            }
        }
    }
    remoteAuthenticationProvider(net.biomodels.jummp.webapp.RemoteAuthenticationProvider) {
        remoteJummpApplicationAdapter = ref("remoteJummpApplicationAdapter")
    }

    if (Environment.getCurrent() == Environment.DEVELOPMENT) {
        timingAspect(org.perf4j.log4j.aop.TimingAspect)
    }
}
