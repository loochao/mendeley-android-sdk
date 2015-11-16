package com.mendeley.api.request.provider;

import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Folder;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.params.FolderRequestParameters;
import com.mendeley.api.request.PatchAuthorizedRequest;
import com.mendeley.api.request.PostNetworkRequest;

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

import static com.mendeley.api.request.Request.API_URL;

/**
 * NetworkProvider class for Folder API calls
 */
public class FolderNetworkProvider {
	public static final String FOLDERS_URL = API_URL + "folders";





    /* URLS */

    /**
     * Building the url for get folders
     *
     * @param params folder request parameters object
     * @return the url string
     */
    public static String getGetFoldersUrl(FolderRequestParameters params, String requestUrl) {
        StringBuilder url = new StringBuilder();

        url.append(requestUrl==null? FOLDERS_URL :requestUrl);

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

        return url.toString();
    }

    public static String getGetFoldersUrl(FolderRequestParameters params) {
        return getGetFoldersUrl(params, null);
    }

    /**
     * Building the url for get folder
     *
     * @param folderId the folder id to get
     * @return the url string
     */
    public static String getGetFolderUrl(String folderId) {
        return FOLDERS_URL + "/" + folderId;
    }

    /**
     * Building the url for patch folder
     *
     * @param folderId the folder id to patch
     * @return the url string
     */
    public static String getPatchFolderUrl(String folderId) {
        return FOLDERS_URL + "/" + folderId;
    }

    /**
	 * Building the url for delete folder
	 * 
	 * @param folderId the folder id
	 * @return the url string
	 */
    public static String getDeleteFolderUrl(String folderId) {
		return FOLDERS_URL + "/" + folderId;
	}

    /**
     * Building the url for get folder document ids
     *
     * @param folderId the folder id
     * @return the url string
     */
    public static String getGetFolderDocumentIdsUrl(String folderId) {
        return FOLDERS_URL + "/" + folderId + "/documents";
    }

    /**
     * Building the url for post document to folder
     *
     * @param folderId the folder id
     * @return the url string
     */
    public static String getPostDocumentToFolderUrl(String folderId) {
        return FOLDERS_URL + "/" + folderId + "/documents";
    }

    /**
	 * Building the url for delete document from folder
	 * 
	 * @param folderId the id of the folder
	 * @param documentId the id of the document to delete
	 */
    public static String getDeleteDocumentFromFolderUrl(String folderId, String documentId) {
		return FOLDERS_URL + "/" + folderId + "/documents/" + documentId;
	}
	
    /* PROCEDURES */

    public static class GetFoldersRequest extends GetAuthorizedRequest<List<Folder>> {
        public GetFoldersRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        @Override
        protected List<Folder> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseFolderList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", "application/vnd.mendeley-folder.1+json");
        }
    }

    public static class GetFolderRequest extends GetAuthorizedRequest<Folder> {
        public GetFolderRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        @Override
        protected Folder manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseFolder(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", "application/vnd.mendeley-folder.1+json");
        }
    }

    public static class PostFolderRequest extends PostNetworkRequest<Folder> {
        private final Folder folder;

        public PostFolderRequest(String url, Folder folder, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
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
            headers.put("Content-type", "application/vnd.mendeley-folder.1+json");
        }
    }

    public static class PatchFolderAuthorizedRequest extends PatchAuthorizedRequest<Folder> {
        private final Folder folder;

        public PatchFolderAuthorizedRequest(String url, Folder folder, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, null, authTokenManager, clientCredentials);
            this.folder = folder;
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", "application/vnd.mendeley-folder.1+json");
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

    public static class PostDocumentToFolderRequest extends PostNetworkRequest<Void> {
        private final String documentId;

        public PostDocumentToFolderRequest(String url, String documentId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
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
            headers.put("Content-type", "application/vnd.mendeley-document.1+json");
        }
    }

    public static class GetFolderDocumentIdsRequest extends GetAuthorizedRequest<List<String>> {
        public GetFolderDocumentIdsRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        @Override
        protected List<String> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentIds(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", "application/vnd.mendeley-document.1+json");
        }
    }
}
