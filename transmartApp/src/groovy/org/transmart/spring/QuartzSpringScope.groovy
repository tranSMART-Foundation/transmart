package org.transmart.spring

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import org.springframework.core.NamedThreadLocal

@CompileStatic
@Slf4j('logger')
class QuartzSpringScope implements Scope {

    private static final ThreadLocal<Map<String, Object>> SCOPE_MAP = createScopeMap()

    void setProperty(String name, value) {
        SCOPE_MAP.get()[name] = value
    }

    void clear() {
        SCOPE_MAP.remove()
    }

    def get(String name, ObjectFactory<?> objectFactory) {
        // the only way beans are added to this scope is via setProperty. We refuse to create beans
        def ret = SCOPE_MAP.get()[name]
	if(ret) {
	    return ret
        }

        throw new IllegalStateException('No bean named "' + name + '" has been submitted to this scope. This scope does not create beans')
    }

    def remove(String name) {
        throw new UnsupportedOperationException('Scope does not support removal')
    }

    void registerDestructionCallback(String name, Runnable callback) {
        logger.warn 'Destruction callbacks are not supported; tried to add one for {}', name
    }

    def resolveContextualObject(String key) {}

    String getConversationId() {}

    @CompileDynamic
    private static ThreadLocal<Map<String, Object>> createScopeMap() {
	new NamedThreadLocal<Map<String, Object>>('JobScope') {
	    protected Map<String, Object> initialValue() { [:] }
	}
    }
}
