package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;

import java.io.IOException;
import java.text.ParseException;

import static com.mendeley.api.request.NetworkUtils.createGetConnectionWithMendeleyAuthToken;

/**
 * A NetworkProcedure specialised for making HTTP DELETE requests.
 */
public class DeleteNetworkRequest extends NetworkRequest<Void> {
    private final String url;

    public DeleteNetworkRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
    }


    protected int getExpectedResponse() {
        return 204;
    }

    @Override
    protected RequestResponse<Void> doRun() throws MendeleyException {
        try {
            con = createGetConnectionWithMendeleyAuthToken(url, "DELETE", authTokenManager);
            con.connect();

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            }

            return new RequestResponse(null, serverDate);
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url, pe);
        } catch (IOException e) {
            throw new MendeleyException("Could not perform DELETE request", e);
        } finally {
            closeConnection();
        }
    }
}
