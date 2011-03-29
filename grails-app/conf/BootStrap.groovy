import net.biomodels.jummp.model.ModelFormat
import grails.plugin.jms.listener.ServiceInspector
import grails.plugin.jms.listener.ListenerConfigFactory

class BootStrap {
    def springSecurityService;
    def vcsService
    def grailsApplication

    def init = { servletContext ->
        vcsService.init()

        ModelFormat format = ModelFormat.findByIdentifier("UNKNWON")
        if (!format) {
            format = new ModelFormat(identifier: "UNKNOWN", name: "Unknown format")
            format.save(flush: true)
        }
        // start the JMS Listener beans
        ServiceInspector si = new ServiceInspector()
        ListenerConfigFactory listenerConfigFactory = new ListenerConfigFactory()
        def listenerConfigs = si.getListenerConfigs(net.biomodels.jummp.jms.ModelJmsAdapter, listenerConfigFactory, grailsApplication)
        listenerConfigs.each {
            it.serviceBeanName = "modelJmsAdapterService"
            grailsApplication.mainContext.getBean(it.listenerContainerBeanName).start()
        }
        listenerConfigs = si.getListenerConfigs(net.biomodels.jummp.jms.UserJmsAdapter, listenerConfigFactory, grailsApplication)
        listenerConfigs.each {
            it.serviceBeanName = "userJmsAdapterService"
            grailsApplication.mainContext.getBean(it.listenerContainerBeanName).start()
        }
        listenerConfigs = si.getListenerConfigs(net.biomodels.jummp.jms.ApplicationJmsAdapter, listenerConfigFactory, grailsApplication)
        listenerConfigs.each {
            it.serviceBeanName = "applicationJmsAdapterService"
            grailsApplication.mainContext.getBean(it.listenerContainerBeanName).start()
        }
    }
    def destroy = {
    }
}
