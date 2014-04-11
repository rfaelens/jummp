import net.biomodels.jummp.plugins.security.Person
import net.biomodels.jummp.model.Publication
import net.biomodels.jummp.model.PublicationLinkProvider
import net.biomodels.jummp.model.PublicationPerson as PubPerson

databaseChangeLog = {

	changeSet(author: "raza (generated)", id: "1397230578681-1") {
		createTable(tableName: "publication_person") {
			column(name: "publication_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "person_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "position", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "pub_alias", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "raza (generated)", id: "1397230578681-4") {
		addPrimaryKey(columnNames: "publication_id, person_id", constraintName: "publication_pPK", tableName: "publication_person")
	}

	changeSet(author: "raza (generated)", id: "1397230578681-7") {
		createIndex(indexName: "FKE7516CC8448831C4", tableName: "publication_person") {
			column(name: "person_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1397230578681-8") {
		createIndex(indexName: "FKE7516CC87C6F423F", tableName: "publication_person") {
			column(name: "publication_id")
		}
	}

	changeSet(author: "raza (generated)", id: "1397230578681-5") {
		addForeignKeyConstraint(baseColumnNames: "person_id", baseTableName: "publication_person", constraintName: "FKE7516CC8448831C4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "person", referencesUniqueColumn: "false")
	}

	changeSet(author: "raza (generated)", id: "1397230578681-6") {
		addForeignKeyConstraint(baseColumnNames: "publication_id", baseTableName: "publication_person", constraintName: "FKE7516CC87C6F423F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "publication", referencesUniqueColumn: "false")
	}
	
	changeSet(author: "raza and mihai", id: "delete persons 1") {
		grailsChange {
			change{
				String query="delete from person where not exists (select * from user where person_id=person.id)";
				sql.execute(query);
			}
		}
	}
	changeSet(author: "raza and mihai", id: "import pubmed authors 2") {
		grailsChange {
			change{
				def pubs=Publication.list();
				def pubmedSource=[];
				pubs.each { publication ->
					if (publication.linkProvider.linkType == PublicationLinkProvider
																	.LinkType
																	.PUBMED) {
						pubmedSource.add(publication)
						url = new URL("http://www.ebi.ac.uk/europepmc/webservices/rest/search/query=ext_id:${publication.link}%20src:med&resulttype=core")
						try {
							def slurper = new XmlSlurper().parse(url.openStream())
							int counter=0;
							for (def authorXml in slurper.resultList.result.authorList.author) {
							 	 String alias = authorXml.fullName[0].text()
							 	 String orcid = null
							 	 Person person;
							 	 if (authorXml.authorId[0]?.@type=="ORCID") {
							 	 	 	orcid=authorXml.authorId[0].text()
							 	 	 	def candidate = Person.findByOrcid(orcid)
							 	 	 	if (candidate) {
							 	 	 		person=candidate;
							 	 		}	 	 
							 	 }
							 	 if (!person) {
							 	 	 person = new Person(userRealName: alias)
							 	 	 person.save();
							 	 }
							 	 PubPerson pubPerson=new PubPerson( person: person,
							 	 									pubAlias: alias, 
							 	 									position: counter++, 
							 	 									publication: publication );
							 	 pubPerson.save();
							}
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}	
	
}
