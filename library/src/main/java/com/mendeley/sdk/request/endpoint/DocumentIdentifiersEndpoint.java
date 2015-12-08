package com.mendeley.sdk.request.endpoint;


import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.mendeley.sdk.request.Request.MENDELEY_API_BASE_URL;

public class DocumentIdentifiersEndpoint {

    public static String DOCUMENT_IDENTIFIER_TYPES_BASE_URL = MENDELEY_API_BASE_URL + "identifier_types";
    public static final String DOCUMENT_IDENTIFIER_CONTENT_TYPE = "application/vnd.mendeley-document-identifier.1+json";

    public static class GetDocumentIdentifiersRequest extends GetAuthorizedRequest<Map<String, String>> {
        public GetDocumentIdentifiersRequest(AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(DOCUMENT_IDENTIFIER_TYPES_BASE_URL), authTokenManager, appCredentials);
        }

        protected Map<String, String> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.stringsMapFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DOCUMENT_IDENTIFIER_CONTENT_TYPE);
        }
    }

}
