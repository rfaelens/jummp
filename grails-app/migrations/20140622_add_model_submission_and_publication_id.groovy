import net.biomodels.jummp.core.model.identifier.generator.NullModelIdentifierGenerator

databaseChangeLog = {

    changeSet(author: "mglont (generated)", id: "1403411311764-1") {
        addColumn(tableName: "model") {
            column(name: "perennialPublicationIdentifier", type: "varchar(255)")
        }
    }

    changeSet(author: "mglont (generated)", id: "1403411311764-2") {
        addColumn(tableName: "model") {
            column(name: "submission_id", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(authod: "Mihai Glont", id: "populate-submission-id-20140622") {
        grailsChange {
            change {
                def sig = ctx.getBean("submissionIdGenerator")
                def pig = ctx.getBean("publicationIdGenerator")
                final boolean HAVE_PUBLICATION_ID = !(pig instanceof NullModelIdentifierGenerator)
                sql.eachRow("select * from model") { m ->
                    m.submissionId = sig.generate()
                    m.publicationId = HAVE_PUBLICATION_ID ? pig.generate() : null
                }
            }
        }
    }
}
