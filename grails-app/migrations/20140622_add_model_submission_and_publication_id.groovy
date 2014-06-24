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
}
