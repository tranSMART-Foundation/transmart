package heim.session

import groovy.util.logging.Log4j
import org.springframework.aop.scope.ScopedProxyUtils
import org.springframework.beans.BeansException
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.config.BeanDefinitionVisitor
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.Scope
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.core.NamedThreadLocal
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.util.StringValueResolver
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.NoSuchResourceException

/**
 * Managed the @SessionScope scope. Object storage is the session itself.
 */
@Log4j
@Component
class SmartRSessionSpringScope implements Scope, BeanFactoryPostProcessor, Ordered {

    private static final String SCOPE_NAME = 'smartRSession'

    private static final boolean PROXY_CLASSES = true // rather than interfaces

    final int order = Ordered.LOWEST_PRECEDENCE;


    private static ThreadLocal<SessionContext> ACTIVE_SESSION =
            new NamedThreadLocal<SessionContext>("ActiveSmartRSession") {
                @Override
                protected SessionContext initialValue() {
                    null
                }
            };

    static <T> T withActiveSession(SessionContext ctx, Closure<T> closure) {
        if (ACTIVE_SESSION.get()) {
            if (ACTIVE_SESSION.get() != ctx) {
                throw new IllegalStateException(
                        'Attempt to set session in context with another ' +
                                'one already in place')
            } else {
                return
            }
        }
        try {
            ACTIVE_SESSION.set(ctx)
            closure.call()
        } finally {
            ACTIVE_SESSION.set(null)
        }
    }


    private SessionContext getSessionContext() {
        def res = ACTIVE_SESSION.get()
        if (!res) {
            throw new IllegalStateException('No active smartR session set')
        }
        res
    }

    @Override
    Object get(String name, ObjectFactory<?> objectFactory) {
        def object = sessionContext.getBean(name)
        if (object == null) {
            object = objectFactory.getObject()
            sessionContext.addBean(name, object)
        }
        object
    }

    @Override
    Object remove(String name) {
        sessionContext.removeBean(name)
    }

    @Override
    void registerDestructionCallback(String name, Runnable callback) {
        sessionContext.registerDestructionCallback(name, callback)
    }

    @Override
    Object resolveContextualObject(String key) {
        if (sessionContext.hasProperty(key)) {
            sessionContext."$key"
        }
    }

    @Override
    String getConversationId() {
        sessionContext.sessionId.toString()
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope('smartRSession', this)
    }
}
