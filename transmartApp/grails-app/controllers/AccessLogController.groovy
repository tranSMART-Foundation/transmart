import com.recomdata.util.ExcelGenerator
import com.recomdata.util.ExcelSheet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.AccessLogFilter
import org.transmart.plugin.shared.UtilService
import org.transmartproject.db.log.AccessLogEntry

import java.text.SimpleDateFormat

class AccessLogController {

    private static final List<String> headers = ['Access Time', 'User', 'Event', 'Event Message'].asImmutable()

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Autowired private SearchService searchService
    @Autowired private UtilService utilService

    @Value('${com.recomdata.admin.paginate.max:0}')
    private int paginateMax

    def list() {

	AccessLogFilter filter = configureFilter()

	Map<String, ?> pageMap = searchService.createPagingParamMap(params, paginateMax, 0)
	pageMap.sort = 'accessTime'
	pageMap.order = 'desc'

	List<AccessLogEntry> result = AccessLogEntry.createCriteria().list(
	    max: pageMap.max,
	    offset: pageMap.offset,
	    sort: pageMap.sort,
	    order: pageMap.order) {
	    between 'accessTime', filter.startdate, filter.enddate
        }

	SimpleDateFormat df1 = new SimpleDateFormat('dd/MM/yyyy')
	[accessLogList: result, startdate: df1.format(filter.startdate),
	 enddate: df1.format(filter.enddate), totalcount: result.totalCount]
    }

    def export() {

	AccessLogFilter filter = configureFilter()

	Map<String, ?> pageMap = searchService.createPagingParamMap(params, paginateMax, 0)
	pageMap.sort = 'accessTime'
	pageMap.order = 'desc'

	List<AccessLogEntry> results = AccessLogEntry.createCriteria().list(
	    sort: pageMap.sort,
	    order: pageMap.order) {
	    between 'accessTime', filter.startdate, filter.enddate
        }

	List<List> values = []
	for (AccessLogEntry accessLog in results) {
	    values << [accessLog.accessTime, accessLog.username, accessLog.event, accessLog.eventMessage]
        }

	utilService.sendDownload response, 'application/vnd.ms-excel; charset=utf-8', 'pre_clinical.xls',
	    new ExcelGenerator().generateExcel([new ExcelSheet('sheet1', headers, values)])
    }

    def show(AccessLogEntry accessLog) {
	if (!accessLog) {
	    flash.message = "AccessLogEntry not found with id ${params.id}"
	    redirect action: 'list'
        }
	else {
	    [accessLog: accessLog]
        }
    }

    def delete(AccessLogEntry accessLog) {
	if (accessLog) {
	    accessLog.delete()
	    flash.message = "AccessLogEntry $params.id deleted"
        }
	else {
	    flash.message = "AccessLogEntry not found with id $params.id"
	}
	redirect action: 'list'
    }

    def edit(AccessLogEntry accessLog) {
	if (!accessLog) {
	    flash.message = "AccessLogEntry not found with id $params.id"
	    redirect action: 'list'
        }
        else {
	    [accessLog: accessLog]
        }
    }

    def update(AccessLogEntry accessLog) {
	if (accessLog) {
	    accessLog.properties = params
	    if (!accessLog.hasErrors() && accessLog.save()) {
		flash.message = "AccessLogEntry $params.id updated"
		redirect action: 'show', id: accessLog.id
	    }
	    else {
		render view: 'edit', model: [accessLog: accessLog]
	    }
        }
        else {
	    flash.message = "AccessLogEntry not found with id $params.id"
	    redirect action: 'edit', id: params.id
        }
    }

    def create() {
	[accessLog: new AccessLogEntry(params)]
    }

    def save() {
	AccessLogEntry accessLog = new AccessLogEntry(params)
	if (!accessLog.hasErrors() && accessLog.save()) {
	    flash.message = "AccessLogEntry $accessLog.id created"
	    redirect action: 'show', id: accessLog.id
        }
        else {
	    render view: 'create', model: [accessLog: accessLog]
        }
    }

    private AccessLogFilter configureFilter() {
	AccessLogFilter filter = session.accesslogFilter
	if (filter == null) {
	    filter = new AccessLogFilter()
	    session.accesslogFilter = filter
        }

	SimpleDateFormat df1 = new SimpleDateFormat('dd/MM/yyyy')
	GregorianCalendar calendar = new GregorianCalendar()

	try {
	    if (filter.startdate == null || params.startdate != null) {
		filter.startdate = df1.parse(params.startdate)
            }
        }
	catch (ignored) {
	    calendar.time = new Date()
	    calendar.add Calendar.WEEK_OF_MONTH, -1
	    filter.startdate = calendar.time
	}

	try {
	    if (filter.enddate == null || params.enddate != null) {
		calendar.time = df1.parse(params.enddate)
		calendar.set Calendar.HOUR_OF_DAY, 23
		calendar.set Calendar.MINUTE, 59
		filter.enddate = calendar.time
	    }
        }
	catch (ignored) {
	    filter.enddate = new Date()
        }

	filter
    }
}
