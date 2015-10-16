package heim.session

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import groovy.util.logging.Log4j
import org.springframework.aop.scope.ScopedProxyUtils
import org.transmartproject.core.users.User

/**
 * Holds the data for a specific session.
 */
@Log4j
class SessionContext {

    public final static String SMART_R_USER_BEAN = 'smartRBean'

    final UUID sessionId

    final String workflowType

    private Map<String, Object> beans = [:].asSynchronized()

    private Multimap<String, Runnable> destructionCallbacks =
            Multimaps.synchronizedMultimap(HashMultimap.create())

    SessionContext(User user, String workflowType) {
        sessionId = UUID.randomUUID()
        this.workflowType = workflowType
        beans[SMART_R_USER_BEAN] = user
    }

    Object getBean(String beanName) {
        beans[beanName]
    }

    void addBean(String beanName, Object value) {
        beans[beanName] = value
    }

    Object removeBean(String beanName) {
        destructionCallbacks.removeAll(beanName)
        beans.remove(beanName)
    }

    void registerDestructionCallback(String name, Runnable callback) {
        destructionCallbacks.put(name, callback)
    }

    void destroy() {
        destructionCallbacks.asMap().each { String bean,
                                            Collection<Runnable> callbacks ->
            log.debug("Calling destruction callbacks for bean $bean")
            callbacks.each {
                it.run()
            }
        }
    }
}
