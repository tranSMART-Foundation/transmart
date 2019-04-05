package org.transmartproject.security

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class BruteForceLoginLockServiceSpec extends Specification {

	private BruteForceLoginLockService service = mockService(BruteForceLoginLockService)

	void 'test lock after allowed number of attempts'() {
		expect:
		!service.isLocked('test')

		when:
		service.failLogin 'test'

		then:
		!service.isLocked('test')

		when:
		service.failLogin 'test'

		then:
		service.isLocked 'test'
	}

	void 'test successful trial does not unlock'() {
		when:
		service.allowedNumberOfAttempts = 1

		service.failLogin 'test'

		then:
		service.isLocked 'test'

		when:
		service.loginSuccess 'test'

		then:
		service.isLocked 'test'
	}

	void 'test successful trial removes bad trials count'() {
		expect:
		2 == service.remainedAttempts('test')

		when:
		service.failLogin 'test'

		then:
		1 == service.remainedAttempts('test')

		when:
		service.loginSuccess 'test'

		then:
		2 == service.remainedAttempts('test')
	}

	void 'test time removes bad trials count'() {
		when:
		service.allowedNumberOfAttempts = 1
		//0 - expires immediately
		service.@failedAttempts = CacheBuilder.newBuilder()
				.expireAfterWrite(0, TimeUnit.MINUTES)
				.build({ 0 } as CacheLoader)

		service.failLogin 'test'

		then:
		!service.isLocked('test')
	}

	void 'test invalid allowed number of attempts setting'() {
		when:
		service.allowedNumberOfAttempts = -1
		service.afterPropertiesSet()

		then:
		thrown IllegalArgumentException
	}

	void 'test invalid lock time in minutes setting'() {
		when:
		service.lockTimeInMinutes = -1
		service.afterPropertiesSet()

		then:
		thrown IllegalArgumentException
	}

	// override the mixin method to set required properties
	def <T> T mockService(Class<T> serviceClass) {
		BruteForceLoginLockService service = new BruteForceLoginLockService(
				allowedNumberOfAttempts: 2,
				lockTimeInMinutes: 10)
		service.afterPropertiesSet()
		service
	}
}
