import com.recomdata.datasetexplorer.proxy.XmlHttpProxy
import com.recomdata.datasetexplorer.proxy.XmlHttpProxyServlet
import groovy.util.logging.Slf4j

/**
 * @author JIsikoff
 */
@Slf4j('logger')
class ProxyController {

    static defaultAction = 'proxy'

    def proxy(String callback, String urlparams, String count, String url) {
	String serviceKey = null // TODO always null

	String bodyContent = request.inputStream.text

        try {
            String urlString = null
            // encode the url to prevent spaces from being passed along
	    if (urlparams) {
		urlparams = urlparams.replace(' ', '+')
            }

            try {
                //code for passing the url directly through instead of using configuration file
		if (url) {
                    // build the URL
		    if (urlparams && !url.contains('?')) {
			url += '?'
                    }
		    else if (urlparams) {
			url += '&'
                    }
		    urlString = url
		    if (urlparams) {
			urlString += urlparams
		    }
		}
		else {
		    PrintWriter writer = response.writer
		    if (serviceKey == null) {
			writer.write 'XmlHttpProxyServlet Error: id parameter specifying serivce required.'
                    }
                    else {
			writer.write "XmlHttpProxyServlet Error : service for id '" + serviceKey + "' not  found."
		    }
                    writer.flush()
                    return
                }
            }
	    catch (e) {
		logger.error 'XmlHttpProxyServlet Error loading service: {}', e.message, e
            }

	    Map paramsMap = [format: 'json']
            // do not allow for xdomain unless the context level setting is enabled.
	    boolean allowXDomain = true // TODO always true
	    if (callback && allowXDomain) {
		paramsMap.callback = callback
            }
	    if (count) {
		paramsMap.count = count
            }

	    if (!urlString) {
		PrintWriter writer = response.writer
		writer.write 'XmlHttpProxyServlet parameters:  id[Required] urlparams[Optional] format[Optional] callback[Optional]'
                writer.flush()
                return
            }

	    response.contentType = 'text/xml;charset=UTF-8' //changed from text/json in jmaki source
	    OutputStream out = response.outputStream

	    InputStream xslInputStream = null
	    String xslURLString = null
	    if (xslURLString != null) {  // TODO always null
		URL xslURL = servletContext.getResource(resourcesDir + 'xsl/' + xslURLString)
                if (xslURL == null) {
		    xslURL = XmlHttpProxyServlet.getResource(classpathResourcesDir + 'xsl/' + xslURLString)
                }
                if (xslURL != null) {
                    xslInputStream = xslURL.openStream()
                }
                else {
		    String message = 'Could not locate the XSL stylesheet provided for service id ' +
			serviceKey + '. Please check the XMLHttpProxy configuration.'
		    logger.debug message
                    try {
			out.write message.bytes
                        out.flush()
                        return
                    }
		    catch (IOException ignored) {}
                }
            }

	    String userName = null // TODO always null
	    String password = null // TODO always null
	    if (!request.post) {
		logger.trace 'proxying to:{}', urlString
		new XmlHttpProxy().doGet urlString, out, xslInputStream, paramsMap, userName, password
            }
            else {
		if (!bodyContent) {
		    logger.debug 'XmlHttpProxyServlet attempting to post to url {} with no body content', urlString
		}
		logger.trace 'proxying to:{}', urlString
		new XmlHttpProxy().doPost urlString, out, xslInputStream, paramsMap, bodyContent,
		    request.contentType, userName, password
            }
        }
	catch (e) {
	    logger.error 'XmlHttpProxyServlet: caught {}', e.message, e
            try {
		PrintWriter writer = response.writer
		writer.write 'XmlHttpProxyServlet error loading service for ' +
		    serviceKey + ' . Please notify the administrator.'
		writer.flush()
            }
	    catch (IOException ioe) {
		logger.error ioe.message, ioe
            }
        }
    }
}
