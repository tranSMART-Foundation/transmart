package org.transmart.xnat;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Sijin He
 */
public class RESTRequest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public HttpResponse doGet(String url, String sessionId) {
        try {
            HttpGet getRequest = new HttpGet(url);
            getRequest.addHeader("Cookie", sessionId);
            return new DefaultHttpClient().execute(getRequest);
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public HttpResponse doPost(String url, List<? extends NameValuePair> parameters) {
        try {            HttpPost post = new HttpPost(url);

            post.setEntity(new UrlEncodedFormEntity(parameters));
            return new DefaultHttpClient().execute(post);
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
