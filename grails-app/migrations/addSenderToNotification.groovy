databaseChangeLog = {

	changeSet(author: "raza (generated)", id: "1413371021073-1") {
		addColumn(tableName: "notification") {
			column(name: "sender_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "raza (generated)", id: "1413371021073-3") {
		createIndex(indexName: "FK237A88EB36B119DA", tableName: "notification") {
			column(name: "sender_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1413371021073-2") {
		addForeignKeyConstraint(baseColumnNames: "sender_id", baseTableName: "notification", constraintName: "FK237A88EB36B119DA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}
}
