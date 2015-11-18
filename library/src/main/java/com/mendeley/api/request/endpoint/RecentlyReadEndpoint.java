package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.PostAuthorizedRequest;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

/**
 * NetworkProvider class for Recently read API calls
 */
public class RecentlyReadEndpoint {
	public static final String RECENTLY_READ_BASE_URL = MENDELEY_API_BASE_URL + "recently_read";
    public static final String RECENTLY_READ_CONTENT_TYPE = "application/vnd.mendeley-recently-read.1+json";


    public static Uri getGetRecentlyReadUrl(String groupId, String fileId, int limit) {
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

        return Uri.parse(url.toString());
    }

    public static class GetRecentlyReadRequest extends GetAuthorizedRequest<List<ReadPosition>> {
        public GetRecentlyReadRequest(String groupId, String fileId, int limit, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(RecentlyReadEndpoint.getGetRecentlyReadUrl(groupId, fileId, limit), authTokenManager, clientCredentials);
        }

        @Override
        protected List<ReadPosition> manageResponse(InputStream is) throws JSONException, ParseException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseReadPositionList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", RECENTLY_READ_CONTENT_TYPE);
        }
    }


    public static class PostRecentlyReadRequest extends PostAuthorizedRequest<ReadPosition> {
        // we need to crate a new procedure as this endpoint returns 200 or 201
        // depending on whether the read position existed or not

        private final ReadPosition readPosition;

        public PostRecentlyReadRequest(ReadPosition readPosition, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(RECENTLY_READ_BASE_URL), authTokenManager, clientCredentials);
            this.readPosition = readPosition;
        }

        @Override
        protected void writePostBody(OutputStream os) throws Exception {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(JsonParser.jsonFromReadPosition(readPosition));
            writer.flush();
            writer.close();
        }

        @Override
        protected ReadPosition manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.parseReadPosition(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", RECENTLY_READ_CONTENT_TYPE);
        }
    }

}
