package jobs

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Scope('job')
class AcghGroupTest extends AcghAnalysisJob {

    @Override
    protected List<String> getRStatements() {
        [
                '''source('$pluginDirectory/aCGH/acgh-group-test.R')''',
                '''acgh.group.test(column = 'group',
                                   test.statistic = '$statisticsType',
                                   test.aberrations = '$aberrationType',
                                   number.of.permutations=$numberOfPermutations)'''
        ]
    }

    @Override
    protected String getForwardPath() {
        "/aCGHgroupTest/aCGHgroupTestOutput?jobName=${name}"
    }
}
