package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Folder;
import com.mendeley.api.request.DeleteAuthorizedRequest;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.PatchAuthorizedRequest;
import com.mendeley.api.request.PostAuthorizedRequest;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
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

public class FoldersEndpoint {

	public static final String FOLDERS_BASE_URL = MENDELEY_API_BASE_URL + "folders";
    public static final String FOLDER_CONTENT_TYPE = "application/vnd.mendeley-folder.1+json";

    public static class GetFoldersRequest extends GetAuthorizedRequest<List<Folder>> {
        private static Uri getGetFoldersUrl(FolderRequestParameters params) {
            Uri uri = Uri.parse(FOLDERS_BASE_URL);
            return params != null ? params.appendToUi(uri) : uri;
        }

        public GetFoldersRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetFoldersRequest(FolderRequestParameters parameters, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getGetFoldersUrl(parameters), authTokenManager, clientCredentials);
        }

        @Override
        protected List<Folder> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseFolderList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", FOLDER_CONTENT_TYPE);
        }
    }

    public static class GetFolderRequest extends GetAuthorizedRequest<Folder> {
        public GetFolderRequest(String folderId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL + "/" + folderId), authTokenManager, clientCredentials);
        }

        @Override
        protected Folder manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseFolder(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", FOLDER_CONTENT_TYPE);
        }
    }

    public static class PostFolderRequest extends PostAuthorizedRequest<Folder> {
        private final Folder folder;

        public PostFolderRequest(Folder folder, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(FoldersEndpoint.FOLDERS_BASE_URL), authTokenManager, clientCredentials);
            this.folder = folder;
        }

        @Override
        protected void writePostBody(OutputStream os) throws Exception {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(JsonParser.jsonFromFolder(folder));
            writer.flush();
        }

        @Override
        protected Folder manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.parseFolder(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", FOLDER_CONTENT_TYPE);
        }
    }

    public static class PatchFolderAuthorizedRequest extends PatchAuthorizedRequest<Folder> {
        private final Folder folder;

        public PatchFolderAuthorizedRequest(String folderId, Folder folder, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL + "/" + folderId), null, authTokenManager, clientCredentials);
            this.folder = folder;
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", FOLDER_CONTENT_TYPE);
        }

        @Override
        protected HttpEntity createPatchingEntity() throws Exception {
            final String json = JsonParser.jsonFromFolder(folder);
            return new StringEntity(json, "UTF-8");
        }

        @Override
        protected Folder manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.parseFolder(reader);
        }

    }

    public static class PostDocumentToFolderRequest extends PostAuthorizedRequest<Void> {
        private final String documentId;

        public PostDocumentToFolderRequest(String folderId, String documentId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL).buildUpon().appendPath(folderId).appendPath("documents").build(), authTokenManager, clientCredentials);
            this.documentId = documentId;
        }

        @Override
        protected Void manageResponse(InputStream is) throws Exception {
            return null;
        }

        @Override
        protected void writePostBody(OutputStream os) throws Exception {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(JsonParser.jsonFromDocumentId(documentId));
            writer.flush();
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DocumentEndpoint.DOCUMENTS_CONTENT_TYPE);
        }
    }

    public static class GetFolderDocumentIdsRequest extends GetAuthorizedRequest<List<String>> {

        private static Uri getGetFolderDocumentIdsUrl(FolderRequestParameters params, String folderId) {
            Uri uri = Uri.parse(FOLDERS_BASE_URL).buildUpon().appendPath(folderId).appendPath("documents").build();
            return params != null ? params.appendToUi(uri) : uri;
        }

        public GetFolderDocumentIdsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetFolderDocumentIdsRequest(FolderRequestParameters parameters, String folderId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(getGetFolderDocumentIdsUrl(parameters, folderId), authTokenManager, clientCredentials);
        }

        @Override
        protected List<String> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentIds(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DocumentEndpoint.DOCUMENTS_CONTENT_TYPE);
        }
    }

    public static class DeleteFolderRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteFolderRequest(String folderId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL).buildUpon().appendPath(folderId).build(), authTokenManager, clientCredentials);
        }
    }

    public static class DeleteDocumentFromFolder extends DeleteAuthorizedRequest<Void> {
        public DeleteDocumentFromFolder(String folderId, String documentId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL).buildUpon().appendPath(folderId).appendPath(documentId).build(), authTokenManager, clientCredentials);
        }
    }

    /**
     * This class represents parameters for folder SDK requests.
     * <p>
     * Uninitialised properties will be ignored.
     */
    public static class FolderRequestParameters {
        /**
         * Group ID. If not supplied, returns user folders.
         */
        public String groupId;

        /**
         * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
         */
        public Integer limit;


        Uri appendToUi(Uri uri) {
            final Uri.Builder bld = uri.buildUpon();

            if (groupId != null) {
                bld.appendQueryParameter("view", groupId);
            }
            if (limit != null) {
                bld.appendQueryParameter("group_id", Integer.toString(limit));
            }

            return bld.build();
        }
    }
}
