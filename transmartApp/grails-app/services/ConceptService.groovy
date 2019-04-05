import i2b2.Concept
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

class ConceptService {

    static transactional = false

    @Autowired private DataSource dataSource

    Concept getConceptByBaseCode(String baseCode) {
	Concept.findByBaseCode baseCode
    }

    List<Concept> getChildrenConcepts(Concept concept) {
	if (!concept || !concept.id || concept.level == null) {
	    return null
	}

	Concept.executeQuery('''
		from Concept as c
		where c.fullName like :fullNameLike
		  and level = :levelNew''',
		[fullNameLike: concept.fullName + "%", levelNew: concept.level + 1])
    }
}
