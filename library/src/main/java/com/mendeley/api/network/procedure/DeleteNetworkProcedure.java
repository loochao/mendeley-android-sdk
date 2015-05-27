package com.mendeley.api.network.procedure;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;

import java.io.IOException;
import java.text.ParseException;

import static com.mendeley.api.network.NetworkUtils.getConnection;

/**
 * A NetworkProcedure specialised for making HTTP DELETE requests.
 */
public class DeleteNetworkProcedure extends NetworkProcedure<Void> {
    private final String url;

    public DeleteNetworkProcedure(String url, AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.url = url;
    }

    @Override
    protected int getExpectedResponse() {
        return 204;
    }

    @Override
    protected Void run() throws MendeleyException {
        try {
            con = getConnection(url, "DELETE", authenticationManager);
            con.connect();

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            }
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url);
        } catch (IOException e) {
            throw new MendeleyException("Could not perform DELETE request", e);
        } finally {
            closeConnection();
        }
        return null;
    }
}
