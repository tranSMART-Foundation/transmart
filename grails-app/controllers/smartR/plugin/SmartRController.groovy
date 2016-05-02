package smartR.plugin

import grails.converters.JSON
import heim.session.SessionService
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType


class SmartRController {

    SessionService sessionService

    static layout = 'smartR'

    def index() {
        [ scriptList: sessionService.availableWorkflows()]
    }

    /**
    *   Called to get the path to smartR.js such that the plugin can be loaded in the datasetExplorer
    */
    def loadScripts = {

        // list of required javascript files
        def scripts = [servletContext.contextPath + pluginContextPath + '/js/smartR/smartR.js']

        // list of required css files
        def styles = []

        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

        // for all js files
        for (file in scripts) {
            def m = [:]
            m["path"] = file.toString()
            m["type"] = "script"
            rows.put(m);
        }

        // for all css files
        for (file in styles) {
            def n = [:]
            n["path"] = file.toString()
            n["type"] = "css"
            rows.put(n);
        }

        result.put("success", true)
        result.put("totalCount", scripts.size())
        result.put("files", rows)

        render result as JSON;
    }

    /**
     * Get smart-r plugin context path
     */
    def smartRContextPath = {
        render servletContext.contextPath + pluginContextPath as String;
    }

    def biocompendium = {
        def url = 'http://biocompendium.embl.de'
        def path = '/cgi-bin/biocompendium.cgi'
        def http = new HTTPBuilder(url)
        def query = [
            section: 'upload_gene_lists_general',
            primary_org: 'Human',
            background: 'whole_genome',
            Category1: 'Human',
            gene_list_1: 'gene_list_1',
            SubCat1: 'hgnc_symbol',
            attachment1: params.genes
        ]
        http.post(path: path, body: query, requestContentType: ContentType.URLENC) { response ->
            def text = response.entity.content.text
            render text
        }
    }
}
