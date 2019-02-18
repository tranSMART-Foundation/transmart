package jobs.steps.helpers

import groovy.transform.CompileStatic
import jobs.misc.AnalysisQuartzJobAdapter
import org.codehaus.groovy.grails.test.runner.phase.IntegrationTestPhaseConfigurer
import org.junit.After
import org.junit.Before
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.support.GenericBeanDefinition

@CompileStatic
abstract class AbstractJobsIntegrationTest {

	@Before
	void initIntegrationTest() {
		addJobNameBean()
		initializeAsBean this
	}

	@After
	void cleanupJobScope() {
		AnalysisQuartzJobAdapter.cleanJobBeans()
	}

	private void addJobNameBean() {
		ConstructorArgumentValues constructorArgs = new ConstructorArgumentValues()
		constructorArgs.addIndexedArgumentValue(0, 'testJobName')

		IntegrationTestPhaseConfigurer.currentApplicationContext.registerBeanDefinition 'jobName',
				new GenericBeanDefinition(
						beanClass: String,
						constructorArgumentValues: constructorArgs,
						scope: 'job')
	}

	/**
	 * The autowiring in GrailsTestInterceptor is too limited.
	 * It only manually creates an AutowiredAnnotationBeanPostProcessor and
	 * manually calls it (see GrailsTestAutowirer#autowire), so @Resource
	 * annotations, which are handled by CommonAnnotationBeanPostProcessor,
	 * are not ignored.
	 */
	protected void initializeAsBean(object) {
		BeanFactory factory = IntegrationTestPhaseConfigurer.currentApplicationContext.beanFactory
		// "traditional" autowiring, also called in GrailsTestAutowirer#autowire
		factory.autowireBeanProperties object, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false
		/* apply bean post processors' postProcessPropertyValues(), which will
		 * do annotation injection courtesy of AutowiredAnnotationBeanPostProcessor
		 * and CommonAnnotationBeanPostProcessor */
		factory.autowireBean object
	}
}
