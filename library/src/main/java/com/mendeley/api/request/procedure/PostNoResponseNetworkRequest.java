package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;


import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;

import static com.mendeley.api.request.NetworkUtils.getConnection;

// FIXME: I'm not sure we need this class
public abstract class PostNoResponseNetworkRequest extends NetworkRequest<Void> {
    private final String url;
    private final String contentType;

    public PostNoResponseNetworkRequest(String url, String contentType,
                                        AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
        this.contentType = contentType;
    }

    @Override
    protected int getExpectedResponse() {
        return 201;
    }

    @Override
    protected RequestResponse<Void> doRun() throws MendeleyException {
        try {
            final String json = obtainJsonToPost();
            con = getConnection(url, "POST", authTokenManager);
            con.addRequestProperty("Content-type", contentType);
            con.setFixedLengthStreamingMode(json.getBytes().length);
            con.connect();

            os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(json);
            writer.flush();
            writer.close();
            os.close();

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            }

            return new RequestResponse<Void>(null, serverDate);
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url, pe);
        } catch (IOException e) {
            throw new MendeleyException("Could not make POST request", e);
        } catch (JSONException e) {
            throw new MendeleyException("Could not make POST request", e);
        } finally {
            closeConnection();
        }
    }

    protected abstract String obtainJsonToPost() throws JSONException;
}
