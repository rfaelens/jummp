databaseChangeLog = {

	changeSet(author: "raza (generated)", id: "1397055073770-1") {
		createTable(tableName: "alias") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "aliasPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "raza (generated)", id: "1397055073770-2") {
		createTable(tableName: "person_alias") {
			column(name: "person_aliases_id", type: "bigint")

			column(name: "alias_id", type: "bigint")
		}
	}

	changeSet(author: "raza (generated)", id: "1397055073770-3") {
		createTable(tableName: "publication_alias") {
			column(name: "publication_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "alias_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "author_position", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "raza (generated)", id: "1397055073770-6") {
		addPrimaryKey(columnNames: "publication_id, alias_id", constraintName: "publication_aPK", tableName: "publication_alias")
	}

	changeSet(author: "raza (generated)", id: "1397055073770-7") {
		dropForeignKeyConstraint(baseTableName: "publication_person", constraintName: "FKE7516CC8448831C4")
	}

	changeSet(author: "raza (generated)", id: "1397055073770-8") {
		dropForeignKeyConstraint(baseTableName: "publication_person", constraintName: "FKE7516CC8A253AE96")
	}

	changeSet(author: "raza (generated)", id: "1397055073770-13") {
		createIndex(indexName: "FKEC9B00E65C3234A5", tableName: "person_alias") {
			column(name: "person_aliases_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1397055073770-14") {
		createIndex(indexName: "FKEC9B00E6C906E650", tableName: "person_alias") {
			column(name: "alias_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1397055073770-15") {
		createIndex(indexName: "FK38324A7D7C6F423F", tableName: "publication_alias") {
			column(name: "publication_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1397055073770-16") {
		createIndex(indexName: "FK38324A7DC906E650", tableName: "publication_alias") {
			column(name: "alias_id")
		}
	}
	
	changeSet(author: "raza (generated)", id: "1397055073770-17") {
		createIndex(indexName: "person_id_uniq_1397055073568", tableName: "user", unique: "true") {
			column(name: "person_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1397055073770-18") {
		dropTable(tableName: "publication_person")
	}


	changeSet(author: "raza (generated)", id: "1397055073770-9") {
		addForeignKeyConstraint(baseColumnNames: "alias_id", baseTableName: "person_alias", constraintName: "FKEC9B00E6C906E650", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "alias", referencesUniqueColumn: "false")
	}

	changeSet(author: "raza (generated)", id: "1397055073770-10") {
		addForeignKeyConstraint(baseColumnNames: "person_aliases_id", baseTableName: "person_alias", constraintName: "FKEC9B00E65C3234A5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "person", referencesUniqueColumn: "false")
	}

	changeSet(author: "raza (generated)", id: "1397055073770-11") {
		addForeignKeyConstraint(baseColumnNames: "alias_id", baseTableName: "publication_alias", constraintName: "FK38324A7DC906E650", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "alias", referencesUniqueColumn: "false")
	}

	changeSet(author: "raza (generated)", id: "1397055073770-12") {
		addForeignKeyConstraint(baseColumnNames: "publication_id", baseTableName: "publication_alias", constraintName: "FK38324A7D7C6F423F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "publication", referencesUniqueColumn: "false")
	}
}
