import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import grails.util.Environment
import org.codehaus.groovy.grails.commons.ApplicationHolder

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
