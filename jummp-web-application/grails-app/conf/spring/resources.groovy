import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import grails.util.Environment
import net.biomodels.jummp.plugins.jms.UserJmsRemoteAdapter
import net.biomodels.jummp.webapp.remote.RemoteUserService
import net.biomodels.jummp.plugins.jms.JummpApplicationJmsRemoteAdapter

// Place your Spring DSL code here
beans = {
    jummpApplicationJmsRemoteAdapter(JummpApplicationJmsRemoteAdapter) {
        jmsSynchronousService = ref("jmsSynchronousService")
    }
    jmsAuthenticationProvider(net.biomodels.jummp.webapp.JmsAuthenticationProvider) {
        remoteJummpApplicationAdapter = jummpApplicationJmsRemoteAdapter
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
