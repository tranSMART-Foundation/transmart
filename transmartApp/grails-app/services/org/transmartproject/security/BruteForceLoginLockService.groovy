package org.transmartproject.security

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.InitializingBean
import org.springframework.util.Assert

import javax.annotation.PostConstruct

import static java.util.concurrent.TimeUnit.MINUTES

/**
 * Counts failed login attempts per account.
 * It has functionality for checking when given account should be locked and on how long.
 *
 * idea taken from
 * http://www.grygoriy.com/blog/2012/10/06/prevent-brute-force-attack-with-spring-security/
 */
@Slf4j('logger')
class BruteForceLoginLockService implements InitializingBean {

    static transactional = false

    private Cache<String, Integer> failedAttempts

    int allowedNumberOfAttempts
    int lockTimeInMinutes

    /**
     * Triggers on each unsuccessful login attempt and increases number of failedAttempts in local accumulator
     * @param login - username which is trying to login
     */
    void failLogin(String login) {
	int numberOfAttempts = failedAttempts.get(login)
	logger.debug 'fail login {} previous number for failedAttempts $numberOfAttempts', login
	failedAttempts.put login, numberOfAttempts + 1
    }

    /**
     * Triggers on each successful login attempt and resets number of failedAttempts in local accumulator
     * @param login - username which is login
     */
    void loginSuccess(String login) {
        if (!isLocked(login)) {
	    logger.debug 'successful login for {}', login
	    failedAttempts.invalidate login
        }
    }

    boolean isLocked(String login) {
	remainedAttempts(login) < 1
    }

    int remainedAttempts(String login) {
        int result = allowedNumberOfAttempts - failedAttempts.get(login)
        result < 0 ? 0 : result
    }

    void afterPropertiesSet() {
	Assert.isTrue allowedNumberOfAttempts > 0, 'allowedNumberOfAttempts has to be greater than 0'
	Assert.isTrue lockTimeInMinutes > 0, 'lockTimeInMinutes has to be greater than 0'

	failedAttempts = CacheBuilder.newBuilder()
	    .expireAfterWrite(lockTimeInMinutes, MINUTES)
	    .build({ 0 } as CacheLoader) as Cache<String, Integer>
    }
}
