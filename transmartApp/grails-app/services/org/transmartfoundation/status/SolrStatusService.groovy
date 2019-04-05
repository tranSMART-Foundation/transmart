package org.transmartfoundation.status

import groovy.transform.CompileStatic
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.util.NamedList
import org.springframework.beans.factory.annotation.Value

@CompileStatic
class SolrStatusService {

    static transactional = false
		
    private static final String solrQuery = '*:*'

    @Value('${com.rwg.solr.host:}')
    private String solrHost

    SolrStatus getStatus() {

	// https://lucene.apache.org/solr/5_4_1/solr-solrj/index.html
	String urlString = 'http://' + solrHost + '/solr/'

	SolrClient solr = new HttpSolrClient(urlString)
		
	NamedList nl = new NamedList()
	nl.addAll(q: solrQuery)
	SolrParams p = SolrParams.toSolrParams(nl)
		
	boolean reachedServer = serverDoesRespond(solr)
	
	int nDocs = getDocumentCountForQuery(solr, 'rwg', p)
	boolean rwgAvailable = nDocs > -1
	int rwgNumberOfRecords = rwgAvailable ? nDocs : 0
		
	nDocs = getDocumentCountForQuery(solr,'browse',p)
	boolean browseAvailable = nDocs > -1
	int browseNumberOfRecords = browseAvailable ? nDocs : 0

	nDocs = getDocumentCountForQuery(solr,'sample',p)
	boolean sampleAvailable = nDocs > -1
	int sampleNumberOfRecords = sampleAvailable ? nDocs : 0
			
	solr.close()
		
	new SolrStatus(
	    url                  : urlString,
	    connected            : reachedServer,
	    rwgAvailable         : rwgAvailable,
	    rwgNumberOfRecords   : rwgNumberOfRecords,
	    browseAvailable      : browseAvailable,
	    browseNumberOfRecords: browseNumberOfRecords,
	    sampleAvailable      : sampleAvailable,
	    sampleNumberOfRecords: sampleNumberOfRecords,
	    lastProbe            : new Date())
    }

    private boolean serverDoesRespond(SolrClient solr) {
	try {
	    solr.ping()
	    true
	}
	catch (SolrServerException ignored) {
	    false
	}
	catch (ignored) {
	    true
	}
    }
	
    private int getDocumentCountForQuery(SolrClient client, String coreName, SolrParams sp) {
	try {
	    client.query(coreName, sp).results.numFound
	}
	catch (ignored) {
	    -1
	}
    }
}
