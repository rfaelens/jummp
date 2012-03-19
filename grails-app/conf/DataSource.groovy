Properties databaseProperties = new Properties()
try {
    databaseProperties.load(new FileInputStream(System.getProperty("user.home") + "/.jummp.properties"))
    String server = databaseProperties.getProperty("jummp.database.server")
    String port = databaseProperties.getProperty("jummp.database.port")
    String database = databaseProperties.getProperty("jummp.database.database")
    String protocol = "mysql"
    switch (databaseProperties.getProperty("jummp.database.type")) {
    case "POSTGRESQL":
        protocol = "postgresql"
        databaseProperties.setProperty("jummp.database.driver", "org.postgresql.Driver")
        databaseProperties.setProperty("jummp.database.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        break
    case "MYSQL": // mysql is our default
    default:
        databaseProperties.setProperty("jummp.database.driver", "com.mysql.jdbc.Driver")
        databaseProperties.setProperty("jummp.database.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect")
        break
    }
    databaseProperties.setProperty("jummp.database.url", "jdbc:${protocol}://${server}:${port}/${database}")
    databaseProperties.setProperty("jummp.database.pooled", "true")
    databaseProperties.setProperty("jummp.database.dbCreate", "update")
    environments {
        test {
            throw new Exception("Test system")
        }
    }
} catch (Exception e) {
    // no database configured yet, use hsqldb
    databaseProperties.setProperty("jummp.database.driver", "org.hsqldb.jdbcDriver")
    databaseProperties.setProperty("jummp.database.username", "sa")
    databaseProperties.setProperty("jummp.database.password", "")
    databaseProperties.setProperty("jummp.database.url", "jdbc:hsqldb:mem:devDb")
    databaseProperties.setProperty("jummp.database.pooled", "false")
    databaseProperties.setProperty("jummp.database.dbCreate", "create")
    databaseProperties.setProperty("jummp.database.dialect", "org.hibernate.dialect.HSQLDialect")
}
def databaseConfig = new ConfigSlurper().parse(databaseProperties)

dataSource {
    pooled = Boolean.parseBoolean(databaseConfig.jummp.database.pooled)
    driverClassName = databaseConfig.jummp.database.driver
    username = databaseConfig.jummp.database.username
    password = databaseConfig.jummp.database.password
    dialect  = databaseConfig.jummp.database.dialect
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
        hibernate {
            cache.use_second_level_cache = false
            cache.use_query_cache = false
        }
        dataSource {
            dbCreate = "update"
            url = "jdbc:hsqldb:mem:testDb"
            dialect = ""
            driverClassName = "org.hsqldb.jdbcDriver"
        }
    }
    production {
        dataSource {
            dbCreate = databaseConfig.jummp.database.dbCreate
            url = databaseConfig.jummp.database.url
        }
    }
}
