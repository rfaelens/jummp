databaseChangeLog = {

	changeSet(author: "mglont (generated)", id: "1393841722118-1") {
		dropIndex(indexName: "vcs_id", tableName: "revision")
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-2") {
		createIndex(indexName: "FK5302D47DB0D9DC4D", tableName: "acl_entry") {
			column(name: "acl_object_identity")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-3") {
		createIndex(indexName: "FK2A2BB00970422CC5", tableName: "acl_object_identity") {
			column(name: "object_id_class")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-4") {
		createIndex(indexName: "description_id_uniq_1393841721857", tableName: "gene_ontology", unique: "true") {
			column(name: "description_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-5") {
		createIndex(indexName: "FK9388A534ADCECDF", tableName: "model_history_item") {
			column(name: "model_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-6") {
		createIndex(indexName: "FKF074B7DBADCECDF", tableName: "revision") {
			column(name: "model_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-7") {
		createIndex(indexName: "unique_vcs_id", tableName: "revision", unique: "true") {
			column(name: "model_id")

			column(name: "vcs_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-8") {
		createIndex(indexName: "person_id_uniq_1393841721853", tableName: "user", unique: "true") {
			column(name: "person_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-9") {
		createIndex(indexName: "FK143BF46A86D614A4", tableName: "user_role") {
			column(name: "role_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-10") {
		createIndex(indexName: "FK951FF55BE1775669", tableName: "wcm_content") {
			column(name: "parent_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393841722118-11") {
		createIndex(indexName: "content_space_Idx", tableName: "wcm_content") {
			column(name: "space_id")
		}
	}
}
