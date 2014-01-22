databaseChangeLog = {

	changeSet(author: "mglont (generated)", id: "1390183642476-1") {
		createTable(tableName: "acl_class") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-2") {
		createTable(tableName: "acl_entry") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ace_order", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "acl_object_identity", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "audit_failure", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "audit_success", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "granting", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "mask", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "sid", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-3") {
		createTable(tableName: "acl_object_identity") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "object_id_class", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "entries_inheriting", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "object_id_identity", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "owner_sid", type: "BIGINT")

			column(name: "parent_object", type: "BIGINT")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-4") {
		createTable(tableName: "acl_sid") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "principal", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "sid", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-5") {
		createTable(tableName: "author") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "first_name", type: "VARCHAR(255)")

			column(name: "initials", type: "VARCHAR(255)")

			column(name: "last_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-6") {
		createTable(tableName: "gene_ontology") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "description_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-7") {
		createTable(tableName: "gene_ontology_relationship") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "from_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "to_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-8") {
		createTable(tableName: "gene_ontology_revision") {
			column(name: "gene_ontology_revisions_id", type: "BIGINT")

			column(name: "revision_id", type: "BIGINT")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-9") {
		createTable(tableName: "miriam_datatype") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "pattern", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "preferred_id", type: "BIGINT")

			column(name: "urn", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-10") {
		createTable(tableName: "miriam_identifier") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "datatype_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-11") {
		createTable(tableName: "miriam_resource") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "action", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "datatype_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "location", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "obsolete", type: "BIT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-12") {
		createTable(tableName: "model") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "deleted", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "publication_id", type: "BIGINT")

			column(name: "vcs_identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-13") {
		createTable(tableName: "model_format") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "format_version", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-14") {
		createTable(tableName: "model_history_item") {
			column(name: "model_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "last_accessed_date", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-15") {
		createTable(tableName: "publication") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "affiliation", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "day", type: "INT")

			column(name: "issue", type: "INT")

			column(name: "journal", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "link", type: "VARCHAR(255)")

			column(name: "link_provider_id", type: "BIGINT")

			column(name: "month", type: "VARCHAR(255)")

			column(name: "pages", type: "VARCHAR(255)")

			column(name: "synopsis", type: "VARCHAR(5000)") {
				constraints(nullable: "false")
			}

			column(name: "title", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "volume", type: "INT")

			column(name: "year", type: "INT")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-16") {
		createTable(tableName: "publication_author") {
			column(name: "publication_authors_id", type: "BIGINT")

			column(name: "author_id", type: "BIGINT")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-17") {
		createTable(tableName: "publication_link_provider") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "identifiers_prefix", type: "VARCHAR(255)")

			column(name: "link_type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "pattern", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-18") {
		createTable(tableName: "repository_file") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "VARCHAR(500)")

			column(name: "hidden", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "main_file", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "mime_type", type: "VARCHAR(255)")

			column(name: "path", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "revision_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "user_submitted", type: "BIT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-19") {
		createTable(tableName: "revision") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "comment", type: "VARCHAR(1000)")

			column(name: "deleted", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "LONGTEXT")

			column(name: "format_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "minor_revision", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "model_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)")

			column(name: "owner_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "revision_number", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "state", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "upload_date", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "validated", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "vcs_id", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-20") {
		createTable(tableName: "role") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "authority", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-21") {
		createTable(tableName: "tag_links") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tag_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tag_ref", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-22") {
		createTable(tableName: "tags") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-23") {
		createTable(tableName: "user") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "account_expired", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "account_locked", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "email", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "enabled", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "institution", type: "VARCHAR(255)")

			column(name: "orcid", type: "VARCHAR(255)")

			column(name: "password", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "password_expired", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "password_forgotten_code", type: "VARCHAR(255)")

			column(name: "password_forgotten_invalidation", type: "DATETIME")

			column(name: "registration_code", type: "VARCHAR(255)")

			column(name: "registration_invalidation", type: "DATETIME")

			column(name: "user_real_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-24") {
		createTable(tableName: "user_role") {
			column(name: "role_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-25") {
		createTable(tableName: "wcm_content") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "aliasuri", type: "VARCHAR(50)") {
				constraints(nullable: "false")
			}

			column(name: "changed_by", type: "VARCHAR(255)")

			column(name: "changed_on", type: "DATETIME")

			column(name: "content_dependencies", type: "VARCHAR(500)")

			column(name: "created_by", type: "VARCHAR(255)")

			column(name: "created_on", type: "DATETIME")

			column(name: "description", type: "VARCHAR(500)")

			column(name: "identifier", type: "VARCHAR(80)")

			column(name: "language", type: "VARCHAR(3)")

			column(name: "meta_copyright", type: "VARCHAR(200)")

			column(name: "meta_creator", type: "VARCHAR(80)")

			column(name: "meta_publisher", type: "VARCHAR(80)")

			column(name: "meta_source", type: "VARCHAR(80)")

			column(name: "order_index", type: "INT")

			column(name: "parent_id", type: "BIGINT")

			column(name: "publish_from", type: "DATETIME")

			column(name: "publish_until", type: "DATETIME")

			column(name: "space_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "status_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "title", type: "VARCHAR(100)") {
				constraints(nullable: "false")
			}

			column(name: "valid_for", type: "INT")

			column(name: "class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "color", type: "INT")

			column(name: "content", type: "LONGTEXT")

			column(name: "height", type: "INT")

			column(name: "width", type: "INT")

			column(name: "target_id", type: "BIGINT")

			column(name: "user_specific_content", type: "BIT")

			column(name: "file_mime_type", type: "VARCHAR(255)")

			column(name: "file_size", type: "BIGINT")

			column(name: "sync_status", type: "SMALLINT")

			column(name: "files_count", type: "INT")

			column(name: "keywords", type: "VARCHAR(255)")

			column(name: "summary", type: "VARCHAR(500)")

			column(name: "author", type: "VARCHAR(80)")

			column(name: "email", type: "VARCHAR(80)")

			column(name: "ip_address", type: "VARCHAR(50)")

			column(name: "website_url", type: "VARCHAR(100)")

			column(name: "allowgsp", type: "BIT")

			column(name: "html_title", type: "LONGTEXT")

			column(name: "menu_title", type: "VARCHAR(40)")

			column(name: "template_id", type: "BIGINT")

			column(name: "comment_markup", type: "VARCHAR(4)")

			column(name: "max_entries_to_display", type: "INT")

			column(name: "allowed_methods", type: "VARCHAR(40)")

			column(name: "script_id", type: "BIGINT")

			column(name: "url", type: "VARCHAR(1000)")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-26") {
		createTable(tableName: "wcm_content_version") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "content_title", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "VARCHAR(255)")

			column(name: "created_on", type: "DATETIME")

			column(name: "object_class_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "object_content", type: "LONGTEXT") {
				constraints(nullable: "false")
			}

			column(name: "object_key", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "revision", type: "INT")

			column(name: "space_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-27") {
		createTable(tableName: "wcm_related_content") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "is_internal", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "last_checked_on", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "relation_type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "source_content_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "status", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "target_content_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-28") {
		createTable(tableName: "wcm_space") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "aliasuri", type: "VARCHAR(80)")

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-29") {
		createTable(tableName: "wcm_status") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "code", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "VARCHAR(80)") {
				constraints(nullable: "false")
			}

			column(name: "public_content", type: "BIT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-30") {
		addPrimaryKey(columnNames: "model_id, user_id", tableName: "model_history_item")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-31") {
		addPrimaryKey(columnNames: "role_id, user_id", tableName: "user_role")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-66") {
		createIndex(indexName: "class", tableName: "acl_class", unique: "true") {
			column(name: "class")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-67") {
		createIndex(indexName: "acl_object_identity", tableName: "acl_entry", unique: "true") {
			column(name: "acl_object_identity")

			column(name: "ace_order")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-68") {
		createIndex(indexName: "object_id_class", tableName: "acl_object_identity", unique: "true") {
			column(name: "object_id_class")

			column(name: "object_id_identity")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-69") {
		createIndex(indexName: "sid", tableName: "acl_sid", unique: "true") {
			column(name: "sid")

			column(name: "principal")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-70") {
		createIndex(indexName: "identifier", tableName: "miriam_datatype", unique: "true") {
			column(name: "identifier")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-71") {
		createIndex(indexName: "identifier", tableName: "miriam_identifier", unique: "true") {
			column(name: "identifier")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-72") {
		createIndex(indexName: "identifier", tableName: "miriam_resource", unique: "true") {
			column(name: "identifier")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-73") {
		createIndex(indexName: "vcs_identifier", tableName: "model", unique: "true") {
			column(name: "vcs_identifier")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-74") {
		createIndex(indexName: "identifier", tableName: "model_format", unique: "true") {
			column(name: "identifier")

			column(name: "format_version")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-75") {
		createIndex(indexName: "model_id", tableName: "revision", unique: "true") {
			column(name: "model_id")

			column(name: "revision_number")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-76") {
		createIndex(indexName: "vcs_id", tableName: "revision", unique: "true") {
			column(name: "vcs_id")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-77") {
		createIndex(indexName: "authority", tableName: "role", unique: "true") {
			column(name: "authority")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-78") {
		createIndex(indexName: "name", tableName: "tags", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-79") {
		createIndex(indexName: "email", tableName: "user", unique: "true") {
			column(name: "email")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-80") {
		createIndex(indexName: "username", tableName: "user", unique: "true") {
			column(name: "username")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-81") {
		createIndex(indexName: "content_aliasURI_Idx", tableName: "wcm_content", unique: "false") {
			column(name: "aliasuri")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-82") {
		createIndex(indexName: "content_changedOn_Idx", tableName: "wcm_content", unique: "false") {
			column(name: "changed_on")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-83") {
		createIndex(indexName: "content_contentName_Idx", tableName: "wcm_content", unique: "false") {
			column(name: "title")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-84") {
		createIndex(indexName: "content_createdOn_Idx", tableName: "wcm_content", unique: "false") {
			column(name: "created_on")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-85") {
		createIndex(indexName: "parent_id", tableName: "wcm_content", unique: "true") {
			column(name: "parent_id")

			column(name: "space_id")

			column(name: "aliasuri")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-86") {
		createIndex(indexName: "aliasuri", tableName: "wcm_space", unique: "true") {
			column(name: "aliasuri")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-87") {
		createIndex(indexName: "name", tableName: "wcm_space", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-88") {
		createIndex(indexName: "space_aliasURI_Idx", tableName: "wcm_space", unique: "false") {
			column(name: "aliasuri")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-89") {
		createIndex(indexName: "space_name_Idx", tableName: "wcm_space", unique: "false") {
			column(name: "name")
		}
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-32") {
		addForeignKeyConstraint(baseColumnNames: "acl_object_identity", baseTableName: "acl_entry", baseTableSchemaName: "jummp", constraintName: "FK5302D47D8B9F71F2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "acl_object_identity", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-33") {
		addForeignKeyConstraint(baseColumnNames: "sid", baseTableName: "acl_entry", baseTableSchemaName: "jummp", constraintName: "FK5302D47D5B1B0850", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "acl_sid", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-34") {
		addForeignKeyConstraint(baseColumnNames: "object_id_class", baseTableName: "acl_object_identity", baseTableSchemaName: "jummp", constraintName: "FK2A2BB009699FB980", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "acl_class", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-35") {
		addForeignKeyConstraint(baseColumnNames: "owner_sid", baseTableName: "acl_object_identity", baseTableSchemaName: "jummp", constraintName: "FK2A2BB0095C2B98C4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "acl_sid", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-36") {
		addForeignKeyConstraint(baseColumnNames: "parent_object", baseTableName: "acl_object_identity", baseTableSchemaName: "jummp", constraintName: "FK2A2BB0097FC8265D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "acl_object_identity", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-37") {
		addForeignKeyConstraint(baseColumnNames: "description_id", baseTableName: "gene_ontology", baseTableSchemaName: "jummp", constraintName: "FKF242873932FC8D7C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "miriam_identifier", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-38") {
		addForeignKeyConstraint(baseColumnNames: "from_id", baseTableName: "gene_ontology_relationship", baseTableSchemaName: "jummp", constraintName: "FKFBB69D7E2DF0334A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gene_ontology", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-39") {
		addForeignKeyConstraint(baseColumnNames: "to_id", baseTableName: "gene_ontology_relationship", baseTableSchemaName: "jummp", constraintName: "FKFBB69D7E57F37ED9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gene_ontology", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-40") {
		addForeignKeyConstraint(baseColumnNames: "gene_ontology_revisions_id", baseTableName: "gene_ontology_revision", baseTableSchemaName: "jummp", constraintName: "FK8CD9722112342382", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gene_ontology", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-41") {
		addForeignKeyConstraint(baseColumnNames: "revision_id", baseTableName: "gene_ontology_revision", baseTableSchemaName: "jummp", constraintName: "FK8CD97221265115F5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "revision", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-42") {
		addForeignKeyConstraint(baseColumnNames: "preferred_id", baseTableName: "miriam_datatype", baseTableSchemaName: "jummp", constraintName: "FK229D59E41FE7B37C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "miriam_resource", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-43") {
		addForeignKeyConstraint(baseColumnNames: "datatype_id", baseTableName: "miriam_identifier", baseTableSchemaName: "jummp", constraintName: "FKB846CA93E20DD0F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "miriam_datatype", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-44") {
		addForeignKeyConstraint(baseColumnNames: "datatype_id", baseTableName: "miriam_resource", baseTableSchemaName: "jummp", constraintName: "FKA397840E3E20DD0F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "miriam_datatype", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-45") {
		addForeignKeyConstraint(baseColumnNames: "publication_id", baseTableName: "model", baseTableSchemaName: "jummp", constraintName: "FK633FB297C6F423F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "publication", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-46") {
		addForeignKeyConstraint(baseColumnNames: "model_id", baseTableName: "model_history_item", baseTableSchemaName: "jummp", constraintName: "FK9388A534ADCECDF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "model", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-47") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "model_history_item", baseTableSchemaName: "jummp", constraintName: "FK9388A5342C00D884", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-48") {
		addForeignKeyConstraint(baseColumnNames: "link_provider_id", baseTableName: "publication", baseTableSchemaName: "jummp", constraintName: "FKBFBBA22C46318640", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "publication_link_provider", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-49") {
		addForeignKeyConstraint(baseColumnNames: "author_id", baseTableName: "publication_author", baseTableSchemaName: "jummp", constraintName: "FKCE9AF2FEC6942B5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "author", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-50") {
		addForeignKeyConstraint(baseColumnNames: "publication_authors_id", baseTableName: "publication_author", baseTableSchemaName: "jummp", constraintName: "FKCE9AF2FEA253AE96", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "publication", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-51") {
		addForeignKeyConstraint(baseColumnNames: "revision_id", baseTableName: "repository_file", baseTableSchemaName: "jummp", constraintName: "FK807B5151265115F5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "revision", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-52") {
		addForeignKeyConstraint(baseColumnNames: "format_id", baseTableName: "revision", baseTableSchemaName: "jummp", constraintName: "FKF074B7DB33F8EE8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "model_format", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-53") {
		addForeignKeyConstraint(baseColumnNames: "model_id", baseTableName: "revision", baseTableSchemaName: "jummp", constraintName: "FKF074B7DBADCECDF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "model", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-54") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "revision", baseTableSchemaName: "jummp", constraintName: "FKF074B7DB97E7889C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-55") {
		addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "tag_links", baseTableSchemaName: "jummp", constraintName: "FK7C35D6D45A3B441D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "tags", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-56") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", baseTableSchemaName: "jummp", constraintName: "FK143BF46A86D614A4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-57") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", baseTableSchemaName: "jummp", constraintName: "FK143BF46A2C00D884", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-58") {
		addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "wcm_content", baseTableSchemaName: "jummp", constraintName: "FK951FF55BE1775669", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "wcm_content", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-59") {
		addForeignKeyConstraint(baseColumnNames: "script_id", baseTableName: "wcm_content", baseTableSchemaName: "jummp", constraintName: "FK951FF55B3A4CE4A4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "wcm_content", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-60") {
		addForeignKeyConstraint(baseColumnNames: "space_id", baseTableName: "wcm_content", baseTableSchemaName: "jummp", constraintName: "FK951FF55BF1D5C51A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "wcm_space", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-61") {
		addForeignKeyConstraint(baseColumnNames: "status_id", baseTableName: "wcm_content", baseTableSchemaName: "jummp", constraintName: "FK951FF55B68CFF5A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "wcm_status", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-62") {
		addForeignKeyConstraint(baseColumnNames: "target_id", baseTableName: "wcm_content", baseTableSchemaName: "jummp", constraintName: "FK951FF55B3573F022", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "wcm_content", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-63") {
		addForeignKeyConstraint(baseColumnNames: "template_id", baseTableName: "wcm_content", baseTableSchemaName: "jummp", constraintName: "FK951FF55B988A1A5A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "wcm_content", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-64") {
		addForeignKeyConstraint(baseColumnNames: "source_content_id", baseTableName: "wcm_related_content", baseTableSchemaName: "jummp", constraintName: "FKDE7BDB47AD0EC2DE", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "wcm_content", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1390183642476-65") {
		addForeignKeyConstraint(baseColumnNames: "target_content_id", baseTableName: "wcm_related_content", baseTableSchemaName: "jummp", constraintName: "FKDE7BDB47361353A8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "wcm_content", referencedTableSchemaName: "jummp", referencesUniqueColumn: "false")
	}
}
