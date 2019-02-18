package jobs.misc

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * @author carlos
 */
@CompileStatic
@Component
@Scope('job')
class AnalysisConstraints {

    Map<String, Object> map = [:]

    def getAt(String key) {
        map[key]
    }

    def getProperty(String propertyName) {
        getAt propertyName
    }

    String toString() {
        'AnalysisConstraints' + map
    }
}
