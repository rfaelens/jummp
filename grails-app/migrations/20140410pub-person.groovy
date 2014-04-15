databaseChangeLog = {

	changeSet(author: "raza (generated)", id: "1397229912672-3") {
		dropForeignKeyConstraint(baseTableName: "publication_person", baseTableSchemaName: "consortium", constraintName: "FKE7516CC8448831C4")
	}

	changeSet(author: "raza (generated)", id: "1397229912672-4") {
		dropForeignKeyConstraint(baseTableName: "publication_person", baseTableSchemaName: "consortium", constraintName: "FKE7516CC8A253AE96")
	}

	changeSet(author: "raza (generated)", id: "1397229912672-5") {
		createIndex(indexName: "person_id_uniq_1397229912274", tableName: "user", unique: "true") {
			column(name: "person_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1397229912672-6") {
		dropTable(tableName: "publication_person")
	}
}
