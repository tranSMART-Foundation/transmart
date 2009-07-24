package uk.ac.ebi.mydas.proxy;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 30-Jun-2008
 * Time: 11:11:41
 * This Runnable class performs the query against the proxied DAS server.
 */
public class DasQueryRunnerThread implements Runnable{

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "AbstractProxyDataSource".
     */
    private static final Logger LOGGER = Logger.getLogger(DasQueryRunnerThread.class);


    private String urlQueryString;

    HttpClient httpClient;

    private boolean finished = false;

    private boolean successful;

    private StringBuffer responseBuffer = new StringBuffer();

    public DasQueryRunnerThread(HttpClient client, String urlQueryString){
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

        GetMethod get = new GetMethod(urlQueryString);
        try{
            if (LOGGER.isDebugEnabled()){
                LOGGER.debug("connecting to " + urlQueryString);
            }

            // Execute the get.
            int statusCode = httpClient.executeMethod(get);

            if (statusCode != HttpStatus.SC_OK) {
                successful = false;
                LOGGER.warn("Remote DAS Service at '" + urlQueryString + "' failed: Returned HTTP status code :" + statusCode + " with status :" + get.getStatusLine());
            }
            else {
                // Read the response body.
                InputStreamReader reader = null;
                try{
                    reader = new InputStreamReader( get.getResponseBodyAsStream(), get.getResponseCharSet() );
                    char[] charbuf = new char[1024];
                    int size;
                    while ((size = reader.read(charbuf)) > -1){
                        String line = new String(charbuf, 0, size).replaceAll(">\\s+<", "><");
//                        line = line.replaceAll(">\\s+<", "><");    // Compress it (remove pointless white space)
                        responseBuffer.append(line);
                    }

                }
                finally {
                    if (reader != null){
                        reader.close();
                    }
                }

                successful = responseBuffer.length() > 0;
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Could not form a valid URL from " + urlQueryString, e);
        } catch (IOException e) {
            LOGGER.error("IOException thrown when requesting URL " + urlQueryString, e);
        }
        finally {
            get.releaseConnection();
            finished = true;
        }
    }

    public boolean isFinished (){
        return finished;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getResponseString(){
        return responseBuffer.toString();
    }

    public String getUrlQueryString(){
        return urlQueryString;
    }

}
