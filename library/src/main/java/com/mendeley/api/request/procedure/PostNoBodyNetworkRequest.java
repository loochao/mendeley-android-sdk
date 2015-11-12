package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;

import java.io.IOException;
import java.text.ParseException;

import static com.mendeley.api.request.NetworkUtils.createGetConnectionWithMendeleyAuthToken;

// FIXME get rid of this class by using an inputstream instead of a String to post data
/**
 * A NetworkProcedure specialised for making HTTP POST requests with no message body or response.
 */
public class PostNoBodyNetworkRequest extends NetworkRequest<Void> {
    private final String url;

    public PostNoBodyNetworkRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
    }

    protected int getExpectedResponse() {
        return 204;
    }

    @Override
    protected RequestResponse<Void> doRun() throws MendeleyException {
        try {
            con = createGetConnectionWithMendeleyAuthToken(url, "POST", authTokenManager);
            con.connect();

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
        } finally {
            closeConnection();
        }

    }
}
