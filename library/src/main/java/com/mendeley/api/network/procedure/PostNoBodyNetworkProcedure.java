package com.mendeley.api.network.procedure;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;

import java.io.IOException;
import java.text.ParseException;

import static com.mendeley.api.network.NetworkUtils.getConnection;

/**
 * A NetworkProcedure specialised for making HTTP POST requests with no message body or response.
 */
public class PostNoBodyNetworkProcedure extends NetworkProcedure<Void> {
    private final String url;

    public PostNoBodyNetworkProcedure(String url, AuthenticationManager authenticationManager) {
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
            con = getConnection(url, "POST", authenticationManager);
            con.connect();

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            }
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url, pe);
        } catch (IOException e) {
            throw new MendeleyException("Could not make POST request", e);
        } finally {
            closeConnection();
        }
        return null;
    }
}
