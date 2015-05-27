package com.mendeley.api.network.task;

import android.util.Log;

import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.exceptions.UserCancelledException;
import com.mendeley.api.impl.BaseMendeleySdk;

import org.json.JSONException;

import java.io.IOException;

import static com.mendeley.api.network.NetworkUtils.getConnection;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

/**
 * A NetworkTask specialised for making HTTP GET requests.
 */
public abstract class GetNetworkTask extends NetworkTask {
    private static final String TAG = BaseMendeleySdk.TAG;

    @Override
    protected int getExpectedResponse() {
        return 200;
    }

    @Override
    protected MendeleyException doInBackground(String... params) {
        try {
            executeRequest(params[0], 0);
            return null;
        } catch (MendeleyException me) {
            return me;
        }
    }

    private void executeRequest(final String url, final int currentRetry) throws MendeleyException {
        String responseBody = null;
        try {
            con = getConnection(url, "GET", getAccessTokenProvider());
            con.addRequestProperty("Content-type", getContentType());
            con.connect();

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            }

            if (isCancelled()) {
                throw new UserCancelledException();
            }

            is = con.getInputStream();
            responseBody = readInputStream(is);
            processJsonString(responseBody);
        } catch (MendeleyException me) {
            throw me;
        } catch (IOException ioe) {
            // If the issue is due to IOException, retry up to MAX_HTTP_RETRIES times
            // TODO: move this logic to the blocking version and delegate into it
            if (currentRetry <  BaseMendeleySdk.MAX_HTTP_RETRIES) {
                Log.w(TAG, "Problem connecting to " + url + ": " + ioe.getMessage() + ". Retrying (" + (currentRetry + 1) + "/" + BaseMendeleySdk.MAX_HTTP_RETRIES + ")");
                executeRequest(url, currentRetry + 1);
            } else {
                throw new MendeleyException("IO error in GET request " + url + ": " + ioe.toString(), ioe);
            }
        } catch (JSONException e) {
            throw new JsonParsingException("Pasing error in GET request " + url + ": " + e.toString() + ". Response was: " + responseBody, e);
        } catch (Exception e) {
            throw new MendeleyException("Error in GET request " + url + ": " + e.toString(), e);
        } finally {
            closeConnection();
        }
    }

    protected abstract void processJsonString(String jsonString) throws JSONException;

    protected abstract String getContentType();
}
