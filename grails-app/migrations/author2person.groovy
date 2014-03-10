databaseChangeLog = {

	changeSet(author: "mglont (generated)", id: "1393423756842-1") {
		createTable(tableName: "person") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "institution", type: "VARCHAR(255)")

			column(name: "orcid", type: "VARCHAR(255)")

			column(name: "user_real_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

    changeSet(author: "Mihai Glont", id: "make-authors-and-users-persons-20140223-1") {
        grailsChange {
            change {
                def author = sql.dataSet("author")
                def user = sql.dataSet("user")
                def person = sql.dataSet("person")
                author.rows().each { a ->
                    final String fName = a.first_name
                    final String initials = a.initials
                    final String lName = a.last_name
                    StringBuffer realName = new StringBuffer()
                    if (fName) {
                        realName.append(fName).append(" ")
                    } else if (initials) {
                        realName.append(initials).append(" ")
                    }
                    realName.append(lName)
                    try {
                        person.add(version: 0, user_real_name: realName.toString())
                    } catch(java.sql.SQLException e) {
                        println "Migration of 'author'.${a.inspect()} to 'person'.$realName failed: ${e.message}"
                    }
                }
                user.rows().each { u ->
                    final String INST = u.institution
                    final String ORCID = u.orcid
                    final String NAME = u.user_real_name
                    try {
                        person.add(version:0, institution: INST, orcid: ORCID, user_real_name: NAME)
                    } catch(java.sql.SQLException e) {
                        println "Migration of 'user'.${u.inspect()} failed: ${e.message}"
                    }
                }
            }
        }
    }

	changeSet(author: "mglont (generated)", id: "1392928519622-2") {
		createTable(tableName: "publication_person") {
			column(name: "publication_authors_id", type: "BIGINT")

			column(name: "person_id", type: "BIGINT")
		}
	}

    changeSet(author: "Mihai Glont", id: "publication_author-becomes-publication_person-20140223-1") {
        grailsChange {
            change {
                def pubAuth = sql.dataSet("publication_author")
                def pubPers = sql.dataSet("publication_person")
                try {
                    pubAuth.rows().each { p ->
                        pubPers.add(publication_authors_id: p.publication_authors_id, person_id: p.author_id)
                    }
                } catch(java.sql.SQLException e) {
                    println "Migration of 'publication_author'.${p.inspect()} failed: ${e.message}"
                }
            }
        }
    }

	changeSet(author: "mglont (generated)", id: "1392928519622-3") {
		addColumn(tableName: "user") {
			column(name: "person_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

    changeSet(author: "Mihai Glont", id: "add-person_id-to-users-20140223-1") {
        grailsChange {
            change {
                sql.eachRow("select * from user") { u ->
                    final String INST = u.institution
                    final String ORCID = u.orcid
                    final String NAME = u.user_real_name
                    def params = [NAME] //must have at least the name
                    StringBuilder b = new StringBuilder()
                    b.append("select * from person where user_real_name = ?")
                    if (ORCID) {
                        b.append(" and orcid = ?")
                        params.add(ORCID)
                    }
                    if (INST) {
                        b.append(" and institution = ?")
                        params.add(INST)
                    }
                    def person = sql.firstRow(b.toString(), params)
                    if (person) {
                        sql.executeUpdate("update user set person_id = ? where id = ? limit 1",
                                [person.id, u.id])
                    }
                }
            }
        }
    }

	changeSet(author: "mglont (generated)", id: "1392928519622-4") {
		addNotNullConstraint(columnDataType: "VARCHAR(80)", columnName: "aliasuri", tableName: "wcm_space")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-5") {
		dropForeignKeyConstraint(baseTableName: "publication_author", constraintName: "FKCE9AF2FEC6942B5")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-6") {
		dropForeignKeyConstraint(baseTableName: "publication_author", constraintName: "FKCE9AF2FEA253AE96")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-11") {
		createIndex(indexName: "orcid", tableName: "person", unique: "true") {
			column(name: "orcid")
		}
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-13") {
		dropColumn(columnName: "institution", tableName: "user")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-14") {
		dropColumn(columnName: "orcid", tableName: "user")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-15") {
		dropColumn(columnName: "user_real_name", tableName: "user")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-16") {
		dropTable(tableName: "author")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-17") {
		dropTable(tableName: "publication_author")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-7") {
		addForeignKeyConstraint(baseColumnNames: "person_id", baseTableName: "publication_person", constraintName: "FKE7516CC8448831C4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-8") {
		addForeignKeyConstraint(baseColumnNames: "publication_authors_id", baseTableName: "publication_person", constraintName: "FKE7516CC8A253AE96", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "publication", referencesUniqueColumn: "false")
	}

	changeSet(author: "mglont (generated)", id: "1393423756842-9") {
		addForeignKeyConstraint(baseColumnNames: "person_id", baseTableName: "user", constraintName: "FK36EBCB448831C4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", referencesUniqueColumn: "false")
	}
}
