package com.mendeley.api.network.task;

import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.network.NetworkUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.IOException;

import static com.mendeley.api.network.NetworkUtils.getHttpPatch;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

public abstract class PatchNetworkTask extends NetworkTask {
    @Override
    protected int getExpectedResponse() {
        return 200;
    }

    @Override
    protected MendeleyException doInBackground(String... params) {
        String url = params[0];
        String jsonString = params[1];

        HttpClient httpclient = new DefaultHttpClient();
        NetworkUtils.HttpPatch httpPatch = getHttpPatch(url, getDate(), getContentType(), getAccessTokenProvider());

        try {
            httpPatch.setEntity(new StringEntity(jsonString, "UTF-8"));
            HttpResponse response = httpclient.execute(httpPatch);

            final int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != getExpectedResponse()) {
                return HttpResponseException.create(response, url);
            } else {
                HttpEntity responseEntity = response.getEntity();
                is = responseEntity.getContent();
                String responseString = readInputStream(is);
                processJsonString(responseString);
                return null;
            }
        } catch (IOException e) {
            return new JsonParsingException(e.getMessage());
        } catch (JSONException e) {
            return new JsonParsingException(e.getMessage());
        } finally {
            closeConnection();
        }
    }

    protected abstract void processJsonString(String jsonString) throws JSONException;

    protected abstract String getDate();

    protected abstract String getContentType();
}
