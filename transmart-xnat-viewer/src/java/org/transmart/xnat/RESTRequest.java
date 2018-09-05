package org.transmart.xnat;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author Sijin He
 */
public class RESTRequest {

    public HttpResponse doGet(String url, String session) {

        HttpResponse response = null;

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(url);
            getRequest.addHeader("Cookie", session);

            response = httpClient.execute(getRequest);

            

        } catch (ClientProtocolException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
        
        return response;


    }

    public HttpResponse doPost(String url, List parameters) {

        HttpResponse response = null;

        try {

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);

            post.setEntity(new UrlEncodedFormEntity(parameters));
            response = client.execute(post);

        } catch (IOException ex) {
            Logger.getLogger(XNATREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
}
