package com.mendeley.api.request.endpoint;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.DeleteAuthorizedRequest;
import com.mendeley.api.request.PostAuthorizedRequest;

import java.io.InputStream;
import java.io.OutputStream;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class TrashEndpoint {
    public static String BASE_URL = MENDELEY_API_BASE_URL + "trash";

    /* URLS */

    public static Uri getRecoverUrl(String documentId) {
        return Uri.parse(BASE_URL + "/" + documentId + "/restore");
    }

    public static Uri getDeleteUrl(String documentId) {
        return Uri.parse( BASE_URL + "/" + documentId);
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
