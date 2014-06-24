import net.biomodels.jummp.core.model.identifier.generator.NullModelIdentifierGenerator

databaseChangeLog = {
    changeSet(author: "Mihai Glont", id: "populate-submission-id-20140622") {
        grailsChange {
            change {
                def sig = ctx.getBean("submissionIdGenerator")
                sql.eachRow("select * from model") { m ->
                    m.submission_id = sig.generate()
                }
            }
        }
    }
}
