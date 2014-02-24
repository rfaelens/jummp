import groovy.util.ConfigObject

class MaintenanceController {
	def grailsApplication
	
	def index={
		render(view: '/maintenance.gsp');
	}
	
	def turnOn={
		def bean=grailsApplication.mainContext.getBean("maintenanceMode")
		System.out.println(bean.maintenanceMode)
		bean.turnOn()
		System.out.println(bean.maintenanceMode)
		render(view: '/maintenance.gsp');
	}
}
