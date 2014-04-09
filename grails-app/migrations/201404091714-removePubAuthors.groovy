import net.biomodels.jummp.plugins.security.Person
import net.biomodels.jummp.plugins.security.User

databaseChangeLog = {
	changeSet(author: "raza and mihai", id: "delete automatically-added persons") {
		grailsChange {
			change{
				List userPersons = User.list().collect { it.person }
//				List userPersons = Person.findByIdInList(userIds)
				List personsToDelete = Person.list() - userPersons
				personsToDelete.each {
					println "Deleting person ${it.userRealName}"
					it.delete()	
				}
			}
		}
	}
}
