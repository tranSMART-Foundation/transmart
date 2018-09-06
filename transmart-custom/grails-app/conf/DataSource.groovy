dataSource {
	dbCreate = 'update'
	driverClassName = 'org.h2.Driver'
	jmxExport = true
	password = ''
	pooled = true
	url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;INIT=RUNSCRIPT FROM 'test/integration/h2_init.sql'"
	username = 'sa'
}

hibernate {
	cache {
		region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
		use_query_cache = false
		use_second_level_cache = true
	}
	format_sql = true
	singleSession = true
	use_sql_comments = true
}
