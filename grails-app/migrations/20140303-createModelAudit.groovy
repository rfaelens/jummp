databaseChangeLog = {

	changeSet(author: "mglont (generated)", id: "1393857199763-1") {
		createTable(tableName: "model_audit") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "model_auditPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "changes_made", type: "longtext")

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "format", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "model_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "success", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393857199763-5") {
		createIndex(indexName: "FK309537452C00D884", tableName: "model_audit") {
			column(name: "user_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393857199763-6") {
		createIndex(indexName: "FK30953745ADCECDF", tableName: "model_audit") {
			column(name: "model_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393857199763-2") {
		addForeignKeyConstraint(baseColumnNames: "model_id", baseTableName: "model_audit", constraintName: "FK30953745ADCECDF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "model", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1393857199763-3") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "model_audit", constraintName: "FK309537452C00D884", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}
}
