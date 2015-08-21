package com.mendeley.api.network.procedure;

import android.util.Log;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.impl.BaseMendeleySdk;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;

import static com.mendeley.api.network.NetworkUtils.getConnection;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

/**
 * A NetworkProcedure specialised for making HTTP GET requests.
 */
public abstract class GetNetworkProcedure<ResultType> extends NetworkProcedure<ResultType> {

    private static final String TAG = BaseMendeleySdk.TAG;

    private final String url;
    private final String contentType;

    protected GetNetworkProcedure(String url, String contentType,
                                  AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.url = url;
        this.contentType = contentType;
    }

    @Override
    protected int getExpectedResponse() {
        return 200;
    }

    public ResultType run() throws MendeleyException {
        return run(0);
    }

    private ResultType run(final int currentRetry) throws MendeleyException {
        String responseString = null;
        try {
            con = getConnection(url, "GET", authenticationManager);
            con.addRequestProperty("Content-type", contentType);
            con.connect();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw  HttpResponseException.create(con);
            }

            getResponseHeaders();
            is = con.getInputStream();
            responseString = readInputStream(is);
            return processJsonString(responseString);
        } catch (MendeleyException me) {
            throw me;
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url, pe);
        } catch (IOException ioe) {
            // If the issue is due to IOException, retry up to MAX_HTTP_RETRIES times
            if (currentRetry <  BaseMendeleySdk.MAX_HTTP_RETRIES) {
                Log.w(TAG, "Problem connecting to " + url + ": " + ioe.getMessage() + ". Retrying (" + (currentRetry + 1) + "/" + BaseMendeleySdk.MAX_HTTP_RETRIES + ")");
                return run(currentRetry + 1);
            } else {
                throw new MendeleyException("IO error in GET request " + url + ": " + ioe.toString(), ioe);
            }
        } catch (JSONException e) {
            throw new JsonParsingException("Passing error in GET request " + url + ": " + e.toString() + ". Response was: " + responseString, e);
        } catch (Exception e) {
            throw new MendeleyException("Error in GET request " + url + ": " + e.toString(), e);
        } finally {
            closeConnection();
        }
    }

    protected abstract ResultType processJsonString(String jsonString) throws JSONException, ParseException;
}
