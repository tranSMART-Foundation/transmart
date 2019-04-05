package org.transmartproject.security

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

@CompileStatic
@Slf4j('logger')
class SSLCertificateValidation {

    static void disable() {
        try {
            SSLContext sslContext = SSLContext.getInstance('TLS')
	    TrustManager[] trustManagers = [new NullX509TrustManager()]
	    sslContext.init null, trustManagers, null
	    HttpsURLConnection.setDefaultSSLSocketFactory sslContext.socketFactory
	    HttpsURLConnection.setDefaultHostnameVerifier new NullHostnameVerifier()
        }
	catch (e) {
	    logger.error e.message, e
        }
    }

    @CompileStatic
    private static class NullX509TrustManager implements X509TrustManager {
	void checkClientTrusted(X509Certificate[] chain, String authType) {}
	void checkServerTrusted(X509Certificate[] chain, String authType) {}
	X509Certificate[] getAcceptedIssuers() { new X509Certificate[0] }
    }

    @CompileStatic
    private static class NullHostnameVerifier implements HostnameVerifier {
	boolean verify(String hostname, SSLSession session) { true }
    }
}
