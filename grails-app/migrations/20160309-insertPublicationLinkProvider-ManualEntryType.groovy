import net.biomodels.jummp.model.PublicationLinkProvider

/**
 * Created by Tung Nguyen (tnguyen@ebi.ac.uk) on 09/03/2016.
 */
databaseChangeLog = {
    changeSet(author: "Tung Nguyen", id: "20160309-133102-1") {
        grailsChange {
            change {
                int version = 0
                String identifiers_prefix = ''
                String link_type='MANUAL ENTRY'
                String pattern = '*'
//                def pubLinkProvider = sql.executeUpdate("select * from publication_link_provider where link_type like '%MANUAL ENTRY%'")
//                if (!pubLinkProvider) {
//                    sql.execute("insert publication_link_provider(version,identifiers_prefix,link_type,pattern) values(?,?,?,?)",
//                        [version, identifiers_prefix, link_type, pattern])
//                }
                new PublicationLinkProvider(
                    version: version,
                    identifiers_prefix: identifiers_prefix,
                    link_type: link_type,
                    pattern: pattern
                ).save(flush: true)
            }
        }
    }
}
