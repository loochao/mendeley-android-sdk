package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Document;
import com.mendeley.api.request.DeleteAuthorizedRequest;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.PostAuthorizedRequest;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class TrashEndpoint {
    public static String BASE_URL = MENDELEY_API_BASE_URL + "trash";

    /* URLS */

    /**
     * Building the url for get trashed documents
     *
     * @return the url string
     */
    public static Uri getTrashDocumentsUrl(DocumentEndpoint.DocumentRequestParameters params, Date deletedSince) {
        Uri uri = Uri.parse(TrashEndpoint.BASE_URL);
        if (params != null) {
            uri = params.appendToUi(uri, deletedSince);
        }
        return uri;
    }

    public static Uri getRecoverUrl(String documentId) {
        return Uri.parse(BASE_URL + "/" + documentId + "/restore");
    }

    public static Uri getDeleteUrl(String documentId) {
        return Uri.parse( BASE_URL + "/" + documentId);
    }

    public static class GetTrashedDocumentsRequest extends GetAuthorizedRequest<List<Document>> {
        public GetTrashedDocumentsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetTrashedDocumentsRequest(DocumentEndpoint.DocumentRequestParameters parameters, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getTrashDocumentsUrl(parameters, null), authTokenManager, clientCredentials);
        }

        @Override
        protected List<Document> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DocumentEndpoint.DOCUMENTS_CONTENT_TYPE);
        }
    }

    public static class DeleteTrashedDocumentRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteTrashedDocumentRequest(String documentId,  AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(TrashEndpoint.getDeleteUrl(documentId), authTokenManager, clientCredentials);
        }
    }

    public static class RestoreDocumentRequest extends PostAuthorizedRequest<Void> {
        public RestoreDocumentRequest(String documentId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(TrashEndpoint.getRecoverUrl(documentId), authTokenManager, clientCredentials);
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
