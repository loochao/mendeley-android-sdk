package com.mendeley.api.request;

import android.util.Log;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.request.procedure.NetworkRequest;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import static com.mendeley.api.request.NetworkUtils.createGetConnectionWithMendeleyAuthToken;
import static com.mendeley.api.request.NetworkUtils.createGetConnection;

/**
 * A NetworkProcedure specialised for making HTTP GET requests.
 */
public abstract class GetNetworkRequest<ResultType> extends NetworkRequest<ResultType> {

    private final String url;
    private final String contentType;
    private RequestProgressListener progressListener;

    protected GetNetworkRequest(String url, String contentType, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
        this.contentType = contentType;
    }

    @Override
    public final RequestResponse<ResultType> doRun() throws MendeleyException {
        return doRun(0);
    }

    /**
     * Sets a listener to be notified of progress
     * @param progressListener
     */
    public final void setProgressListener(RequestProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    private RequestResponse<ResultType> doRun(final int currentRetry) throws MendeleyException {
        return doRun(currentRetry, url, true);
    }

    private RequestResponse<ResultType> doRun(final int currentRetry, String url, boolean addMendeleyAuthToken) throws MendeleyException {
        try {

            if (addMendeleyAuthToken) {
                con = createGetConnectionWithMendeleyAuthToken(url, "GET", authTokenManager);
            } else {
                con = createGetConnection(url, "GET");
            }

            if (contentType != null) {
                con.addRequestProperty("Content-type", contentType);
            }
            con.connect();

            final int responseCode = con.getResponseCode();

            if (responseCode >= 300 && responseCode < 400) {
                final String redirectionUrl = con.getHeaderField("location");
                return doRun(0, redirectionUrl, false);
            }

            if (responseCode < 200 && responseCode >= 300) {
                throw HttpResponseException.create(con);
            }

            getResponseHeaders();

            // wrapping the input stream of the connection in one ProgressPublisherInputStream
            // to publish progress as the file is being read
            is = new MyProgressPublisherInputStream(con.getInputStream(), con.getContentLength());
            return new RequestResponse(manageResponse(is), serverDate, next);
        } catch (MendeleyException me) {
            throw me;
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url, pe);
        } catch (IOException ioe) {
            // If the issue is due to IOException, retry up to MAX_HTTP_RETRIES times
            if (currentRetry <  MAX_HTTP_RETRIES) {
                Log.w(TAG, "Problem connecting to " + url + ": " + ioe.getMessage() + ". Retrying (" + (currentRetry + 1) + "/" + MAX_HTTP_RETRIES + ")");
                return doRun(currentRetry + 1);
            } else {
                throw new MendeleyException("IO error in GET request " + url + ": " + ioe.toString(), ioe);
            }
        } catch (Exception e) {
            throw new MendeleyException("Error in GET request " + url + ": " + e.toString(), e);
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

    protected abstract ResultType manageResponse(InputStream is) throws Exception;

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
