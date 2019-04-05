grails.controllers.defaultScope = 'singleton'
grails.converters.encoding = 'UTF-8'
grails.enable.native2ascii = true
grails.exceptionresolver.params.exclude = ['password']
grails.hibernate.cache.queries = false
grails.hibernate.osiv.readonly = false
grails.hibernate.pass.readonly = false
grails.json.legacy.builder = false
grails.logging.jul.usebridge = true
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [
	all:           '*/*',
	atom:          'application/atom+xml',
	css:           'text/css',
	csv:           'text/csv',
	form:          'application/x-www-form-urlencoded',
	html:          ['text/html','application/xhtml+xml'],
	js:            'text/javascript',
	json:          ['application/json', 'text/json'],
	multipartForm: 'multipart/form-data',
	rss:           'application/rss+xml',
	text:          'text/plain',
	hal:           ['application/hal+json','application/hal+xml'],
	xml:           ['text/xml', 'application/xml']
]
grails.project.groupId = appName
grails.resources.adhoc.includes = ['/images/**', '/css/**', '/js/**', '/plugins/**']
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
grails.scaffolding.templates.domainSuffix = ''
grails.spring.bean.packages = []
grails.views.default.codec = 'html'
grails {
	views {
		gsp {
			encoding = 'UTF-8'
			htmlcodec = 'xml'
			codecs {
				expression = 'html'
				scriptlet = 'html'
				taglib = 'none'
				staticparts = 'none'
			}
		}
	}
}
grails.web.disable.multipart = false

log4j = {
	error 'org.codehaus.groovy.grails',
	      'org.springframework',
	      'org.hibernate',
	      'net.sf.ehcache.hibernate'

	debug 'org.hibernate.SQL'
	trace 'org.hibernate.type.descriptor.sql.BasicBinder'
}

environments {
	production {
		grails.logging.jul.usebridge = false
	}
}
