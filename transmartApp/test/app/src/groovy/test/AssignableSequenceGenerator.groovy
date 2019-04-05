package test

import grails.util.Environment
import org.hibernate.engine.SessionImplementor
import org.hibernate.id.SequenceGenerator

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class AssignableSequenceGenerator extends SequenceGenerator {
	@Override
	Serializable generate(SessionImplementor session, o) {
		if (Environment.current == Environment.TEST && o.id != null) {
			o.id
		}
		else {
			super.generate session, o
		}
	}
}
