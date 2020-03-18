//check external configuration as described in Config.groovy


dataSource_oauth2 {
    dbCreate = 'update'
    driverClassName = 'org.h2.Driver'
    formatSql = true
    logSql = true
    password = ''
    url = "jdbc:h2:~/.grails/oauth2db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE"
    username = 'sa'
}

hibernate {
	cache {
		use_query_cache = true
		use_second_level_cache = true

		// make sure hibernate.cache.provider_class is not being set
		// see http://stackoverflow.com/a/3690212/127724 and the docs for the cache-ehcache plugin
		region.factory_class = 'grails.plugin.cache.ehcache.hibernate.BeanEhcacheRegionFactory'
//		region.factory_class = 'org.hibernate.cache.SingletonEhCacheRegionFactory' // Hibernate 3
	}
//	flush.mode = 'manual'
	format_sql = true
	singleSession = true
	use_sql_comments = true
}

hibernate_oauth2 {
	cache {
		use_query_cache = false
		use_second_level_cache = false
	}
//	flush.mode = 'manual'
	format_sql = true
	singleSession = true
	use_sql_comments = true
}

environments {
	test {
		dataSource {
			dbCreate = 'update'
			driverClassName = 'org.h2.Driver'
			formatSql = true
			logSql = true
			password = ''
			url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;INIT=RUNSCRIPT FROM './h2_init.sql'"
			username = 'sa'
		}

		dataSource_oauth2 {
			dbCreate = 'update'
			driverClassName = 'org.h2.Driver'
			formatSql = true
			logSql = true
			password = ''
			url = "jdbc:h2:mem:oauth2;MVCC=TRUE"
			username = 'sa'
		}

		hibernate {
			cache {
				use_query_cache = false
				use_second_level_cache = true
			}
		}
	}
}
