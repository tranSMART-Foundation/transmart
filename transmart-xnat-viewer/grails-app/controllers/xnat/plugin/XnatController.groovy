package xnat.plugin

import groovy.util.logging.Slf4j
import org.apache.http.HttpResponse
import org.transmart.xnat.XNATREST

@Slf4j('logger')
class XnatController {

    ScanService scanService

    def download(String url) {

	XNATREST xnat = new XNATREST(scanService.domain, scanService.username, scanService.password)

	HttpResponse data = xnat.fetchData(url)

        InputStream input = null
	OutputStream output = response.outputStream
        byte[] buffer = new byte[1024]

        try {
	    input = data.entity.content

            for (int length; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length)
            }
        }
	catch (IOException | IllegalStateException e) {
	    logger.error e.message, e
        }
        finally {
            try {
		output?.close()
            }
	    catch (IOException ignored) {}
            try {
		input?.close()
            }
	    catch (IOException ignored) {}
        }
    }

    def image(String url) {
	download url
    }

    def info(String url) {
	XNATREST xnat = new XNATREST(scanService.domain, scanService.username, scanService.password)
	HttpResponse data = xnat.fetchData(url)
	BufferedReader rd = new BufferedReader(new InputStreamReader(data.entity.content))

        // Read response until the end
	boolean escape = false
	String line
        while ((line = rd.readLine()) != null) {
	    if (line.toLowerCase().contains("layout_content")) {
                escape = true
            }

	    if (line.toLowerCase().contains("mylogger")) {
                escape = false
            }

            if (escape) {
                render line.trim()
            }
        }
    }
}
