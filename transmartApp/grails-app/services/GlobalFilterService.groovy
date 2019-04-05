import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.beans.factory.annotation.Value

/**
 * @author mmcduffie
 */
class GlobalFilterService {

    static transactional = false

    @Value('${com.recomdata.search.paginate.max:0}')
    private int paginateMax

    Map createPagingParamMap(GrailsParameterMap params) {
	[max   : params.int('max', paginateMax),
	 offset: params.int('offset', 0)]
    }
}
