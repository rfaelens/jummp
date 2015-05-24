databaseChangeLog = {
    changeSet(author: "mglont (generated)", id: "1432312720111-1") {
        addColumn(tableName: "model") {
            column(name: "first_published", type: "datetime")
        }
    }

    changeSet(author: "Mihai Glont", id: "record-date-model-was-published-20150522-1") {
        grailsChange {
            change {
                def model = sql.dataSet("model")
                def modelAudit = sql.dataSet("model_audit")
                final int AUDIT_TYPE = 4 // publishing
                model.rows().each { m ->
                    long id = m.id
                    def query = '''select min(date_created) as dateCreated
from model_audit
where success = 1
and type = :type
and model_id = :model'''
                    def params = [type: AUDIT_TYPE, model: id]
                    def publicationEvent = modelAudit.firstRow(query, params)
                    Date publicationDate = publicationEvent?.dateCreated
                    if (publicationDate) {
                        println "Model ${m.submission_id} published at $publicationDate"
                        sql.executeUpdate('''update model
set first_published = ? where id = ? limit 1''', [publicationDate, id])
                    }
                }
            }
        }
    }
}
