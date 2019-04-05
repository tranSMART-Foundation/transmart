package org.transmart.marshallers

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.core.type.filter.TypeFilter

@Slf4j('logger')
class MarshallerRegistrarService implements FactoryBean {

    private static final String PACKAGE = 'org.transmart.marshallers'
    private static final String RESOURCE_PATTERN = '**/*Marshaller.class'

    final Class objectType
    final boolean singleton = true

    @Autowired ApplicationContext ctx

    void start() {
        logger.info 'Registering marshallers'

	ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(
	    (BeanDefinitionRegistry) ctx, false) {
            protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
                Set<BeanDefinitionHolder> superValue = super.doScan(basePackages)
		logger.debug 'Found marshallers: {}', superValue

		for (holder in superValue) {
                    def bean = ctx.getBean(holder.beanName)
		    JSON.registerObjectMarshaller(bean.targetType, bean.&convert)
                }

                superValue
            }
        }

	scanner.resourcePattern = RESOURCE_PATTERN
	scanner.addIncludeFilter new TypeFilter() {
	    boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
		metadataReader.classMetadata.className.matches '.+Marshaller'
	    }
	}

	scanner.scan PACKAGE
    }

    def getObject() {
        start()
        null
    }
}
