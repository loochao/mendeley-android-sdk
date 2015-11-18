package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class DocumentTypesEndpoint {

    public static String DOCUMENT_TYPES_BASE_URL = MENDELEY_API_BASE_URL + "document_types";
    private static final String DOCUMENT_TYPES_CONTENT_TYPE = "application/vnd.mendeley-document-type.1+json";

    public static class GetDocumentTypesRequest extends GetAuthorizedRequest<Map<String, String>> {
        public GetDocumentTypesRequest(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(DOCUMENT_TYPES_BASE_URL), authTokenManager, clientCredentials);
        }

        protected Map<String, String> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseStringsMap(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DOCUMENT_TYPES_CONTENT_TYPE);
        }
    }

}
