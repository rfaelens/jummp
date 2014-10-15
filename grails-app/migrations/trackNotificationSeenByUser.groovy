databaseChangeLog = {

	changeSet(author: "raza (generated)", id: "1413393516794-2") {
		addColumn(tableName: "notification_user") {
			column(name: "notification_seen", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
}
