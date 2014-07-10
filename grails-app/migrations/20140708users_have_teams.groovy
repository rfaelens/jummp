databaseChangeLog = {

	changeSet(author: "mglont (generated)", id: "1404931436939-1") {
		createTable(tableName: "team") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "teamPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(255)")

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "owner_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-2") {
		createTable(tableName: "user_team") {
			column(name: "team_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-3") {
		addPrimaryKey(columnNames: "team_id, user_id", constraintName: "user_teamPK", tableName: "user_team")
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-7") {
		createIndex(indexName: "FK36425D97E7889C", tableName: "team") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-8") {
		createIndex(indexName: "unique_name", tableName: "team", unique: "true") {
			column(name: "owner_id")

			column(name: "name")
		}
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-9") {
		createIndex(indexName: "FK143CB6512C00D884", tableName: "user_team") {
			column(name: "user_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-10") {
		createIndex(indexName: "FK143CB651DEFB7744", tableName: "user_team") {
			column(name: "team_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-4") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "team", constraintName: "FK36425D97E7889C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-5") {
		addForeignKeyConstraint(baseColumnNames: "team_id", baseTableName: "user_team", constraintName: "FK143CB651DEFB7744", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "team", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1404931436939-6") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_team", constraintName: "FK143CB6512C00D884", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}
}
