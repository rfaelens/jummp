import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import grails.util.Environment
import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.plugin.jms.listener.ServiceInspector
import grails.plugin.jms.listener.ListenerConfigFactory

// Place your Spring DSL code here
beans = {
    xmlns aop: "http://www.springframework.org/schema/aop"
    def grailsApplication = ApplicationHolder.application

    aop.config {
        // intercept all methods annotated with PostLogging annotation
        // and pass it to PostLoggingAdvice
        pointcut(id: "postLoggingPointcut", expression: "@annotation(net.biomodels.jummp.core.events.PostLogging)")
        advisor('pointcut-ref': "postLoggingPointcut", 'advice-ref': "postLogging")
    }
    postLogging(net.biomodels.jummp.core.events.PostLoggingAdvice)

    jmsConnectionFactory(SingleConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'tcp://localhost:61616'
        }
    }

    // JMS Adapter need to have Service suffix to work with JMS Grails plugin
    userJmsAdapterService(net.biomodels.jummp.jms.UserJmsAdapter) {
        userService = ref("userService")
        authenticationHashService = ref("authenticationHashService")
    }
    modelJmsAdapterService(net.biomodels.jummp.jms.ModelJmsAdapter) {
        modelService = ref("modelDelegateService")
        authenticationHashService = ref("authenticationHashService")
    }
    applicationJmsAdapterService(net.biomodels.jummp.jms.ApplicationJmsAdapter) {
        authenticationHashService = ref("authenticationHashService")
        authenticationManager = ref("authenticationManager")
    }
    // for JMS Listeners
    ServiceInspector si = new ServiceInspector()
    ListenerConfigFactory listenerConfigFactory = new ListenerConfigFactory()
    def listenerConfigs = si.getListenerConfigs(net.biomodels.jummp.jms.ModelJmsAdapter, listenerConfigFactory, grailsApplication)
    listenerConfigs.each {
        it.serviceBeanName = "modelJmsAdapterService"
        it.register(delegate)
    }
    listenerConfigs = si.getListenerConfigs(net.biomodels.jummp.jms.UserJmsAdapter, listenerConfigFactory, grailsApplication)
    listenerConfigs.each {
        it.serviceBeanName = "userJmsAdapterService"
        it.register(delegate)
    }
    listenerConfigs = si.getListenerConfigs(net.biomodels.jummp.jms.ApplicationJmsAdapter, listenerConfigFactory, grailsApplication)
    listenerConfigs.each {
        it.serviceBeanName = "applicationJmsAdapterService"
        it.register(delegate)
    }

    // for DBus
    if (grailsApplication.config.jummp.export.dbus == true) {
        dbusManager(net.biomodels.jummp.dbus.DBusManagerImpl)
        userDBusAdapter(net.biomodels.jummp.dbus.UserDBusAdapterImpl) {
            userService = ref("userService")
            dbusManager = dbusManager
            authenticationHashService = ref("authenticationHashService")
            objectName = "/User"
        }
        applicationDBusAdapter(net.biomodels.jummp.dbus.ApplicationDBusAdapterImpl) {
            dbusManager = dbusManager
            authenticationManager = ref("authenticationManager")
            authenticationHashService = ref("authenticationHashService")
            objectName = "/Application"
        }
    }

    if (Environment.getCurrent() == Environment.DEVELOPMENT) {
        timingAspect(org.perf4j.log4j.aop.TimingAspect)
    }
}
