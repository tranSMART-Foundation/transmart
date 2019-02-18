package jobs.misc

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Slf4j('logger')
class JobSpringScope implements Scope {

    def get(String name, ObjectFactory<?> objectFactory) {
        def object = beansStorage[name]
        if (object == null) {
            object = objectFactory.object
            beansStorage[name] = object
        }
        object
    }

    def remove(String name) {
        beansStorage.remove name
    }

    void registerDestructionCallback(String name, Runnable callback) {
        logger.warn 'Destruction callbacks are not supported'
    }

    def resolveContextualObject(String key) {
        // apparently just used for evaluating bean expressions
        // does not matter for our purposes
    }

    String getConversationId() {
        AnalysisQuartzJobAdapter.CURRENT_JOB_NAME
    }

    private Map<String, Object> getBeansStorage() {
        AnalysisQuartzJobAdapter.BEANS_STORAGE
    }
}
