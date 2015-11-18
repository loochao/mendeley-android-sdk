package com.mendeley.api.request;

import android.net.Uri;
import android.util.Log;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.request.params.Page;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Base class for all synchronous network calls.
 */
public abstract class HttpUrlConnectionAuthorizedRequest<ResultType> extends AuthorizedRequest<ResultType> {

    static {
        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());
    }

    private final Uri url;
    private RequestProgressListener progressListener;

    public HttpUrlConnectionAuthorizedRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
    }

    @Override
    public final Response<ResultType> doRun() throws MendeleyException {
        return doRun(url, 0, true);
    }

    private Response<ResultType> doRun(Uri uri, int currentRetry, boolean addOauthToken) throws MendeleyException {

        InputStream is = null;
        HttpURLConnection con = null;

        try {
            con = createConnection(uri);

            // the redirection in implemented by us
            con.setInstanceFollowRedirects(false);

            if (addOauthToken) {
                con.addRequestProperty("Authorization", "Bearer " + authTokenManager.getAccessToken());
            }

            final Map<String, String> requestHeaders = new HashMap<String, String>();
            appendHeaders(requestHeaders);
            for (String key: requestHeaders.keySet()) {
                con.addRequestProperty(key, requestHeaders.get(key));
            }

            con.connect();

            onConnected(con);

            final int responseCode = con.getResponseCode();

            // Implementation of HTTP redirection.
            if (responseCode >= 300 && responseCode < 400) {
                return followRedirection(con);
            }

            if (responseCode < 200 && responseCode >= 300) {
                String responseString = null;
                try {
                    responseString = NetworkUtils.readInputStream(con.getInputStream());
                } catch (IOException ignored) {}

                throw new HttpResponseException(responseCode, con.getResponseMessage(), url, responseString);
            }

            // wrapping the input stream of the connection in one ProgressPublisherInputStream
            // to publish progress as the file is being read
            is = new MyProgressPublisherInputStream(con.getInputStream(), con.getContentLength());

            final Map<String, List<String>> responseHeaders = con.getHeaderFields();
            return new Response<>(manageResponse(is), getServerDateString(responseHeaders), getNextPage(responseHeaders));

        } catch (MendeleyException me) {
            throw me;
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url, pe);
        } catch (IOException ioe) {
            // If the issue is due to IOException, retry up to MAX_HTTP_RETRIES times
            if (currentRetry <  MAX_HTTP_RETRIES) {
                Log.w(TAG, "Problem connecting to " + url+ ". Retrying (" + (currentRetry + 1) + "/" + MAX_HTTP_RETRIES + ")");
                return doRun(uri, currentRetry + 1, addOauthToken);
            } else {
                throw new MendeleyException("IO error in GET request " + url, ioe);
            }
        } catch (Exception e) {
            throw new MendeleyException("Error in GET request " + url, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /*
     * We implement the redirection by hand because:
     * - we don't want to send the auth token in the query string, but as a HTTP header
     * - if redirected to Amazon or other server, we don't want to forward the Mendeley auth jeader
     * - ... but the redirection that HttpUrlConnection does forwards the header
     */
    private Response<ResultType> followRedirection(HttpURLConnection con) throws MendeleyException {
        final Uri redirectionUri = Uri.parse(con.getHeaderField("location"));
        final boolean addOauthToken = redirectionUri.getHost().equals(Uri.parse(MENDELEY_API_BASE_URL).getHost());
        return doRun(redirectionUri, 0, addOauthToken);
    }

    /**
     * Sets a listener to be notified of progress
     * @param progressListener
     */
    public final void setProgressListener(GetAuthorizedRequest.RequestProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    protected void appendHeaders(Map<String, String> headers) {
    }

    protected abstract HttpURLConnection createConnection(Uri uri) throws IOException;

    protected abstract void onConnected(HttpURLConnection con) throws Exception;

    protected abstract ResultType manageResponse(InputStream is) throws Exception;


    private String getServerDateString(Map<String, List<String>> headersMap) throws IOException {
        final List<String> dateHeaders = headersMap.get("Date");
        if (dateHeaders != null) {
            return headersMap.get("Date").get(0);
        }
        return null;
    }

    private Page getNextPage(Map<String, List<String>> responseHeaders) {
        final List<String> links = responseHeaders.get("Link");
        if (links != null) {
            for (String link : links) {
                try {
                    String linkString = link.substring(link.indexOf("<") + 1, link.indexOf(">"));
                    if (link.contains("next")) {
                        return new Page(linkString);
                    }
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * Implementation of {@link ProgressPublisherInputStream} that pipes the progress to the
     * {@link RequestProgressListener}
     */
    private class MyProgressPublisherInputStream extends ProgressPublisherInputStream {
        public MyProgressPublisherInputStream(InputStream inputStream, int contentLength) {
            super(inputStream, contentLength);
        }

        @Override
        protected void onProgress(int progress) {
            if (progressListener != null) {
                progressListener.onProgress(progress);
            }
        }
    }

    /**
     * To be implemented by classes that want to listen the progress of the download
     */
    public interface  RequestProgressListener {
        /**
         * @param progress in [0-100]
         */
        void onProgress(int progress);
    }
}
