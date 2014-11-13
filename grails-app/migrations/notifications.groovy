databaseChangeLog = {

	changeSet(author: "raza (generated)", id: "1413301526455-1") {
		createTable(tableName: "notification") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "notificationPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "body", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "from", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "notification_type", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "title", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "raza (generated)", id: "1413301526455-2") {
		createTable(tableName: "notification_type_preferences") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "notification_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "notification_type", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "send_mail", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "send_notification", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "raza (generated)", id: "1413301526455-3") {
		createTable(tableName: "notification_user") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "notification_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "notification_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "raza (generated)", id: "1413301526455-4") {
		modifyDataType(columnName: "vcs_identifier", newDataType: "varchar(255)", tableName: "model")
	}

	changeSet(author: "raza (generated)", id: "1413301526455-5") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "vcs_identifier", tableName: "model")
	}

	changeSet(author: "raza (generated)", id: "1413301526455-9") {
		createIndex(indexName: "FKCBE932472C00D884", tableName: "notification_type_preferences") {
			column(name: "user_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1413301526455-10") {
		createIndex(indexName: "FKA24EF69F1F3D24E5", tableName: "notification_user") {
			column(name: "notification_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1413301526455-11") {
		createIndex(indexName: "FKA24EF69F2C00D884", tableName: "notification_user") {
			column(name: "user_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1413301526455-12") {
		createIndex(indexName: "person_id_uniq_1413301526273", tableName: "user", unique: "true") {
			column(name: "person_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1413301526455-6") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "notification_type_preferences", constraintName: "FKCBE932472C00D884", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "raza (generated)", id: "1413301526455-7") {
		addForeignKeyConstraint(baseColumnNames: "notification_id", baseTableName: "notification_user", constraintName: "FKA24EF69F1F3D24E5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "notification", referencesUniqueColumn: "false")
	}

	changeSet(author: "raza (generated)", id: "1413301526455-8") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "notification_user", constraintName: "FKA24EF69F2C00D884", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}
}
