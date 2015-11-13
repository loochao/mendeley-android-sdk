package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.request.NetworkUtils;
import com.mendeley.api.util.Utils;

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
import java.io.InputStream;

import static com.mendeley.api.request.NetworkUtils.getHttpPatch;
import static com.mendeley.api.request.NetworkUtils.readInputStream;

// TODO try to eliminate Apache HTTP
public abstract class PatchNetworkRequest<ResultType> extends Request<ResultType> {
    private final String url;
    private final String contentType;
    private final String date;

    public PatchNetworkRequest(String url, String contentType, String date, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
        this.contentType = contentType;
        this.date = date;
    }


    protected int getExpectedResponse() {
        return 200;
    }

    @Override
    protected RequestResponse<ResultType> doRun() throws MendeleyException {
        final HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, NetworkUtils.CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, NetworkUtils.READ_TIMEOUT);
        HttpClient httpclient = new DefaultHttpClient(httpParameters);

        NetworkUtils.HttpPatch httpPatch = getHttpPatch(url, date, contentType, authTokenManager);

        InputStream is = null;
        try {
            final String json = obtainJsonToPost();
            httpPatch.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse response = httpclient.execute(httpPatch);

            final int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(response, url);
            } else {
                HttpEntity responseEntity = response.getEntity();
                is = responseEntity.getContent();
                String responseString = readInputStream(is);
                // TODO: get the server date
                return new RequestResponse<ResultType>(processJsonString(responseString), null);
            }
        } catch (IOException e) {
            throw new JsonParsingException(e.getMessage(), e);
        } catch (JSONException e) {
            throw new MendeleyException(e.getMessage(), e);
        } finally {
            Utils.closeQuietly(is);
        }
    }

    protected abstract String obtainJsonToPost() throws JSONException;

    protected abstract ResultType processJsonString(String jsonString) throws JSONException, IOException;
}
