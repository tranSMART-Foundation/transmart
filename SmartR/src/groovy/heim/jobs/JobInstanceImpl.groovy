package heim.jobs

import groovy.util.logging.Slf4j
import heim.session.SmartRSessionScopeInterfaced
import heim.tasks.Task
import heim.tasks.TaskFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.OrderComparator
import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.InvalidArgumentsException

import javax.annotation.PostConstruct

/**
 * Created by glopes on 09-10-2015.
 */
@SmartRSessionScopeInterfaced
@Component('jobInstance')
@Slf4j('logger')
class JobInstanceImpl implements JobInstance {

    @Autowired
    List<TaskFactory> taskFactories

    @Value('#{workflowType}')
    String workflow

    @PostConstruct
    void init() {
        // only Spring 4 orders the list automatically
        // Grails 2.3 still uses Spring 3
	logger.debug 'order the list for Spring3, skip when Spring4 is available'
        taskFactories.sort(new OrderComparator())
    }

    Task createTask(String name, Map<String, Object> arguments) {
	logger.debug 'createTask name {} arguments {}', name, arguments
        TaskFactory selectedTaskFactory =
                taskFactories.find { it.handles(name, arguments) }

        if (!selectedTaskFactory) {
            throw new InvalidArgumentsException('No task factory found for ' +
                    "task name '$name' and arguments $arguments")
        }
	logger.debug 'return new Task'
        selectedTaskFactory.createTask(name, arguments)
    }
}
