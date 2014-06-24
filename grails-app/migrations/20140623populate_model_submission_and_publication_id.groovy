import net.biomodels.jummp.core.model.identifier.generator.NullModelIdentifierGenerator
import net.biomodels.jummp.core.model.identifier.decorator.AbstractAppendingDecorator

databaseChangeLog = {
    changeSet(author: "Mihai Glont", id: "populate-submission-id-20140624") {
        grailsChange {
            change {
                AbstractAppendingDecorator.context = ctx
                def sig = ctx.getBean("submissionIdGenerator")
                def modelTable = sql.dataSet("model")
                modelTable.rows().each { m ->
                    String sId = sig.generate()
                    sql.executeUpdate "update model set submission_id = $sId where id = ${m.id}"
                }
            }
        }
    }
}
