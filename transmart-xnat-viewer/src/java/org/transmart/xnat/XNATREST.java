package org.transmart.xnat;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sijin He
 */
public class XNATREST {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static String jsessionid = "";
    
    public XNATREST(String domain, String username, String password){
        login(domain, username, password);
    }

    public void login(String domain, String username, String password) {
        try {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("login_method", "Database"));
            nameValuePairs.add(new BasicNameValuePair("j_username", username));
            nameValuePairs.add(new BasicNameValuePair("j_password", password));
            nameValuePairs.add(new BasicNameValuePair("login", "Login"));
            nameValuePairs.add(new BasicNameValuePair("XNAT_CSRF", ""));
            HttpResponse response = new RESTRequest().doPost("https://" + domain + "/j_spring_security_check", nameValuePairs);
            //Get Headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                if (header.getName().equalsIgnoreCase("Set-Cookie")) {

                    String[] sessionInfo = header.getValue().split(";", -1);
                    for (String s : sessionInfo) {
                        if (s.startsWith("JSESSIONID=")) {
                            jsessionid = s;
                        }
                    }
                }
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public HttpResponse fetchData(String url) {
        HttpResponse response = new RESTRequest().doGet(url, jsessionid);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " +
                                       response.getStatusLine().getStatusCode());
        }

        return response;
    }
}
