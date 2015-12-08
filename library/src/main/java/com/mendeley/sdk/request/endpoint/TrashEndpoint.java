package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.request.DeleteAuthorizedRequest;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.request.PostAuthorizedRequest;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static com.mendeley.sdk.request.Request.MENDELEY_API_BASE_URL;

public class TrashEndpoint {
    public static String BASE_URL = MENDELEY_API_BASE_URL + "trash";

    public static class GetTrashedDocumentsRequest extends GetAuthorizedRequest<List<Document>> {
        public static Uri getTrashDocumentsUrl(DocumentEndpoint.DocumentRequestParameters params) {
            Uri uri = Uri.parse(TrashEndpoint.BASE_URL);
            return (params != null) ? params.appendToUi(uri) : uri;
        }

        public GetTrashedDocumentsRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(url, authTokenManager, appCredentials);
        }

        public GetTrashedDocumentsRequest(DocumentEndpoint.DocumentRequestParameters parameters, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(getTrashDocumentsUrl(parameters), authTokenManager, appCredentials);
        }

        @Override
        protected List<Document> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.documentsFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DocumentEndpoint.DOCUMENTS_CONTENT_TYPE);
        }
    }

    public static class DeleteTrashedDocumentRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteTrashedDocumentRequest(String documentId,  AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(BASE_URL).buildUpon().appendPath(documentId).build(), authTokenManager, appCredentials);
        }
    }

    public static class RestoreTrashedDocumentRequest extends PostAuthorizedRequest<Void> {
        public RestoreTrashedDocumentRequest(String documentId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(BASE_URL).buildUpon().appendPath(documentId).appendPath("restore").build(), authTokenManager, appCredentials);
        }

        @Override
        protected Void manageResponse(InputStream is) throws Exception {
            return null;
        }

        @Override
        protected void writePostBody(OutputStream os) throws Exception {

        }
    }
}
