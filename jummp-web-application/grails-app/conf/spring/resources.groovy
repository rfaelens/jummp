import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory

// Place your Spring DSL code here
beans = {
    jmsAuthenticationProvider(net.biomodels.jummp.webapp.JmsAuthenticationProvider) {
        coreAdapterService = ref("coreAdapterService")
    }
    jmsConnectionFactory(SingleConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'tcp://localhost:61616'
        }
    }
}
