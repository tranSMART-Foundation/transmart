package uk.ac.ebi.mydas.proxy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 30-Jun-2008
 * Time: 11:11:41
 * This Runnable class performs the query against the proxied DAS server.
 */
public class DasQueryRunnerThread implements Runnable {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "AbstractProxyDataSource".
     */
    private static final Logger LOGGER = Logger.getLogger(DasQueryRunnerThread.class);

    private String urlQueryString;

    private HttpClient httpClient;

    private boolean finished = false;

    private boolean successful;

    private String responseBody;

    public DasQueryRunnerThread(HttpClient client, String urlQueryString) {
        this.urlQueryString = urlQueryString;
        this.httpClient = client;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        final HttpGet get = new HttpGet(urlQueryString);
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("connecting to " + urlQueryString);
            }

            // Execute the get.
            final HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                successful = false;
                LOGGER.warn("Remote DAS Service at '" + urlQueryString + "' failed: Returned HTTP status code :" + response.getStatusLine().getStatusCode() + " with status :" + response.getStatusLine().getReasonPhrase());
            } else {
                final HttpEntity httpEntity = response.getEntity();
                responseBody = EntityUtils.toString(httpEntity);
                successful = responseBody.length() > 0;
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Could not form a valid URL from " + urlQueryString, e);
        } catch (IOException e) {
            LOGGER.error("IOException thrown when requesting URL " + urlQueryString, e);
        } finally {
            get.reset();
            finished = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getResponseString() {
        return responseBody;
    }

    public String getUrlQueryString() {
        return urlQueryString;
    }
}
