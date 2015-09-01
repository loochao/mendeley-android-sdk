package com.mendeley.api.network.provider;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.callbacks.read_position.ReadPositionList;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.procedure.GetNetworkProcedure;
import com.mendeley.api.network.procedure.Procedure;

import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.text.ParseException;

import static com.mendeley.api.network.NetworkUtils.API_URL;
import static com.mendeley.api.network.NetworkUtils.getConnection;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

/**
 * NetworkProvider class for Recently read API calls
 */
public class RecentlyReadNetworkProvider {
	public static final String RECENTLY_READ_BASE_URL = API_URL + "recently_read";

    private final AccessTokenProvider accessTokenProvider;

    public RecentlyReadNetworkProvider(AccessTokenProvider accessTokenProvider) {
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

    public static class GetRecentlyReadProcedure extends GetNetworkProcedure<ReadPositionList> {
        public GetRecentlyReadProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-recently-read.1+json", authenticationManager);
        }

        @Override
        protected ReadPositionList processJsonString(String jsonString) throws JSONException, ParseException {
            return new ReadPositionList(JsonParser.parseReadPositionList(jsonString), serverDate);
        }
    }


    public static class PostRecentlyReadProcedure extends Procedure<ReadPosition> {
        // we need to crate a new procedure as this endpoint returns 200 or 201
        // depending on whether the read position existed or not

        private final String url = RECENTLY_READ_BASE_URL;
        private final ReadPosition readPosition;

        public PostRecentlyReadProcedure(ReadPosition readPosition, AuthenticationManager authenticationManager) throws JSONException {
            super(authenticationManager);
            this.readPosition = readPosition;
        }


        @Override
        protected ReadPosition run() throws MendeleyException {

            HttpURLConnection con = null;
            OutputStream os = null;
            InputStream is = null;
            try {
                final String postingJson = JsonParser.jsonFromReadPosition(readPosition);
                con = getConnection(url, "POST", authenticationManager);
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
                    String responseString = readInputStream(is);
                    return JsonParser.parseReadPosition(responseString);
                }
            } catch (ParseException pe) {
                throw new MendeleyException("Could not parse response for " + url);
            } catch (IOException e) {
                throw new MendeleyException(e.getMessage());
            } catch (JSONException e) {
                throw new JsonParsingException(e.getMessage());
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
