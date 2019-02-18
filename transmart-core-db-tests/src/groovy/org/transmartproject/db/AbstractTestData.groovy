package org.transmartproject.db

import grails.util.Holders
import org.hibernate.SessionFactory
import org.slf4j.Logger
import org.transmart.plugin.shared.UtilService

abstract class AbstractTestData {

	private SessionFactory sessionFactory = getBean('sessionFactory', SessionFactory)
	private static UtilService utilService = getBean('utilService', UtilService)

	abstract void saveAll()

	protected void flush() {
		sessionFactory.currentSession.flush()
	}

	protected static <T> T save(T t, Logger logger) {
		t.save(failOnError: true, flush: true)
		if (t.hasErrors()) {
			String message = 'Could not save ' + t + '. Errors: ' + utilService.errorStrings(t)
			logger.error message
			throw new IllegalStateException(message)
		}
		t
	}

	protected static void saveAll(List objects, Logger logger) {
		for (object in objects) {
			save object, logger
		}
	}

	protected static <T> T getBean(Class<T> clazz) {
		Holders.applicationContext.getBean(clazz)
	}

	protected static <T> T getBean(String name, Class<T> clazz) {
		Holders.applicationContext.getBean(name, clazz)
	}
}
