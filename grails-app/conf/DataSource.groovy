Properties databaseProperties = new Properties()
try {
    databaseProperties.load(new FileInputStream(System.getProperty("user.home") + "/.jummp.properties"))
    String server = databaseProperties.getProperty("jummp.database.server")
    String port = databaseProperties.getProperty("jummp.database.port")
    String database = databaseProperties.getProperty("jummp.database.database")
    databaseProperties.setProperty("jummp.database.driver", "com.mysql.jdbc.Driver")
    databaseProperties.setProperty("jummp.database.url", "jdbc:mysql://${server}:${port}/${database}")
    databaseProperties.setProperty("jummp.database.pooled", "true")
    databaseProperties.setProperty("jummp.database.dbCreate", "update")
} catch (Exception e) {
    // no database configured yet, use hsqldb
    databaseProperties.setProperty("jummp.database.driver", "org.hsqldb.jdbcDriver")
    databaseProperties.setProperty("jummp.database.username", "sa")
    databaseProperties.setProperty("jummp.database.password", "")
    databaseProperties.setProperty("jummp.database.url", "jdbc:hsqldb:mem:devDB")
    databaseProperties.setProperty("jummp.database.pooled", "false")
    databaseProperties.setProperty("jummp.database.dbCreate", "create")
}
def databaseConfig = new ConfigSlurper().parse(databaseProperties)

dataSource {
    pooled = Boolean.parseBoolean(databaseConfig.jummp.database.pooled)
    driverClassName = databaseConfig.jummp.database.driver
    username = databaseConfig.jummp.database.username
    password = databaseConfig.jummp.database.password
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = databaseConfig.jummp.database.dbCreate // one of 'create', 'create-drop','update'
            url = databaseConfig.jummp.database.url
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:hsqldb:mem:testDb"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:hsqldb:file:prodDb;shutdown=true"
        }
    }
}
