package sendfile

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Value

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Originally in the sendfile plugin, but the most recent version doesn't
 * compile in Grails 2.4+ because it uses ConfigurationHolder.
 *
 * @author Vitaliy Samolovskih
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j('logger')
class SendFileService {

	private static final int BUFFER_SIZE = 4096

	static transactional = false

	@Value('${grails.plugins.sendfile.apache:false}')
	private boolean supportApache

	@Value('${grails.plugins.sendfile.nginx:false}')
	private boolean supportNginx

	@Value('${grails.plugins.sendfile.tomcat:false}')
	private boolean supportTomcat

	void sendFile(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
	              File file, Map headers = [:]) {
		logger.trace 'sendFile(HttpServletRequest request, HttpServletResponse response, File file, Map headers=[:])'

		long lastModified = file.lastModified()

		boolean modified = true
		try {
			long since = request.getDateHeader('If-Modified-Since')
			modified = since < lastModified - 1000     // 1 second (1000 millisecond) to compensate rounding error
		}
		catch (IllegalArgumentException ignored) {}

		String path = file.absolutePath

		response.setDateHeader 'Date', lastModified
		response.setDateHeader 'Last-Modified', lastModified
		response.setHeader 'Pragma', 'none'
		response.addHeader 'Expires', 'Fri, 04 Aug 2078 12:00:00 GMT'
		response.setHeader 'Etag', '"gf_' + encodeAsMD5(path) + '"'
		response.setHeader  'Cache-Control', 'max-age=604800,public'

		if (modified) {
			// Set status 200 (OK)
			response.status = HttpServletResponse.SC_OK

			// Set content headers
			response.setHeader 'Accept-Ranges', 'bytes'
			response.contentType = mimeType(servletContext, file, headers)
			response.contentLength = (int) file.length()
			response.setHeader 'Content-Disposition', contentDisposition(request, file, headers)

			if (supportNginx) {
				logger.debug 'Use nginx X-Accel-Redirect'
				response.setHeader 'X-Accel-Redirect', path
			}
			else if (supportApache) {
				logger.debug 'Use Apache X-Sendfile'
				response.setHeader 'X-Sendfile', path
			}
			else if (supportTomcat) {
				logger.debug 'Use Tomcat sendfile'
				request.setAttribute 'org.apache.tomcat.sendfile.filename', path
				request.setAttribute 'org.apache.tomcat.sendfile.start', (long) 0
				request.setAttribute 'org.apache.tomcat.sendfile.end', file.length()
			}
			else {
				logger.debug 'Send file directly to response output stream.'
				sendDirectly request, response, file
			}
		}
		else {
			// Send status=304 (Not modified)
			response.status = HttpServletResponse.SC_NOT_MODIFIED
		}

		response.flushBuffer()
	}

	/**
	 * Generate Content-Disposition header
	 */
	private String contentDisposition(HttpServletRequest request, File file, Map headers) {
		StringBuilder builder = new StringBuilder()
		builder << headers.dispositionType ?: 'attachment'
		builder << '; filename'

		// Filename
		String name = URLEncoder.encode(headers.filename as String ?: file.name)
		String userAgent = request.getHeader('User-Agent')
		if (StringUtils.containsIgnoreCase(userAgent, 'firefox') ||
				StringUtils.containsIgnoreCase(userAgent, 'opera')) {
			builder << "*=utf-8''"
			builder << name
		}
		else {
			builder << '="'
			builder << name
			builder << '"'
		}

		builder
	}

	/**
	 * Generate mimetype for file
	 */
	private String mimeType(ServletContext servletContext, File file, Map headers) {
		String mimetype = headers.contentType
		if (mimetype == null) {
			mimetype = servletContext.getMimeType(file.name.toLowerCase())
		}
		if (mimetype == null) {
			mimetype = headers.defaultContentType
		}
		mimetype ?: 'application/octet-stream'
	}

	private void sendDirectly(HttpServletRequest request, HttpServletResponse response, File file) {
		List<Integer> range = parseRange(request)

		// Offset
		long skip = 0
		if (range.size() >= 1) {
			skip = range[0]
		}

		// max byte index
		long size = file.length()
		long max = size
		if (range.size() >= 2) {
			max = Math.min(max, range[1])
		}

		// Write 'Content-Range' header
		StringBuilder rangeHeader = new StringBuilder('bytes')
		if (range) {
			rangeHeader << ' '
			rangeHeader << skip
			rangeHeader << '-'
			rangeHeader << max
			rangeHeader << '/'
			rangeHeader << size
		}

		response.setHeader 'Content-Range', rangeHeader.toString()

		// Write content
		InputStream stream = new FileInputStream(file)
		try {
			long offset = 0
			long len = 0

			while (offset < skip && len >= 0) {
				len = stream.skip(skip - offset)
				offset += len
			}

			byte[] buffer = new byte[BUFFER_SIZE]
			while (offset < max && len >= 0) {
				len = stream.read(buffer, 0, Math.min(buffer.length, (int)(max - offset)))
				if (len > 0) {
					response.outputStream.write buffer, 0, (int) len
				}
			}
		}
		finally {
			stream.close()
		}
	}

	/**
	 * Parse 'Range' header of HTTP-request
	 */
	private List<Integer> parseRange(HttpServletRequest request) {
		List<Integer> list = []
		String range = request.getHeader('Range')
		if (range) {
			Matcher matcher = Pattern.compile(/\d+/).matcher(range)
			while (matcher.find()) {
				list << Integer.decode(matcher.group())
			}
		}
		list
	}

	@CompileDynamic
	private String encodeAsMD5(String s) {
		s.encodeAsMD5()
	}
}
