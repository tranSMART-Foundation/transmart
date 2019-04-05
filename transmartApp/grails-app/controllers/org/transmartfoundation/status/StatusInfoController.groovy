package org.transmartfoundation.status

import org.springframework.beans.factory.annotation.Autowired

class StatusInfoController {

    @Autowired private SolrStatusService solrStatusService
    @Autowired private RserveStatusService rserveStatusService
    @Autowired private GwavaStatusService gwavaStatusService

    def index() {
	[solrStatus: solrStatusService.status,
	 rserveStatus: rserveStatusService.status,
	 gwavaStatus: gwavaStatusService.status]
    }
}
