import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import net.biomodels.jummp.jms.remote.RemoteJummpApplicationAdapterJmsImpl
import net.biomodels.jummp.jms.remote.RemoteUserAdapterJmsImpl
import net.biomodels.jummp.jms.remote.RemoteModelAdapterJmsImpl
import net.biomodels.jummp.jms.remote.RemoteSbmlAdapterJmsImpl

class JummpPluginJmsRemoteGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def author = "Your name"
    def authorEmail = ""
    def title = "Plugin summary/headline"
    def description = '''\\
Brief description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/jummp-plugin-jms-remote"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        jmsConnectionFactory(SingleConnectionFactory) {
            targetConnectionFactory = { ActiveMQConnectionFactory cf ->
                brokerURL = 'tcp://localhost:61616'
            }
        }
        if (!(application.config.jummp.plugin.jms.remote instanceof ConfigObject) && application.config.jummp.plugin.jms.remote) {
            remoteJummpApplicationAdapter(RemoteJummpApplicationAdapterJmsImpl) {
                jmsSynchronousService = ref("jmsSynchronousService")
            }
            userJmsRemoteAdapter(RemoteUserAdapterJmsImpl) {
                jmsSynchronousService = ref("jmsSynchronousService")
            }
            jmsModelAdapter(RemoteModelAdapterJmsImpl) {
                jmsSynchronousService = ref("jmsSynchronousService")
            }
            sbmlJmsRemoteAdapter(RemoteSbmlAdapterJmsImpl) {
                jmsSynchronousService = ref("jmsSynchronousService")
            }
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
