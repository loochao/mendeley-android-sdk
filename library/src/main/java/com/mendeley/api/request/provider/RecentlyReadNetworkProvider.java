package com.mendeley.api.request.provider;

import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.procedure.GetNetworkRequest;
import com.mendeley.api.request.procedure.Request;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.List;

import static com.mendeley.api.request.NetworkUtils.API_URL;
import static com.mendeley.api.request.NetworkUtils.getConnection;

/**
 * NetworkProvider class for Recently read API calls
 */
public class RecentlyReadNetworkProvider {
	public static final String RECENTLY_READ_BASE_URL = API_URL + "recently_read";

    private final AuthTokenManager accessTokenProvider;

    public RecentlyReadNetworkProvider(AuthTokenManager accessTokenProvider) {
        this.accessTokenProvider = accessTokenProvider;
    }

    public static String getGetRecentlyReadUrl(String groupId, String fileId, int limit) {
        StringBuilder url = new StringBuilder();
        url.append(RECENTLY_READ_BASE_URL);

        boolean firstParam = true;

        // groupId
        if (groupId != null) {
            url.append(firstParam ? "?" : "&").append("groupId=" + groupId);
            firstParam = false;
        }
        // fileId
        if (fileId != null) {
            url.append(firstParam ? "?" : "&").append("fileId=" + fileId);
            firstParam = false;
        }
        // limit
        url.append(firstParam ? "?" : "&").append("limit=" + limit);

        return url.toString();
    }

    /* PROCEDURES */

    public static class GetRecentlyReadRequest extends GetNetworkRequest<List<ReadPosition>> {
        public GetRecentlyReadRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-recently-read.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected List<ReadPosition> manageResponse(InputStream is) throws JSONException, ParseException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseReadPositionList(reader);
        }
    }


    public static class PostRecentlyReadRequest extends Request<ReadPosition> {
        // we need to crate a new procedure as this endpoint returns 200 or 201
        // depending on whether the read position existed or not

        private final String url = RECENTLY_READ_BASE_URL;
        private final ReadPosition readPosition;

        public PostRecentlyReadRequest(ReadPosition readPosition, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(authTokenManager, clientCredentials);
            this.readPosition = readPosition;
        }


        @Override
        protected RequestResponse<ReadPosition> doRun() throws MendeleyException {

            HttpURLConnection con = null;
            OutputStream os = null;
            InputStream is = null;
            try {
                final String postingJson = JsonParser.jsonFromReadPosition(readPosition);
                con = getConnection(url, "POST", authTokenManager);
                con.addRequestProperty("Content-type", "application/vnd.mendeley-recently-read.1+json");
                con.setFixedLengthStreamingMode(postingJson.getBytes().length);
                con.connect();

                os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postingJson);
                writer.flush();
                writer.close();
                os.close();

                final int responseCode = con.getResponseCode();
                if (responseCode < 200 && responseCode >= 300 ) {
                    throw HttpResponseException.create(con);
                } else {
                    is = con.getInputStream();
                    // FIXME passing null as server date
                    final JsonReader reader = new JsonReader(new InputStreamReader(is));
                    return new RequestResponse<ReadPosition>(JsonParser.parseReadPosition(reader), null);
                }
            } catch (ParseException pe) {
                throw new MendeleyException("Could not parse response for " + url, pe);
            } catch (IOException e) {
                throw new MendeleyException(e.getMessage(), e);
            } catch (JSONException e) {
                throw new JsonParsingException(e.getMessage(), e);
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException ignored) {
                }

                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ignored) {
                }

                if (con != null) {
                    con.disconnect();
                }
            }

        }
    }

}
