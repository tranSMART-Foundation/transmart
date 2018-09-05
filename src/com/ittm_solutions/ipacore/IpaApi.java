/*
 * Copyright (C) 2016 ITTM S.A.
 *
 * Written by Nils Christian <nils.christian@ittm-solutions.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ittm_solutions.ipacore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

/**
 * Wrapper around the REST API of Ingenuity Pathway Analysis (IPA).
 * <p>
 * This class wraps calls to IPA's REST API. It handles the login procedure,
 * parses responses and returns Java objects (instead of plain strings from http
 * responses), most importantly {@link IpaAnalysisResults} for the results of an
 * analysis.
 * <p>
 * For details check out the documentation about `IPA Integration Module` on
 * <a href="http://www.ingenuity.com/products/custom-solutions">Ingenuity's
 * website</a> (last retrieved July 26, 2016).
 */
public class IpaApi {
    /**
     * user name used to log in to IPA server
     */
    private String username;
    /**
     * password used to log in to IPA server
     */
    private String password;
    /**
     * url of IPA server
     */
    private String serverUrl;
    /**
     * context used for keeping the http session
     */
    private HttpClientContext hcContext = null;
    /**
     * manager to reuse http connections
     */
    private PoolingHttpClientConnectionManager connectionManager = null;
    private final Log log = LogFactory.getLog(IpaApi.class);

    /**
     * Constructor.
     *
     * @param username
     *            the username used to connect to the IPA server
     * @param password
     *            the password used to connect to the IPA server
     * @param serverUrl
     *            the URL of the the IPA server
     */
    public IpaApi(String username, String password, String serverUrl) {
        this.username = username;
        this.password = password;
        this.serverUrl = serverUrl;
        log.debug("Created IpaApi for user: " + username + ", password: *, server: " + serverUrl);
    }

    /**
     * Constructor using the default URL for the IPA server.
     *
     * @param username
     *            the username used to connect to the IPA server
     * @param password
     *            the password used to connect to the IPA server
     */
    public IpaApi(String username, String password) {
        this(username, password, "https://analysis.ingenuity.com");
    }

    /**
     * Returns the connection manager used for all connections to the IPA
     * server.
     * <p>
     * This method initializes the connection manager on its first call,
     * subsequent calls return the same instance.
     *
     * @return connection manager
     */
    private PoolingHttpClientConnectionManager connectionManager() {
        if (connectionManager == null) {
            connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(10);
            // There must be only one connection as IPA fails otherwise
            connectionManager.setDefaultMaxPerRoute(1);
        }

        return connectionManager;
    }

    /**
     * Returns the http client context used for all connections to the IPA
     * server.
     * <p>
     * This method initializes the http client context on its first call,
     * subsequent calls return the same instance.
     *
     * @return http client context
     */
    private HttpClientContext clientContext() {
        if (hcContext == null) {
            // Create a local instance of cookie store
            CookieStore cookieStore = new BasicCookieStore();
            // Create local HTTP context
            hcContext = HttpClientContext.create();
            // Bind custom cookie store to the local context
            hcContext.setCookieStore(cookieStore);
        }

        return hcContext;
    }

    /**
     * Returns a http client with reusing connections.
     * <p>
     * This method uses the http client context and the connection manager to
     * create a http client.
     *
     * @return http client
     */
    private CloseableHttpClient httpClient() {
        PoolingHttpClientConnectionManager cm = this.connectionManager();
        LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setRedirectStrategy(redirectStrategy)
                .build();
        return httpClient;
    }

    /**
     * Returns the final URI in a redirect chain of a request.
     *
     * @param context
     *            client context as returned by {@code clientContext}
     * @return URI
     */
    private static URI currentRedirectUri(HttpClientContext context) {
        HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
        HttpHost currentHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        URI uri=null;
        try {
            uri = ((currentReq.getURI().isAbsolute()) ? currentReq.getURI()
                    : (new URI(currentHost.toURI() + currentReq.getURI())));
        } catch (URISyntaxException e) {
            // this should never happen
            throw new RuntimeException(e);
        }
        return uri;
    }

    /**
     * Parses html content and returns the login ticket.
     * <p>
     * This method reads through the html code and returns the login ticket
     * contained in tag {@code name="lt" value="<ticket>"}.
     *
     * @param is
     *            the input stream containing the response content
     * @return login ticket
     */

    private String loginTicket(InputStream is) throws IpaApiException {
        final Pattern loginTicketRegex = Pattern.compile(".*name=\"lt\" value=\"([^\"]+)\".*");
        try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
            String str = null;
            while ((str = reader.readLine()) != null) {
                Matcher m = loginTicketRegex.matcher(str);
                if (m.find()) {
                    return m.group(1);
                }
            }
        } catch (IOException e) {
            throw new IpaApiException(e);
        }
        // login ticket not defined
        throw new IpaApiException("lt not found");
    }

    /**
     * Performs authentication with IPA server with the given http client and
     * context.
     *
     * @param hc
     *            http client as returned by {@code httpClient}
     * @param context
     *            client context as returned by {@code clientContext}
     *
     * @return login ticket
     */
    private void login(CloseableHttpClient hc, HttpClientContext context) throws IpaApiException {
        String lt = null;
        URI loginUri = null;
        HttpGet httpget = new HttpGet(serverUrl);
        try (CloseableHttpResponse response = hc.execute(httpget, context)) {
            log.info("get " + serverUrl + " status " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IpaApiHttpResponseException(serverUrl,response);
            }

            // get the final URI from the redirect chain
            loginUri = currentRedirectUri(context);

            // get the login ticket from the html content
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IpaApiEntityNullException();
            }
            lt = this.loginTicket(entity.getContent());
        } catch (IOException e) {
            throw new IpaApiException(e);
        }

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("username", username));
        postParameters.add(new BasicNameValuePair("password", password));
        postParameters.add(new BasicNameValuePair("_eventId", "submit"));
        postParameters.add(new BasicNameValuePair("lt", lt));
        HttpPost httppost = new HttpPost(loginUri);
        try {
            httppost.setEntity(new UrlEncodedFormEntity(postParameters));
        } catch (UnsupportedEncodingException e) {
            throw new IpaApiException(e);
        }
        try (CloseableHttpResponse response = hc.execute(httppost, context)) {
            log.info("post " + loginUri + " status " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IpaApiHttpResponseException(serverUrl,response);
            }

            if (!currentRedirectUri(context).toString().startsWith(serverUrl)) {
                throw new IpaApiAuthenticationFailedException();
            }
        } catch (IOException e) {
            throw new IpaApiException(e);
        }
        log.debug("authentication successful");
    }

    /**
     * Checks the string (a part of the content) for known errors.
     * <p>
     * This method reads through the given substring of the response content and
     * checks whether it contains a fragment that could indicate an error.
     *
     * @return null or the error indicator
     */
    private static IpaErrorIndication containsStringIndicatingError(String contentFragement) {
        // FIXME an error div may span multiple lines, and could include other
        // divs; these regexes are not sufficient, a real xml parser should be
        // used
        final Pattern errDivRgx1 = Pattern.compile(".*?<\\s*div\\s+id='errors'.*?>(.*?)</\\s*div\\s*>.*");
        final Pattern errDivRgx2 = Pattern.compile(".*?<\\s*div\\s+id='errors'.*?>(.*)");

        if (Pattern.matches(".*Your current IPA license does not have privileges to access this feature.*", contentFragement)) {
            return IpaErrorIndication.UNAUTHORIZED;
        }
        if (Pattern.matches(".*The dataset name \\(.*?\\) already exists in the project .*?\\.\\s+Please choose a different dataset name.*", contentFragement)) {
            return IpaErrorIndication.DATASET_NAME_EXISTS;
        }
        if (Pattern.matches(".*The analysis name \\(.*?\\) already exists in the project .*?\\.\\s+Please choose a different analysis name.*", contentFragement)) {
            return IpaErrorIndication.ANALYSIS_NAME_EXISTS;
        }
        if (Pattern.matches("^No analysis found for ID=(\\d+) for userId=(\\d+)$", contentFragement)) {
            return IpaErrorIndication.ANALYSIS_ID_NOT_FOUND;
        }
        if (Pattern.matches(".*session has been expired.*", contentFragement)) {
            return IpaErrorIndication.SESSION_EXPIRED;
        }
        Matcher m1 = errDivRgx1.matcher(contentFragement);
        if (m1.find()) {
            IpaErrorIndication err = IpaErrorIndication.OTHER;
            err.setMessage(m1.group(1));
            return err;
        }
        Matcher m2 = errDivRgx2.matcher(contentFragement);
        if (m2.find()) {
            IpaErrorIndication err = IpaErrorIndication.OTHER;
            err.setMessage(m2.group(1));
            return err;
        }
        if (Pattern.matches("<\\s*html.*", contentFragement)) {
            return IpaErrorIndication.CONTAINS_HTML;
        }

        return null;
    }

    /**
     * Checks the remaining content of a {@code HttpResponse} for the reason of an
     * error.
     * <p>
     * Errors are communicated through plain text or html in a response. When
     * parsing a response {@link containsStringIndicatingError} may encounter a
     * hint that the content is not what is expected but rather an error (eg.
     * {@code <html>}). This method can take over to parse the rest of the content
     * to find the real reason of the error (eg. a message contained inside a
     * {@code <div id='errors'>}).
     *
     * @param reader
     *          the reader object created from a {@code HttpResponse}; parts of it
     *          may already be consumed
     * @return the main error reason
     */
    private static IpaErrorIndication parseContentForError(BufferedReader reader, IpaErrorIndication currentReason) throws IOException {
        if (currentReason == null) {
            throw new IllegalArgumentException();
        }
        IpaErrorIndication errorWithString = null;
        IpaErrorIndication errorWithoutString = null;
        boolean sessionExpired = false;
        boolean unAuthorized = false;
        IpaErrorIndication thisErr = currentReason;
        String currentline = null;
        do {
            if (currentline != null) {
                thisErr = containsStringIndicatingError(currentline);
            }
            if (thisErr != null) {
                if (thisErr == IpaErrorIndication.UNAUTHORIZED) {
                    unAuthorized = true;
                } else if (thisErr == IpaErrorIndication.SESSION_EXPIRED) {
                    sessionExpired = true;
                } else if (thisErr == IpaErrorIndication.CONTAINS_HTML) {
                    // nothing here, the least relevant error
                } else if (thisErr == IpaErrorIndication.OTHER) {
                    // NOTE we have never seen this, but in case there are two
                    // error strings just keep the first
                    if (errorWithString == null) {
                        errorWithString = thisErr;
                    }
                } else {
                    errorWithoutString = thisErr;
                }
            }

        } while ((currentline = reader.readLine()) != null);

        // return the most relevant error
        if (errorWithString != null) {
            return errorWithString;
        }
        if (unAuthorized) {
            return IpaErrorIndication.UNAUTHORIZED;
        }
        if (sessionExpired) {
            return IpaErrorIndication.SESSION_EXPIRED;
        }
        if (errorWithoutString != null) {
            return errorWithoutString;
        }
        return IpaErrorIndication.CONTAINS_HTML;
    }

    /**
     * Checks whether the current session is still usable.
     * <p>
     * This method makes a dummy request and checks whether it receives a valid
     * answer. If this is the case, the connection is still usable.
     *
     * @param hc
     *            http client as returned by {@code httpClient}
     * @param context
     *            client context as returned by {@code clientContext}
     * @return null or the error indicator
     */
    private boolean sessionAlive(CloseableHttpClient hc, HttpClientContext context) throws IpaApiException {
        String path = "/pa/api/v1/projectsearch";
        try {
            URI uri = new URIBuilder(serverUrl).setPath(path).addParameter("query", "XXXXXX").build();
            HttpGet httpget = new HttpGet(uri);
            try (CloseableHttpResponse response = hc.execute(httpget, context)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    return false;
                }

                // check content for strings indicating that we first have to log in
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new IpaApiEntityNullException();
                }
                try (InputStreamReader isr = new InputStreamReader(entity.getContent());
                        BufferedReader reader = new BufferedReader(isr)) {
                    String contentLine = null;
                    while ((contentLine = reader.readLine()) != null) {
                        IpaErrorIndication err = containsStringIndicatingError(contentLine);
                        if (err != null) {
                            log.debug(parseContentForError(reader, err));
                            return false;
                        }
                    }
                }
            }
        } catch (URISyntaxException|IOException e) {
            throw new IpaApiException(e);
        }
        return true;
    }

    /**
     * Returns a http client that is authenticated (as thus usable).
     *
     * @return http client
     */
    private CloseableHttpClient authenticatedClient() throws IpaApiException {
        CloseableHttpClient hc = this.httpClient();
        HttpClientContext context = this.clientContext();
        if (this.sessionAlive(hc, context)) {
            log.debug("authenticatedClient: reusing existing session");
            return hc;
        }

        this.login(hc, context);
        log.debug("authenticatedClient: created session");
        return hc;
    }

    /**
     * Exports the given analyses or all analyses of an IPA project.
     * <p>
     * See {@link #exportAnalyses} for details on {@code resultTypes}.
     *
     * @param projectId
     *          the id of an IPA project (usually a number)
     * @param analysisIds
     *          the analyses ids
     * @param resultTypes
     *          the result types (see {@code exportAnalyses})
     * @return the exported results wrapped in a class
     */
    private CloseableHttpResponse exportRawAnalyses(String projectId, List<String> analysisIds, List<String> resultTypes)
            throws IpaApiException {
        final String path = "/pa/api/v1/export";
        CloseableHttpResponse response;
        try {
            URIBuilder uribuilder = new URIBuilder(serverUrl).setPath(path);
            if (projectId != null) {
                uribuilder.addParameter("pid", projectId);
            }
            // FIXME if we get a large number of analysis ids we may have to split
            // this list, see section 2.4.1 of IPA Integration Module
            if (analysisIds.size() > 0) {
                uribuilder.addParameter("aid", StringUtils.join(analysisIds, ","));
            }
            if (resultTypes.size() > 0) {
                uribuilder.addParameter("art", StringUtils.join(resultTypes, ","));
            }
            URI uri = uribuilder.build();

            CloseableHttpClient hc = this.authenticatedClient();
            HttpClientContext context = this.clientContext();
            HttpGet httpget = new HttpGet(uri);
            response = hc.execute(httpget, context);
            log.info("get " + uri + " status " + response.getStatusLine().getStatusCode());
        } catch (URISyntaxException|IOException e) {
            throw new IpaApiException(e);
        }
        return response;
    }

    /**
     * Exports the given analyses.
     * <p>
     * This method calls the export endpoint of the IPA API, allowing to export
     * analyses by their ids and restricting the results to certain types.
     * <p>
     * Available {@code result types} are: {@code settings}, {@code networks},
     * {@code canonicalpathways}, {@code biofunctions}, {@code toxfunctions},
     * {@code upstreamregulators}, {@code causalnetworks}, {@code mypathways},
     * {@code mylists}, {@code toxlists} and {@code analysisreadymolecules}. If
     * {@code resultTypes} is null all results will be exported.
     *
     * @param analysisIds
     *          the analyses ids
     * @param resultTypes
     *          the result types
     * @return list of exported results wrapped in a class
     */
    public synchronized List<IpaAnalysisResults> exportAnalyses(List<String> analysisIds, List<String> resultTypes)
            throws IpaApiException {
        List<IpaAnalysisResults> ipaResults;
        try {
            try (CloseableHttpResponse rawAnalyses = exportRawAnalyses(null, analysisIds, resultTypes)) {
                HttpEntity entity = rawAnalyses.getEntity();
                if (entity == null) {
                    throw new IpaApiEntityNullException();
                }
                ipaResults = parseExport(entity.getContent());
            }
        } catch (IOException e) {
            throw new IpaApiException(e);
        }

        return ipaResults;
    }

    /**
     * Exports the given analyses.
     * <p>
     * This method calls the export endpoint of the IPA API, allowing to export
     * analyses by their ids.
     *
     * @param analysisIds
     *          the analyses ids
     * @return list of exported results wrapped in a class
     */
    public synchronized List<IpaAnalysisResults> exportAnalyses(List<String> analysisIds) throws IpaApiException {
        return exportAnalyses(analysisIds, new ArrayList<String>());
    }

    /**
     * Exports the given analysis.
     * <p>
     * This method calls the export endpoint of the IPA API, allowing to export
     * one analysis by its id.
     *
     * @param analysisId
     *          the analysis id
     * @return exported results wrapped in a class
     */
    public synchronized IpaAnalysisResults exportAnalysis(String analysisId) throws IpaApiException {
        List<String> analysisIds = new ArrayList<String>(Arrays.asList(analysisId));
        List<IpaAnalysisResults> res = exportAnalyses(analysisIds, new ArrayList<String>());
        if (res.size() != 1) {
            return null;
        }
        return res.get(0);
    }

    /**
     * Exports the given analyses or all analyses of an IPA project.
     * <p>
     * This method calls the export endpoint of the IPA API, allowing to export
     * all analyses of a project with the given id.
     *
     * @param query
     *          the search string (allows wildcards like {@code *})
     * @return list of analysis ids
     */
    public synchronized List<String> searchAnalysis(String query) throws IpaApiException {
        final String path = "/pa/api/v1/projectsearch";

        List<String> analysisIds;
        URI uri;
        try {
            uri = new URIBuilder(serverUrl).setPath(path).addParameter("query", query).build();
        } catch (URISyntaxException e) {
            throw new IpaApiException(e);
        }

        CloseableHttpClient hc = this.authenticatedClient();
        HttpClientContext context = this.clientContext();
        HttpGet httpget = new HttpGet(uri);
        try (CloseableHttpResponse response = hc.execute(httpget, context)) {
            log.info("get " + uri + " status " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IpaApiHttpResponseException(uri,response);
            }

            // get the login ticket from the html content
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IpaApiEntityNullException();
            }
            // FIXME don't use EntityUtils.toString
            // FIXME parseContentForError should be used here
            String searchResult = EntityUtils.toString(entity);
            if (searchResult.length() == 0 || searchResult.matches("^\\s+$")) {
                return new ArrayList<String>();
            }
            analysisIds = new ArrayList<String>(Arrays.asList(searchResult.split(",")));
        } catch (IOException e) {
            throw new IpaApiException(e);
        }

        return analysisIds;
    }

    // FIXME this parameter list does not reflect what the IPA-API is capable of
    /**
     * Uploads a dataset and initiates an analysis on the server.
     * <p>
     * This method calls the {@code dataanalysis} endpoint of the IPA API,
     * allowing to upload a dataset and starting an analysis. This method does not
     * (yet) provide all parameters of the IPA API supports.
     * <p>
     * For details of the meaning of the parameters refer to the IPA Integration
     * Module documentation.
     *
     * @param projectName
     *          name of the project folder in IPA
     * @param datasetName
     *          name of the dataset saved in IPA (make sure it does not exist in
     *          {@code projectName})
     * @param analysisName
     *          name of the analysis saved in IPA (make sure it does not exist in
     *          {@code projectName})
     * @param geneIdType
     *          gene or protein identifier type (eg. {@code entrezgene},
     *          {@code affymetrix}, ...)
     * @param geneId
     *          gene or protein ids
     * @param expValueType
     *          expression value type (eg. {@code foldchange}, {@code pvalue}, ...
     * @param expValue
     *          expression values corresponding to the {@code geneId} entries
     * @param expValueType2
     *          expression value type of {@code expValue}
     * @param expValue2
     *          second expression values corresponding to the {@code geneId}
     *          entries
     * @param applicationName
     *          the name of the application calling the API (this will be saved in
     *          an analysis's metadata)
     */
    public synchronized void dataAnalysis(String projectName, String datasetName, String analysisName, String geneIdType, List<String> geneId,
            String expValueType, List<? extends Number> expValue, String expValueType2, List<? extends Number> expValue2, String applicationName)
                    throws IpaApiException {
        if (geneId == null) {
            throw new IllegalArgumentException("geneId cannot be null");
        }
        final String path = "/pa/api/v2/dataanalysis";
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        // this is needed to make sure no interaction is required
        postParameters.add(new BasicNameValuePair("ipaview", "projectmanager"));
        if (projectName != null) {
            postParameters.add(new BasicNameValuePair("projectname", projectName));
        }
        if (datasetName != null) {
            postParameters.add(new BasicNameValuePair("datasetname", datasetName));
        }
        if (analysisName != null) {
            postParameters.add(new BasicNameValuePair("analysisname", analysisName));
        }
        if (geneIdType != null) {
            postParameters.add(new BasicNameValuePair("geneidtype", geneIdType));
        }
        for (String val : geneId) {
            postParameters.add(new BasicNameValuePair("geneid", val));
        }
        if (expValueType != null) {
            postParameters.add(new BasicNameValuePair("expvaltype", expValueType));
        }
        for (Number val : expValue) {
            postParameters.add(new BasicNameValuePair("expvalue", val.toString()));
        }
        if (expValueType2 != null) {
            postParameters.add(new BasicNameValuePair("expvaltype2", expValueType2));
        }
        for (Number val : expValue2) {
            postParameters.add(new BasicNameValuePair("expval2", val.toString().toString()));
        }
        if (applicationName != null) {
            postParameters.add(new BasicNameValuePair("applicationname", applicationName));
        }

        try {
            URI uri = new URIBuilder(serverUrl).setPath(path).build();

            CloseableHttpClient hc = this.authenticatedClient();
            HttpClientContext context = this.clientContext();
            HttpPost httppost = new HttpPost(uri);
            httppost.setEntity(new UrlEncodedFormEntity(postParameters));
            try (CloseableHttpResponse response = hc.execute(httppost, context)) {
                log.info("post " + uri + " status " + response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        log.warn("dataAnalysis http status not ok, content: "+EntityUtils.toString(entity));
                    }
                    throw new IpaApiHttpResponseException(uri,response);
                }
                HttpEntity entity = response.getEntity();
                // boolean gotJnlp = false;
                try (InputStreamReader isr = new InputStreamReader(entity.getContent());
                        BufferedReader reader = new BufferedReader(isr)) {
                    String contentLine = null;
                    // FIXME check for jnlp? final Pattern jnlpRegex = Pattern.compile(".*<jnlp.*");
                    while ((contentLine = reader.readLine()) != null) {
                        IpaErrorIndication err = containsStringIndicatingError(contentLine);
                        if (err != null) {
                            throw new IpaApiException(parseContentForError(reader, err).toString());
                        }

                        // Matcher m = jnlpRegex.matcher(str);
                        // if (m.find())
                        // gotJnlp = true;
                    }
                }
            }
        } catch (URISyntaxException|IOException e) {
            throw new IpaApiException(e);
        }
    }

    /**
     * Returns the analysis name to id mapping for all analyses of a project with
     * the given id.
     * <p>
     * All analyses of the project with the id {@code projectId} are exported and
     * their name to id mapping is returned.
     *
     * @param projectId
     *          the project id, can be found by right-clicking on a project in the
     *          IPA client
     * @return the mapping from analysis name to id
     */
    public synchronized Map<String, String> analysisNameToIdForProjectId(String projectId) throws IpaApiException {
        List<IpaAnalysisResults> ipaSettings = null;
        ArrayList<String> resultsTypes = new ArrayList<String>();
        resultsTypes.add("settings");
        try (CloseableHttpResponse rawAnalyses = exportRawAnalyses(projectId, new ArrayList<String>(), resultsTypes)) {
            HttpEntity entity = rawAnalyses.getEntity();
            if (entity == null) {
                throw new IpaApiEntityNullException();
            }
            ipaSettings = parseExport(entity.getContent());
        } catch (IOException e) {
            throw new IpaApiException(e);
        }

        Map<String, String> anameToId = new HashMap<String, String>();
        for (IpaAnalysisResults analysis : ipaSettings) {
            anameToId.put(analysis.getAnalysisName(), analysis.getAnalysisId());
        }
        return anameToId;
    }

    /**
     * Returns the analysis name to id mapping for all analyses of a project with
     * the given name.
     * <p>
     * All analyses of the project with the name {@code projectName} are exported
     * and their name to id mapping is returned.
     * <p>
     * You should use {@link analysisNameToIdForProjectId} if possible because it
     * is faster, but currently there is now way to programmatically retrieve the
     * project id from its name.
     *
     * @param projectName
     *          the project name
     * @return the mapping from analysis name to id
     */
    public synchronized Map<String, String> analysisNameToIdForProjectName(String projectName) throws IpaApiException {
        return this.analysisNameToIdForProjectName(projectName, "*");
    }

    /**
     * Returns the analysis name to id mapping for all analyses matching
     * {@code query} of a project with the given name.
     * <p>
     * All analyses with a name matching {code query} of the project with the name
     * {@code projectName} are exported and their name to id mapping is returned.
     *
     * @param projectName
     *          the project name
     * @param query
     *          query which analysis names should match
     * @return the mapping from analysis name to id
     */
    public synchronized Map<String, String> analysisNameToIdForProjectName(String projectName, String query)
            throws IpaApiException {
        Map<String, String> anameToId = new HashMap<String, String>();
        // first perform a search that retrieves all analysis ids of all projects
        List<String> analysisIds = this.searchAnalysis(query);
        if (analysisIds.size() == 0) {
            return anameToId;
        }
        // get detailed information about all analyses
        List<IpaAnalysisResults> ipaSettings = null;
        ArrayList<String> resultsTypes = new ArrayList<String>();
        resultsTypes.add("settings");
        try (CloseableHttpResponse rawAnalyses = exportRawAnalyses(null, analysisIds, resultsTypes)) {
            HttpEntity entity = rawAnalyses.getEntity();
            if (entity == null) {
                throw new IpaApiEntityNullException();
            }
            ipaSettings = parseExport(entity.getContent());
        } catch (IOException e) {
            throw new IpaApiException(e);
        }

        // filter by project name and populate the map
        for (IpaAnalysisResults analysis : ipaSettings) {
            if (analysis.getProjectName().equals(projectName)) {
                // FIXME add assertion analysisName does not exist in Map (should
                // never happen)
                anameToId.put(analysis.getAnalysisName(), analysis.getAnalysisId());
            }
        }
        return anameToId;
    }

    /**
     * Parses analysis results and returns it as List of
     * {@code IpaAnalysisResults}.
     *
     * @param is
     *          the input stream, usually the content of a http response
     * @return the results
     */
    public static List<IpaAnalysisResults> parseExport(InputStream is) throws IpaApiException {
        // the different sections we are expecting
        final List<String> sections = Collections.unmodifiableList(
                Arrays.asList(
                        "Analysis Details", "Canonical Pathways", "Upstream Regulators", "Causal Networks", "Causal Networks", "Diseases and Bio Functions",
                        "Tox Functions", "Regulator Effects", "Networks", "My Lists", "Tox Lists", "My Pathways", "Analysis Ready Molecules"));
        // these sections contain a table
        final List<String> tableSections = Collections.unmodifiableList(
                Arrays.asList("Canonical Pathways", "Upstream Regulators", "Diseases and Bio Functions", "Tox Functions", "Networks", "Analysis Ready Molecules"));
        // the columns which should be marked as numeric
        final List<String> numericColumns = Collections.unmodifiableList(Arrays.asList("-log(p-value)", "zScore",
                "Ratio", "Exp Log Ratio", "Exp Fold Change", "Activation z-score", "Bias Term",
                "Bias-corrected z-score", "p-value of overlap", "p-Value", "# Molecules", "Score", "Focus Molecules",
                "Exp p-value"));
        // the regular expression denoting the beginning of a new section
        final Pattern secRgx = Pattern.compile("^(" + StringUtils.join(sections, "|") + ") for (.*)->(.*)->(.*)");

        List<IpaAnalysisResults> ipaResults = new ArrayList<IpaAnalysisResults>();
        try {
            try (InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr)) {
                IpaAnalysisResults currentIpaResults = new IpaAnalysisResults();
                currentIpaResults.setAnalysisName("");
                String currentSection = "";
                String currentAnalysisId = null;
                SimpleTable currentTable = new SimpleTable();
                String currentLine;
                int sectionLine = 0;

                while ((currentLine = reader.readLine()) != null) {
                    // FIXME if there is stored something like `<html` in the table
                    // this will throw
                    IpaErrorIndication err = containsStringIndicatingError(currentLine);
                    if (err != null) {
                        throw new IpaApiException(parseContentForError(reader, err).toString());
                    }

                    // skip empty lines
                    if (currentLine.isEmpty()) {
                        continue;
                    }

                    String[] parts = currentLine.split("\t");
                    Matcher m = secRgx.matcher(parts[0]);
                    if (m.find()) {
                        String newSectionName = m.group(1);
                        String newSectionWorkspace = m.group(2);
                        String newSectionProjectName = m.group(3);
                        String newSectionAnalysisName = m.group(4);
                        // this is the beginning of a new section
                        if (newSectionAnalysisName.length() == 0) {
                            throw new IpaApiParserException("no analysis name");
                        }

                        // store the old section if there is one
                        if (currentTable.isInitialised()) {
                            currentTable.setName(currentSection);
                            // remove duplicated information from table (Analysis
                            // Name is constant)
                            if (currentTable.header().get(0).equals("Analysis") || currentTable.header().get(1).equals("Analysis")) {
                                currentTable.deleteColumn("Analysis");
                            }
                            currentIpaResults.getTables().put(currentTable.getName(), currentTable);
                        }

                        if (!newSectionAnalysisName.equals(currentIpaResults.getAnalysisName())) {
                            // this is a new analysis, store the old one
                            if (currentIpaResults.getAnalysisName().length() > 0) {
                                if (currentAnalysisId == null || currentAnalysisId.length() == 0) {
                                    throw new IpaApiParserException("no analysis ID");
                                }
                                currentIpaResults.setAnalysisId(currentAnalysisId);
                                ipaResults.add(currentIpaResults);
                            }
                            currentAnalysisId = null;
                            currentIpaResults = new IpaAnalysisResults();
                            currentIpaResults.setWorkspace(newSectionWorkspace);
                            currentIpaResults.setProjectName(newSectionProjectName);
                            currentIpaResults.setAnalysisName(newSectionAnalysisName);
                        }
                        sectionLine = 0;
                        currentSection = newSectionName;
                        currentTable = new SimpleTable();
                        if (!currentIpaResults.getWorkspace().equals(newSectionWorkspace)
                                || !currentIpaResults.getProjectName().equals(newSectionProjectName)
                                || !currentIpaResults.getAnalysisName().equals(newSectionAnalysisName)) {
                            throw new IpaApiParserException("table metadata mismatch: " + "current("
                                    + currentIpaResults.getWorkspace() + "," + currentIpaResults.getProjectName() + ","
                                    + currentIpaResults.getAnalysisName() + "), " + "new(" + newSectionWorkspace + ","
                                    + newSectionProjectName + "," + newSectionAnalysisName + ")");
                        }
                    } else {
                        sectionLine += 1;
                        if (sectionLine == 1) {
                            if (tableSections.contains(currentSection)) {
                                // this is the header of a new table; remove
                                // trailing empty column name
                                List<String> header = new ArrayList<String>();
                                for (String p : parts) {
                                    header.add(p.trim());
                                }
                                if (header.get(header.size() - 1).length() == 0) {
                                    header.remove(header.size() - 1);
                                }
                                currentTable.setHeader(header);
                                List<SimpleColumnType> columnTypes = new ArrayList<SimpleColumnType>();
                                for (String c : header) {
                                    if (numericColumns.contains(c)) {
                                        columnTypes.add(SimpleColumnType.NUMERIC);
                                    } else {
                                        columnTypes.add(SimpleColumnType.STRING);
                                    }
                                }
                                currentTable.setColumnTypes(columnTypes);
                            }
                        } else {
                            if (tableSections.contains(currentSection)) {
                                if (currentTable.header().size() > parts.length) {
                                    throw new IpaApiParserException("could not parse table (expected " + currentTable.header().size() + " elements, got " + parts.length);
                                }
                                List<String> row = new ArrayList<String>(Arrays.asList(parts));
                                // beautify certain columns: delete trailing comma, add whitespace after comma
                                for (int i = 0; i < currentTable.header().size(); ++i) {
                                    if (currentTable.header().get(i).equals("Molecules")
                                            || currentTable.header().get(i).equals("Molecules in Network")
                                            || currentTable.header().get(i).equals("Target molecules in dataset")) {
                                        row.set(i, row.get(i).replaceAll(",$", ""));
                                        row.set(i, row.get(i).replaceAll(",([^\\s])", ", $1"));
                                    }
                                }
                                currentTable.addRow(row);
                            } else if (currentSection.equals("Analysis Details")) {
                                if (parts.length == 2) {
                                    if (parts[0].equals("Analysis ID")) {
                                        currentAnalysisId = parts[1];
                                    }
                                }
                            }
                        }
                    }
                }
                // store the last section
                if (currentTable.isInitialised()) {
                    currentTable.setName(currentSection);
                    // remove duplicated information from table (Analysis Name is
                    // constant)
                    if (currentTable.header().get(0).equals("Analysis")
                            || currentTable.header().get(1).equals("Analysis")) {
                        currentTable.deleteColumn("Analysis");
                    }
                    currentIpaResults.getTables().put(currentTable.getName(), currentTable);
                }

                // store the last analysis
                if (currentIpaResults.getAnalysisName().length() > 0) {
                    if (currentAnalysisId == null || currentAnalysisId.length() == 0) {
                        throw new IpaApiParserException("no analysis ID");
                    }
                    currentIpaResults.setAnalysisId(currentAnalysisId);
                    ipaResults.add(currentIpaResults);
                }
            }
        } catch (IOException e) {
            throw new IpaApiException(e);
        }

        return ipaResults;
    }
}
