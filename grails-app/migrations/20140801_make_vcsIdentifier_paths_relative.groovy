databaseChangeLog = {
    changeSet(author: "Mihai Glont", id: "make-vcsIdentifier-paths-relative-20140801") {
        grailsChange {
            change {
                def modelTable = sql.dataSet("model")
                //quick test to see if we need to do any processing
                def firstModel = modelTable.firstRow()
                def firstVcsId = firstModel?.vcs_identifier
                boolean workToBeDone = true
                if (!firstVcsId) {
                    workToBeDone = false
                } else {
                    // vcsIdentifiers always have a trailing slash
                    boolean haveSlashes = firstVcsId.substring(0, firstVcsId.length() -1).
                            contains('/')
                    if (!haveSlashes) {
                        workToBeDone = false
                    }
                }
                if (workToBeDone) {
                    modelTable.rows().each { m ->
                        final String CURRENT_VCS_ID = m.vcs_identifier.substring(0,
                                m.vcs_identifier.length() -1)
                        final int BEGIN = CURRENT_VCS_ID.lastIndexOf('/') + 1
                        final String NEW_ID = CURRENT_VCS_ID.substring(BEGIN,
                                CURRENT_VCS_ID.length()) + '/'
                        sql.executeUpdate """\
update model
set vcs_identifier = $NEW_ID
where id = ${m.id}"""
                    }
                }
            }
        }
    }
}
