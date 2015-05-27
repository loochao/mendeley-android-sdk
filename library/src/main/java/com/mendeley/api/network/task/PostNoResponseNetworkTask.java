package com.mendeley.api.network.task;

import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.mendeley.api.network.NetworkUtils.getConnection;

public abstract class PostNoResponseNetworkTask extends NetworkTask {
    @Override
    protected int getExpectedResponse() {
        return 201;
    }

    @Override
    protected MendeleyException doInBackground(String... params) {
        String url = params[0];
        String jsonString = params[1];

        try {
            con = getConnection(url, "POST", getAccessTokenProvider());
            con.addRequestProperty("Content-type", getContentType());
            con.setFixedLengthStreamingMode(jsonString.getBytes().length);
            con.connect();

            os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonString);
            writer.flush();
            writer.close();
            os.close();

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

    protected abstract String getContentType();
}
