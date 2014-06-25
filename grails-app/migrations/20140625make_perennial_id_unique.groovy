databaseChangeLog = {

	changeSet(author: "mglont (generated)", id: "1403699898993-1") {
		createIndex(indexName: "submission_id_uniq_1403699898132", tableName: "model", unique: "true") {
			column(name: "submission_id")
		}
	}
}
