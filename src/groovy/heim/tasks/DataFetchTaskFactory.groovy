package heim.tasks

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.exceptions.InvalidArgumentsException

/**
 * Created by glopes on 09-10-2015.
 */
@Component
class DataFetchTaskFactory implements TaskFactory, ApplicationContextAware {

    public static final String FETCH_DATA_TASK_NAME = 'fetchData'

    public static final String CONCEPT_KEY_PARAMETER_NAME = 'conceptKey'
    public static final String ASSAY_CONSTRAINTS_PARAMETER_NAME = 'assayConstraints'
    public static final String DATA_CONSTRAINTS_PARAMETER_NAME = 'dataConstraints'
    public static final String PROJECTION_PARAMETER_NAME = 'projection'
    public static final String RESULT_INSTANCE_ID_PARAMETER_NAME = 'resultInstanceId'
    public static final String DATA_TYPE_PARAMETER_NAME = 'dataType'
    public static final String LABEL_PARAMETER_NAME = 'label'

    ApplicationContext applicationContext

    @Override
    boolean handles(String taskName, Map<String, Object> argument) {
        taskName == FETCH_DATA_TASK_NAME
    }

    // we don't support several constraints of the same type yet
    private boolean constraintsOK(Object value) {
        if (value == null) {
            return true
        }
        (value instanceof Map) &&
                value.keySet().every { it instanceof  String } &&
                value.values().every { it instanceof Map }
    }

    @Override
    Task createTask(String name,
                    Map<String, Object> arguments) {
        def conceptKeyString = arguments[CONCEPT_KEY_PARAMETER_NAME]
        def resultInstanceIdString =
                arguments[RESULT_INSTANCE_ID_PARAMETER_NAME] as String
        def labelArgument = arguments[LABEL_PARAMETER_NAME]

        if (!conceptKeyString || !(conceptKeyString instanceof String)) {
            throw new InvalidArgumentsException(
                    "Argument $CONCEPT_KEY_PARAMETER_NAME must be given and " +
                            "be a string")
        }
        if (resultInstanceIdString && !resultInstanceIdString.isLong()) {
            throw new InvalidArgumentsException(
                    "Argument $RESULT_INSTANCE_ID_PARAMETER_NAME must be " +
                            "a long")
        }
        if (!labelArgument || !(labelArgument instanceof String)) {
            throw new InvalidArgumentsException("Argument " +
                    "$LABEL_PARAMETER_NAME must be given and must be a string")
        }

        applicationContext.getBean(DataFetchTask).with {
            conceptKey =  new ConceptKey(conceptKeyString)
            label = labelArgument
            resultInstanceId = resultInstanceIdString as Long

            assayConstraints = arguments[ASSAY_CONSTRAINTS_PARAMETER_NAME]
            dataConstraints = arguments[DATA_CONSTRAINTS_PARAMETER_NAME]
            projection = arguments[PROJECTION_PARAMETER_NAME]
            dataType = arguments[DATA_TYPE_PARAMETER_NAME]

            if (!constraintsOK(assayConstraints)) {
                throw new InvalidArgumentsException(
                        'assay constraints need to be a map string -> map')
            }
            if (!constraintsOK(dataConstraints)) {
                throw new InvalidArgumentsException(
                        'data constraints need to be a map string -> map')
            }

            it
        }
    }

    final int order = 0
}
