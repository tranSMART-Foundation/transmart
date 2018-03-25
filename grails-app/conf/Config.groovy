hibernate {
	cache {
		region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
		use_query_cache = true
		use_second_level_cache = true
	}
	singleSession = true
}

log4j = {
	error 'org.codehaus.groovy.grails',
	      'org.springframework',
	      'org.hibernate',
	      'net.sf.ehcache.hibernate'
}
