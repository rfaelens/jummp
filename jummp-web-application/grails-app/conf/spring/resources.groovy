import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import grails.util.Environment
import net.biomodels.jummp.plugins.jms.UserJmsRemoteAdapter
import net.biomodels.jummp.webapp.remote.RemoteUserService

// Place your Spring DSL code here
beans = {
    jmsAuthenticationProvider(net.biomodels.jummp.webapp.JmsAuthenticationProvider) {
        jummpApplicationAdapterService = ref("jummpApplicationAdapterService")
    }
    jmsConnectionFactory(SingleConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'tcp://localhost:61616'
        }
    }
    userJmsRemoteAdapter(UserJmsRemoteAdapter) {
        jmsSynchronousService = ref("jmsSynchronousService")
    }
    remoteUserService(RemoteUserService) {
        remoteUserAdapter = userJmsRemoteAdapter
    }
    if (Environment.getCurrent() == Environment.DEVELOPMENT) {
        timingAspect(org.perf4j.log4j.aop.TimingAspect)
    }
}
