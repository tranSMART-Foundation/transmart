package javascript

import de.is24.util.karmatestrunner.junit.KarmaTestSuiteRunner
import org.junit.runner.RunWith

@RunWith(KarmaTestSuiteRunner)
@KarmaTestSuiteRunner.KarmaProcessName('./node_modules/.bin/karma')
@KarmaTestSuiteRunner.KarmaConfigPath('karma.conf.js')
class JavaScriptUnitTestKarmaSuite {
}
