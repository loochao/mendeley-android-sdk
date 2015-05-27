package com.mendeley.api.network.task;

import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;

import java.io.IOException;

import static com.mendeley.api.network.NetworkUtils.getConnection;

/**
 * A NetworkTask specialised for making HTTP DELETE requests.
 */
public abstract class DeleteNetworkTask extends NetworkTask {
    @Override
    protected int getExpectedResponse() {
        return 204;
    }

    @Override
    protected MendeleyException doInBackground(String... params) {
        String url = params[0];

        try {
            con = getConnection(url, "DELETE", getAccessTokenProvider());
            con.connect();

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                return HttpResponseException.create(con);
            } else {
                return null;
            }
        } catch (IOException e) {
            return new MendeleyException(e.getMessage(), e);
        } finally {
            closeConnection();
        }
    }
}
