package com.mendeley.api.request;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.util.DateUtils;
import com.mendeley.api.util.Utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

// TODO try to eliminate Apache HTTP
public abstract class PatchAuthorizedRequest<ResultType> extends AuthorizedRequest<ResultType> {
    private final String url;
    private final String date;

    public PatchAuthorizedRequest(String url, String date, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
        this.date = date;
    }

    @Override
    protected RequestResponse<ResultType> doRun() throws MendeleyException {
        final HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, READ_TIMEOUT);
        final HttpClient httpclient = new DefaultHttpClient(httpParameters);

        final HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.setHeader("Authorization", "Bearer " + authTokenManager.getAccessToken());
        if (date != null) {
            httpPatch.setHeader("If-Unmodified-Since", date);
        }

        final Map<String, String> requestHeaders = new HashMap<>();
        appendHeaders(requestHeaders);
        for (String key: requestHeaders.keySet()) {
            httpPatch.setHeader(key, requestHeaders.get(key));
        }

        InputStream is = null;
        try {
            httpPatch.setEntity(createPatchingEntity());

            HttpResponse response = httpclient.execute(httpPatch);

            final int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode < 200 && responseCode >= 300) {
                String responseString = null;
                try {
                    responseString = NetworkUtils.readInputStream(response.getEntity().getContent());
                } catch (IOException ignored) {
                }
                throw new HttpResponseException(responseCode, response.getStatusLine().getReasonPhrase(), url, responseString);
            }

            final HttpEntity responseEntity = response.getEntity();
            return new RequestResponse<>(manageResponse(responseEntity.getContent()), getServerDate(response));

        } catch (JSONException e) {
            throw new JsonParsingException("Error parsing model to patch", e);
        } catch (Exception e) {
            throw new MendeleyException(e.getMessage(), e);
        } finally {
            Utils.closeQuietly(is);
        }
    }

    private Date getServerDate(HttpResponse response) throws ParseException {
        final Header date = response.getFirstHeader("Date");
        if (date == null) {
            return null;
        }
        return DateUtils.parseDateInHeader(date.getValue());
    }

    protected void appendHeaders(Map<String, String> headers) {
    }

    protected abstract HttpEntity createPatchingEntity() throws Exception;

    protected abstract ResultType manageResponse(InputStream is) throws Exception;


    /**
     * Extends HttpEntityEnclosingRequestBase to provide PATCH request method.
     */
    static class HttpPatch extends HttpEntityEnclosingRequestBase {
        public final static String METHOD_NAME = "PATCH";

        public HttpPatch() {
            super();
        }

        public HttpPatch(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpPatch(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }
}
