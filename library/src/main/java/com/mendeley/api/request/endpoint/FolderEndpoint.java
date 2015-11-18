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
import com.mendeley.api.request.params.FolderRequestParameters;

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
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class FolderEndpoint {

	public static final String FOLDERS_BASE_URL = MENDELEY_API_BASE_URL + "folders";
    public static final String FOLDER_CONTENT_TYPE = "application/vnd.mendeley-folder.1+json";

    /* URLS */

    /**
     * Building the url for get folders
     *
     * @param params folder request parameters object
     * @return the url string
     */
    public static Uri getGetFoldersUrl(FolderRequestParameters params, String requestUrl) {
        StringBuilder url = new StringBuilder();

        url.append(requestUrl==null? FOLDERS_BASE_URL :requestUrl);

        if (params != null) {
            boolean firstParam = true;
            if (params.groupId != null) {
                url.append(firstParam?"?":"&").append("group_id="+params.groupId);
                firstParam = false;
            }
            if (params.limit != null) {
                url.append(firstParam?"?":"&").append("limit="+params.limit);
                firstParam = false;
            }
        }

        return Uri.parse(url.toString());
    }

    public static Uri getGetFoldersUrl(FolderRequestParameters params) {
        return getGetFoldersUrl(params, null);
    }

    /**
     * Building the url for get folder
     *
     * @param folderId the folder id to get
     * @return the url string
     */
    public static Uri getGetFolderUrl(String folderId) {
        return Uri.parse(FOLDERS_BASE_URL + "/" + folderId);
    }

    /**
     * Building the url for patch folder
     *
     * @param folderId the folder id to patch
     * @return the url string
     */
    public static Uri getPatchFolderUrl(String folderId) {
        return Uri.parse(FOLDERS_BASE_URL + "/" + folderId);
    }

    /**
	 * Building the url for delete folder
	 * 
	 * @param folderId the folder id
	 * @return the url string
	 */
    public static Uri getDeleteFolderUrl(String folderId) {
		return Uri.parse(FOLDERS_BASE_URL + "/" + folderId);
	}

    /**
     * Building the url for get folder document ids
     *
     * @param folderId the folder id
     * @return the url string
     */
    public static String getGetFolderDocumentIdsUrl(String folderId) {
        return FOLDERS_BASE_URL + "/" + folderId + "/documents";
    }

    /**
     * Building the url for post document to folder
     *
     * @param folderId the folder id
     * @return the url string
     */
    public static Uri getPostDocumentToFolderUrl(String folderId) {
        return Uri.parse(FOLDERS_BASE_URL + "/" + folderId + "/documents");
    }

    /**
	 * Building the url for delete document from folder
	 * 
	 * @param folderId the id of the folder
	 * @param documentId the id of the document to delete
	 */
    public static Uri getDeleteDocumentFromFolderUrl(String folderId, String documentId) {
		return Uri.parse(FOLDERS_BASE_URL + "/" + folderId + "/documents/" + documentId);
	}
	
    /* PROCEDURES */

    public static class GetFoldersRequest extends GetAuthorizedRequest<List<Folder>> {
        public GetFoldersRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetFoldersRequest(FolderRequestParameters parameters, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getGetFoldersUrl(parameters), authTokenManager, clientCredentials);
        }

        @Override
        protected List<Folder> manageResponse(InputStream is) throws JSONException, IOException {
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
            super(getGetFolderUrl(folderId), authTokenManager, clientCredentials);
        }

        @Override
        protected Folder manageResponse(InputStream is) throws JSONException, IOException {
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
            super(Uri.parse(FolderEndpoint.FOLDERS_BASE_URL), authTokenManager, clientCredentials);
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
            super(FolderEndpoint.getPatchFolderUrl(folderId), null, authTokenManager, clientCredentials);
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
            super(getPostDocumentToFolderUrl(folderId), authTokenManager, clientCredentials);
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
        public GetFolderDocumentIdsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetFolderDocumentIdsRequest(FolderRequestParameters parameters, String folderId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(getGetFoldersUrl(parameters, getGetFolderDocumentIdsUrl(folderId)), authTokenManager, clientCredentials);
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
            super(getDeleteFolderUrl(folderId), authTokenManager, clientCredentials);
        }
    }

    public static class DeleteDocumentFromFolder extends DeleteAuthorizedRequest<Void> {
        public DeleteDocumentFromFolder(String folderId, String documentId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(FolderEndpoint.getDeleteDocumentFromFolderUrl(folderId, documentId), authTokenManager, clientCredentials);
        }
    }
}
