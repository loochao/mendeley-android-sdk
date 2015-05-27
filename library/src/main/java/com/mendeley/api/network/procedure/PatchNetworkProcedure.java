package com.mendeley.api.network.procedure;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.network.NetworkUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import java.io.IOException;

import static com.mendeley.api.network.NetworkUtils.getHttpPatch;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

public abstract class PatchNetworkProcedure<ResultType> extends NetworkProcedure<ResultType> {
    private final String url;
    private final String contentType;
    private final String json;
    private final String date;

    public PatchNetworkProcedure(String url, String contentType, String json, String date,
                                 AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.url = url;
        this.contentType = contentType;
        this.json = json;
        this.date = date;
    }

    @Override
    protected int getExpectedResponse() {
        return 200;
    }

    @Override
    protected ResultType run() throws MendeleyException {
        final HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, NetworkUtils.CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, NetworkUtils.READ_TIMEOUT);
        HttpClient httpclient = new DefaultHttpClient(httpParameters);

        NetworkUtils.HttpPatch httpPatch = getHttpPatch(url, date, contentType, authenticationManager);

        try {
            httpPatch.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse response = httpclient.execute(httpPatch);

            final int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(response, url);
            } else {
                HttpEntity responseEntity = response.getEntity();
                is = responseEntity.getContent();
                String responseString = readInputStream(is);
                return processJsonString(responseString);
            }
        } catch (IOException e) {
            throw new JsonParsingException(e.getMessage());
        } catch (JSONException e) {
            throw new MendeleyException(e.getMessage());
        } finally {
            closeConnection();
        }
    }

    protected abstract ResultType processJsonString(String jsonString) throws JSONException;
}
