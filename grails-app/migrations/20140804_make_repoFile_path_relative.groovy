databaseChangeLog = {
    changeSet(author: "Mihai Glont", id: "make-repository-file-paths-relative-20140804") {
        grailsChange {
            change {
                def theTable = sql.dataSet("repository_file")
                //quick test to see if we need to do any processing
                def firstPath = theTable.firstRow()?.path
                boolean workToBeDone = true
                if (!firstPath) {
                    workToBeDone = false
                } else {
                    boolean haveSlashes = firstPath.contains('/')
                    if (!haveSlashes) {
                        workToBeDone = false
                    }
                }
                if (workToBeDone) {
                    theTable.rows().each { rf ->
                        String currentPath = rf.path
                        final int LEN = currentPath.length()
                        final int BEGIN = currentPath.lastIndexOf('/') + 1
                        final String NEW_PATH = currentPath.substring(BEGIN, LEN)
                        sql.executeUpdate """\
update repository_file
set path = $NEW_PATH
where id = ${rf.id}"""
                    }
                }
            }
        }
    }
}
