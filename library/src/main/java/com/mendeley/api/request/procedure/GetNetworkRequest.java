package com.mendeley.api.request.procedure;

import android.util.Log;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import static com.mendeley.api.request.NetworkUtils.getConnection;

/**
 * A NetworkProcedure specialised for making HTTP GET requests.
 */
public abstract class GetNetworkRequest<ResultType> extends NetworkRequest<ResultType> {

    private final String url;
    private final String contentType;

    protected GetNetworkRequest(String url, String contentType, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
        this.contentType = contentType;
    }

    @Override
    protected int getExpectedResponse() {
        return 200;
    }

    @Override
    public RequestResponse<ResultType> doRun() throws MendeleyException {
        return run(0);
    }

    private RequestResponse<ResultType> run(final int currentRetry) throws MendeleyException {
        try {
            con = getConnection(url, "GET", authTokenManager);
            con.addRequestProperty("Content-type", contentType);
            con.connect();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw  HttpResponseException.create(con);
            }

            getResponseHeaders();
            is = con.getInputStream();
            return new RequestResponse(manageResponse(is), serverDate, next);
        } catch (MendeleyException me) {
            throw me;
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url, pe);
        } catch (IOException ioe) {
            // If the issue is due to IOException, retry up to MAX_HTTP_RETRIES times
            if (currentRetry <  MAX_HTTP_RETRIES) {
                Log.w(TAG, "Problem connecting to " + url + ": " + ioe.getMessage() + ". Retrying (" + (currentRetry + 1) + "/" + MAX_HTTP_RETRIES + ")");
                return run(currentRetry + 1);
            } else {
                throw new MendeleyException("IO error in GET request " + url + ": " + ioe.toString(), ioe);
            }
        } catch (JSONException e) {
            throw new JsonParsingException("Passing error in GET request " + url + ": " + e.toString(), e);
        } catch (Exception e) {
            throw new MendeleyException("Error in GET request " + url + ": " + e.toString(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            closeConnection();
        }
    }

    protected abstract ResultType manageResponse(InputStream is) throws JSONException, ParseException, IOException;
}
