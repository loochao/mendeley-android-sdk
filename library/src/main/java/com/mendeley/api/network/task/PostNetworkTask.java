package com.mendeley.api.network.task;

import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;

import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.mendeley.api.network.NetworkUtils.getConnection;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

public abstract class PostNetworkTask extends NetworkTask {
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

                is = con.getInputStream();
                String responseString = readInputStream(is);

                processJsonString(responseString);

                return null;
            }

        } catch (JSONException e) {
            return new JsonParsingException(e.getMessage());
        } catch (IOException e) {
            return new MendeleyException(e.getMessage());
        } finally {
            closeConnection();
        }
    }

    protected abstract void processJsonString(String jsonString) throws JSONException;

    protected abstract String getContentType();
}
