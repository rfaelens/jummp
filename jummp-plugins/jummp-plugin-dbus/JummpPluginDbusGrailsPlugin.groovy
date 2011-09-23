import net.biomodels.jummp.dbus.DBusManagerImpl
import net.biomodels.jummp.dbus.DiffDataDBusAdapterImpl;
import net.biomodels.jummp.dbus.ModelHistoryDBusAdapterImpl;
import net.biomodels.jummp.dbus.UserDBusAdapterImpl
import net.biomodels.jummp.dbus.ApplicationDBusAdapterImpl
import net.biomodels.jummp.dbus.ModelDBusAdapterImpl
import net.biomodels.jummp.dbus.remote.RemoteDiffDataAdapterDBusImpl;
import net.biomodels.jummp.dbus.remote.RemoteJummpApplicationAdapterDBusImpl
import net.biomodels.jummp.dbus.remote.RemoteUserAdapterDBusImpl
import net.biomodels.jummp.dbus.remote.RemoteModelAdapterDBusImpl
import net.biomodels.jummp.dbus.remote.RemoteModelHistoryDBusAdapterImpl
import net.biomodels.jummp.dbus.SbmlDBusAdapterImpl
import net.biomodels.jummp.dbus.remote.RemoteSbmlAdapterDBusImpl
import net.biomodels.jummp.dbus.MiriamDBusAdapterImpl
import net.biomodels.jummp.dbus.remote.RemoteMiriamAdapterDBusImpl
import net.biomodels.jummp.dbus.GeneOntologyTreeDBusAdapterImpl
import net.biomodels.jummp.dbus.remote.RemoteGeneOntologyTreeDBusAdapterImpl

class JummpPluginDbusGrailsPlugin {
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
    def documentation = "http://grails.org/plugin/jummp-plugin-dbus"
    def packaging = "binary"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        if (!(application.config.jummp.plugin.dbus.export instanceof ConfigObject) && application.config.jummp.plugin.dbus.export) {
            dbusManager(DBusManagerImpl) {
                grailsApplication = ref("grailsApplication")
            }
            userDBusAdapter(UserDBusAdapterImpl) {
                userService = ref("userService")
                dbusManager = dbusManager
                authenticationHashService = ref("authenticationHashService")
                objectName = "/User"
            }
            applicationDBusAdapter(ApplicationDBusAdapterImpl) {
                dbusManager = dbusManager
                authenticationManager = ref("authenticationManager")
                authenticationHashService = ref("authenticationHashService")
                grailsApplication = ref("grailsApplication")
                objectName = "/Application"
            }
            modelDBusAdapter(ModelDBusAdapterImpl) {
                dbusManager = dbusManager
                modelDelegateService = ref("modelDelegateService")
                authenticationHashService = ref("authenticationHashService")
                objectName = "/Model"
            }
            sbmlDBusAdapter(SbmlDBusAdapterImpl) {
                dbusManager = dbusManager
                authenticationHashService = ref("authenticationHashService")
                sbmlService = ref("sbmlService")
                modelDelegateService = ref("modelDelegateService")
                objectName = "/SBML"
            }
            miriamDBusAdapter(MiriamDBusAdapterImpl) {
                dbusManager = dbusManager
                authenticationHashService = ref("authenticationHashService")
                miriamService = ref("miriamService")
                objectName = "/Miriam"
            }
            geneOntologyTreeDBusAdapter(GeneOntologyTreeDBusAdapterImpl) {
                dbusManager = dbusManager
                authenticationHashService = ref("authenticationHashService")
                geneOntologyTreeService = ref("geneOntologyTreeService")
                objectName = "/GOTree"
            }
			diffDataDBusAdapter(DiffDataDBusAdapterImpl) {
                dbusManager = dbusManager
                authenticationHashService = ref("authenticationHashService")
                diffDataService = ref("diffDataService")
                objectName = "/DiffData"
            }
            modelHistoryDBusAdapter(ModelHistoryDBusAdapterImpl) {
                dbusManager = dbusManager
                authenticationHashService = ref("authenticationHashService")
                modelHistoryService = ref("modelHistoryService")
                objectName = "/ModelHistory"
            }
        }
        if (!(application.config.jummp.plugin.dbus.remote instanceof ConfigObject) && application.config.jummp.plugin.dbus.remote) {
            remoteJummpApplicationAdapter(RemoteJummpApplicationAdapterDBusImpl)
            remoteUserAdapterDBusImpl(RemoteUserAdapterDBusImpl)
            remoteModelAdapterDBusImpl(RemoteModelAdapterDBusImpl)
            remoteSbmlAdapterDBusImpl(RemoteSbmlAdapterDBusImpl)
            remoteMiriamAdapterDBusImpl(RemoteMiriamAdapterDBusImpl)
			remoteDiffDataAdapterDBusImpl(RemoteDiffDataAdapterDBusImpl)
            remoteGeneOntologyTreeDBusAdapterImpl(RemoteGeneOntologyTreeDBusAdapterImpl)
            remoteModelHistoryDBusAdapterImpl(RemoteModelHistoryDBusAdapterImpl)
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
