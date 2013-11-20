/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/





Properties databaseProperties = new Properties()
try {
	def service = new net.biomodels.jummp.plugins.configuration.ConfigurationService()
    String pathToConfig=service.getConfigFilePath()
    if (!pathToConfig) {
    	throw new Exception("No config file available, using defaults")
    }
    databaseProperties.load(new FileInputStream(pathToConfig))
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
    def databaseConfig = new ConfigSlurper().parse(databaseProperties)

	dataSource {
		pooled = Boolean.parseBoolean(databaseConfig.jummp.database.pooled)
		driverClassName = databaseConfig.jummp.database.driver
		username = databaseConfig.jummp.database.username
		password = databaseConfig.jummp.database.password
		dialect  = databaseConfig.jummp.database.dialect
		properties 
				{ 
					maxActive = 50 
					maxIdle = 25 
					minIdle =1 
					initialSize = 1 
					minEvictableIdleTimeMillis = 60000 
					timeBetweenEvictionRunsMillis = 60000 
					numTestsPerEvictionRun = 3 
					maxWait = 10000 
	
					testOnBorrow = true 
					testWhileIdle = true 
					testOnReturn = false 
	
					validationQuery = "SELECT 1" 
		} 
	}
	hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
    //cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
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
	
} catch (Exception e) {
    // no database configured yet, use hsqldb
	hibernate {
		cache.use_second_level_cache = false
		cache.use_query_cache = false
	}
	dataSource {
		dbCreate = "update"
		url = "jdbc:hsqldb:mem:tempDb"
		dialect = ""
		driverClassName = "org.hsqldb.jdbcDriver"
	}
}


