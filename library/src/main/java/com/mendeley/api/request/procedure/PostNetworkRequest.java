package com.mendeley.api.request.procedure;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

public abstract class PostNetworkRequest<ResultType> extends HttpUrlConnectionRequest<ResultType> {

    public PostNetworkRequest(String url, String contentType, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(url, contentType, authTokenManager, clientCredentials);
    }


//    @Override
//    protected RequestResponse<ResultType> doRun() throws MendeleyException {
//        try {
//            con = createGetConnectionWithMendeleyAuthTokenInHeader(url, "POST", authTokenManager);
//            con.addRequestProperty("Content-type", contentType);
//
//            //TODO: do we need this?
////            con.setFixedLengthStreamingMode(json.getBytes().length);
//            con.connect();
//
//            os = con.getOutputStream();
//            writePostBody(os);
//            os.close();
//
//            getResponseHeaders();
//
//            final int responseCode = con.getResponseCode();
//            if (responseCode != getExpectedResponse()) {
//                throw HttpResponseException.create(con);
//            } else {
//                is = con.getInputStream();
//                String responseString = readInputStream(is);
//                return new RequestResponse<ResultType>(parseJsonString(responseString), serverDate);
//            }
//        } catch (ParseException pe) {
//            throw new JsonParsingException("Could not parse web API headers for " + url, pe);
//        } catch (IOException e) {
//            throw new MendeleyException(e.getMessage(), e);
//        } catch (JSONException e) {
//            throw new JsonParsingException(e.getMessage(), e);
//        } catch (Exception e) {
//            throw new MendeleyException(e.getMessage(), e);
//        } finally {
//            closeConnection();
//        }
//    }


    @Override
    protected HttpsURLConnection createConnection(Uri uri) throws IOException {
        HttpsURLConnection connection = NetworkUtils.createHttpsGetConnection(uri.toString(), "POST");
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(0);
        return connection;
    }

    @Override
    protected void onConnected(HttpURLConnection con) throws Exception {
        final OutputStream os = con.getOutputStream();
        writePostBody(os);
        os.close();
    }


    /**
     * @return writes the data to post in the body of the request
     * @throws JSONException
     */
    protected abstract void writePostBody(OutputStream os) throws Exception;

}
