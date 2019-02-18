package jobs

import groovy.transform.CompileStatic
import groovy.json.JsonBuilder
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Scope('job')
class UserParameters {

    Map<String, Object> map = [:]

    def getAt(String key) {
        map.getAt(key)
    }

    def getProperty(String propertyName) {
        getAt propertyName
    }

    String toString() {
        'UserParameters' + map
    }

    String toJSON() {
        JsonBuilder builder = new JsonBuilder()
        builder new TreeMap(map) //sorting the map so its easier to compare visually
        builder
    }

    void each(Closure c) {
        map.each c
    }

}
