import groovy.util.ConfigObject

class MaintenanceController {
	def grailsApplication
	
	def index={
		render(view: '/maintenance.gsp');
	}
	
	def turnOn={
		def bean=grailsApplication.mainContext.getBean("maintenanceMode")
		bean.turnOn()
		render(view: '/maintenance.gsp');
	}
}
