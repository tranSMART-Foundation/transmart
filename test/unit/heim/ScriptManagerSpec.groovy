package heim

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class ScriptManagerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
        def heatmap = new File("/tmp/last_heatmap.png")
        if (heatmap.exists() ){
            heatmap.delete()
        }

    }

    void "test reading in a script from Heatmap workflow"() {
        given: "Heatmap workflow and init.r scripts."
        def workflowName = "heatmap"
        def scriptName = "init.r"
        when: "Trying to read in the init.r script."
        def result = ScriptManagerService.readScript(workflowName, scriptName)
        then: "Resulting string is not null and consists of 8 lines"
        result
        result.split("\n").size() == 8
    }

    void "test running the workflow"(){
        given:"Heatmap workflow"
        def  workflowName = "heatmap"
        when: "Initializing the workflow with the ScriptManager"
        ScriptManagerService.runWorkflow(workflowName)
        then:
            new File("/tmp/last_heatmap.png").exists()
    }

    void "test initializing the workflow"(){
        given:"Heatmap workflow"
        def  workflowName = "heatmap"
        when: "Initializing the workflow with the ScriptManager"
        def result = ScriptManagerService.initializeWorkflow(workflowName)
        then:
        result == '[{"variableName":"expression","variableType":"High-Dimension"},{"variableName":"patients","variableType":"Patient-Set"}]'
    }

    void "test listing workflows"(){
        given:"Only Heatmapworkflow is present"

        when:"listing studies"
            def result =  ScriptManagerService.listWorkflows()
        then:"Only heatmap workflow is returned in a list"
            result.size() == 1
            result[0] == 'heatmap'
    }
}
