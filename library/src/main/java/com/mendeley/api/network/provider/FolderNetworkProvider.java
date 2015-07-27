package com.mendeley.api.network.provider;

import java.util.List;

import org.json.JSONException;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.callbacks.RequestHandle;
import com.mendeley.api.callbacks.document.DocumentIdList;
import com.mendeley.api.callbacks.folder.DeleteFolderCallback;
import com.mendeley.api.callbacks.folder.DeleteFolderDocumentCallback;
import com.mendeley.api.callbacks.folder.FolderList;
import com.mendeley.api.callbacks.folder.GetFolderCallback;
import com.mendeley.api.callbacks.folder.GetFolderDocumentIdsCallback;
import com.mendeley.api.callbacks.folder.GetFoldersCallback;
import com.mendeley.api.callbacks.folder.PatchFolderCallback;
import com.mendeley.api.callbacks.folder.PostDocumentToFolderCallback;
import com.mendeley.api.callbacks.folder.PostFolderCallback;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.exceptions.NoMorePagesException;
import com.mendeley.api.model.DocumentId;
import com.mendeley.api.model.Folder;
import com.mendeley.api.network.Environment;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.NullRequest;
import com.mendeley.api.network.procedure.GetNetworkProcedure;
import com.mendeley.api.network.procedure.PatchNetworkProcedure;
import com.mendeley.api.network.procedure.PostNetworkProcedure;
import com.mendeley.api.network.procedure.PostNoResponseNetworkProcedure;
import com.mendeley.api.network.task.DeleteNetworkTask;
import com.mendeley.api.network.task.GetNetworkTask;
import com.mendeley.api.network.task.PatchNetworkTask;
import com.mendeley.api.network.task.PostNetworkTask;
import com.mendeley.api.network.task.PostNoResponseNetworkTask;
import com.mendeley.api.params.FolderRequestParameters;
import com.mendeley.api.params.Page;

import static com.mendeley.api.network.NetworkUtils.*;

/**
 * NetworkProvider class for Folder API calls
 */
public class FolderNetworkProvider {
	public static final String FOLDERS_URL = API_URL + "folders";

    private final Environment environment;
    private final AccessTokenProvider accessTokenProvider;

    public FolderNetworkProvider(Environment environment, AccessTokenProvider accessTokenProvider) {
        this.environment = environment;
        this.accessTokenProvider = accessTokenProvider;
    }

	/**
	 * @param params folder request parameters object
	 */
    public RequestHandle doGetFolders(FolderRequestParameters params, GetFoldersCallback callback) {
		String[] paramsArray = new String[] { getGetFoldersUrl(params) };
        GetFoldersTask getFoldersTask = new GetFoldersTask(callback);
        getFoldersTask.executeOnExecutor(environment.getExecutor(), paramsArray);
        return getFoldersTask;
	}

    /**
     * @param next reference to next page
     */
    public RequestHandle doGetFolders(Page next, GetFoldersCallback callback) {
        if (Page.isValidPage(next)) {
    		String[] paramsArray = new String[] { next.link };
            GetFoldersTask getFoldersTask = new GetFoldersTask(callback);
            new GetFoldersTask(callback).executeOnExecutor(environment.getExecutor(), paramsArray);
            return getFoldersTask;
        } else {
            callback.onFoldersNotReceived(new NoMorePagesException());
            return NullRequest.get();
        }
    }

	/**
	 * @param folderId the folder id to get
	 */
    public void doGetFolder(String folderId, GetFolderCallback callback) {
		String[] paramsArray = new String[] { getGetFolderUrl(folderId) };
		new GetFolderTask(callback).executeOnExecutor(environment.getExecutor(), paramsArray);
	}

	/**
	 * @param folderId the folder id
	 */
    public void doGetFolderDocumentIds(FolderRequestParameters params, String folderId, GetFolderDocumentIdsCallback callback) {
		String[] paramsArray = new String[] { getGetFoldersUrl(params, getGetFolderDocumentIdsUrl(folderId)) };
		new GetFolderDocumentIdsTask(callback, folderId).executeOnExecutor(environment.getExecutor(), paramsArray);
	}

    /**
     * @param next reference to next page
     */
    public void doGetFolderDocumentIds(Page next, String folderId, GetFolderDocumentIdsCallback callback) {
        if (Page.isValidPage(next)) {
    		String[] paramsArray = new String[] { next.link };
            new GetFolderDocumentIdsTask(callback, folderId).executeOnExecutor(environment.getExecutor(), paramsArray);
        } else {
            callback.onFolderDocumentIdsNotReceived(new NoMorePagesException());
        }
    }

    /**
	 * @param folder the folder to post
	 */
    public void doPostFolder(Folder folder, PostFolderCallback callback) {
		try {
    		String[] paramsArray = new String[] {FOLDERS_URL, JsonParser.jsonFromFolder(folder) };
			new PostFolderTask(callback).executeOnExecutor(environment.getExecutor(), paramsArray);
		} catch (JSONException e) {
            callback.onFolderNotPosted(new JsonParsingException(e.getMessage()));
        }
	}

    /**
     * @param folderId the folder id to patch
     * @param folder the Folder object
     */
    public RequestHandle doPatchFolder(String folderId, Folder folder, PatchFolderCallback callback) {
        String folderString = null;
        try {
            folderString  = JsonParser.jsonFromFolder(folder);
            String[] paramsArray = new String[] { getPatchFolderUrl(folderId), folderString };
            PatchFolderTask patchFolderTask = new PatchFolderTask(callback, folderId);
            patchFolderTask.executeOnExecutor(environment.getExecutor(), paramsArray);
            return patchFolderTask;
        } catch (JSONException e) {
            callback.onFolderNotPatched(new JsonParsingException(e.getMessage()));
            return NullRequest.get();
        }
    }

    /**
     * @param folderId the id of the folder to delete
     */
    public void doDeleteFolder(String folderId, DeleteFolderCallback callback) {
        String[] paramsArray = new String[] { getDeleteFolderUrl(folderId) };
        new DeleteFolderTask(folderId, callback).executeOnExecutor(environment.getExecutor(), paramsArray);
    }

    /**
     * @param folderId the folder id
     * @param documentId the id of the document to add to the folder
     */
    public void doPostDocumentToFolder(String folderId, String documentId, PostDocumentToFolderCallback callback) {
        String documentString = null;
        if (documentId != null && !documentId.isEmpty()) {
            try {
                documentString = JsonParser.jsonFromDocumentId(documentId);
            } catch (JSONException e) {
                callback.onDocumentNotPostedToFolder(new JsonParsingException(e.getMessage()));
            }
        }
        String[] paramsArray = new String[] { getPostDocumentToFolderUrl(folderId), documentString };
        new PostDocumentToFolderTask(callback, folderId).executeOnExecutor(environment.getExecutor(), paramsArray);
    }

    /**
     * @param folderId the id of the folder
     * @param documentId the id of the document to delete
     */
    public void doDeleteDocumentFromFolder(String folderId, String documentId, DeleteFolderDocumentCallback callback) {
        String[] paramsArray = new String[] { getDeleteDocumentFromFolderUrl(folderId, documentId) };
        new DeleteDocumentFromFolderTask(documentId, callback).executeOnExecutor(environment.getExecutor(), paramsArray);
    }

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
	
    /* TASKS */

    private class GetFoldersTask extends GetNetworkTask {
        private final GetFoldersCallback callback;

        List<Folder> folders;

        private GetFoldersTask(GetFoldersCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            folders = JsonParser.parseFolderList(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-folder.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onFoldersReceived(folders, next);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onFoldersNotReceived(exception);
        }
    }

    private class GetFolderTask extends GetNetworkTask {
        private final GetFolderCallback callback;

        Folder folder;

        private GetFolderTask(GetFolderCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            folder = JsonParser.parseFolder(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-folder.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onFolderReceived(folder);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onFolderNotReceived(exception);
        }
    }

    private class PostFolderTask extends PostNetworkTask {
        private final PostFolderCallback callback;

        Folder folder;

        private PostFolderTask(PostFolderCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            folder = JsonParser.parseFolder(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-folder.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onFolderPosted(folder);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onFolderNotPosted(exception);
        }
    }

    private class PatchFolderTask extends PatchNetworkTask {
        private final PatchFolderCallback callback;

		private final String folderId;

        private Folder folder;

        private PatchFolderTask(PatchFolderCallback callback, String folderId) {
            this.callback = callback;
            this.folderId = folderId;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            folder = JsonParser.parseFolder(jsonString);
        }

        @Override
        protected String getDate() {
            return null;
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-folder.1+json";
        }

        @Override
		protected void onSuccess() {
			callback.onFolderPatched(folder);
		}

		@Override
		protected void onFailure(MendeleyException exception) {
			callback.onFolderNotPatched(exception);
		}
	}

    private class DeleteFolderTask extends DeleteNetworkTask {
        private final String folderId;
        private final DeleteFolderCallback callback;

        private DeleteFolderTask(String folderId, DeleteFolderCallback callback) {
            this.folderId = folderId;
            this.callback = callback;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onFolderDeleted(folderId);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onFolderNotDeleted(exception);
        }
    }

    private class PostDocumentToFolderTask extends PostNoResponseNetworkTask {
        private final PostDocumentToFolderCallback callback;

        private final String folderId;

        private PostDocumentToFolderTask(PostDocumentToFolderCallback callback, String folderId) {
            this.callback = callback;
            this.folderId = folderId;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-folder-add-document.1+json";
        }

        @Override
        protected void onSuccess() {
            callback.onDocumentPostedToFolder(folderId);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onDocumentNotPostedToFolder(exception);
        }
    }

    private class DeleteDocumentFromFolderTask extends DeleteNetworkTask {
		private final String documentId;
        private final DeleteFolderDocumentCallback callback;

        public DeleteDocumentFromFolderTask(String documentId, DeleteFolderDocumentCallback callback) {
            this.documentId = documentId;
            this.callback = callback;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
		protected void onSuccess() {
			callback.onFolderDocumentDeleted(documentId);
		}

		@Override
		protected void onFailure(MendeleyException exception) {
			callback.onFolderDocumentNotDeleted(exception);
		}
	}
	
    private class GetFolderDocumentIdsTask extends GetNetworkTask {
        private final GetFolderDocumentIdsCallback callback;

        private final String folderId;

		private List<DocumentId> documentIds;

        private GetFolderDocumentIdsTask(GetFolderDocumentIdsCallback callback, String folderId) {
            this.callback = callback;
            this.folderId = folderId;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            documentIds = JsonParser.parseDocumentIds(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-document.1+json";
        }

        @Override
		protected void onSuccess() {
			callback.onFolderDocumentIdsReceived(folderId, documentIds, next);
		}

		@Override
		protected void onFailure(MendeleyException exception) {
			callback.onFolderDocumentIdsNotReceived(exception);
		}
	}

    /* PROCEDURES */

    public static class GetFoldersProcedure extends GetNetworkProcedure<FolderList> {
        public GetFoldersProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-folder.1+json", authenticationManager);
        }

        @Override
        protected FolderList processJsonString(String jsonString) throws JSONException {
            return new FolderList(JsonParser.parseFolderList(jsonString), next);
        }
    }

    public static class GetFolderProcedure extends GetNetworkProcedure<Folder> {
        public GetFolderProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-folder.1+json", authenticationManager);
        }

        @Override
        protected Folder processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseFolder(jsonString);
        }
    }

    public static class PostFolderProcedure extends PostNetworkProcedure<Folder> {
        public PostFolderProcedure(String url, String json, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-folder.1+json", json, authenticationManager);
        }

        @Override
        protected Folder processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseFolder(jsonString);
        }
    }

    public static class PatchFolderProcedure extends PatchNetworkProcedure<Folder> {
        public PatchFolderProcedure(String url, String json, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-folder.1+json", json, null, authenticationManager);
        }

        @Override
        protected Folder processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseFolder(jsonString);
        }
    }

    public static class PostDocumentToFolderProcedure extends PostNoResponseNetworkProcedure {
        public PostDocumentToFolderProcedure(String url, String json, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-folder-add-document.1+json", json, authenticationManager);
        }
    }

    public static class GetFolderDocumentIdsProcedure extends GetNetworkProcedure<DocumentIdList> {
        public GetFolderDocumentIdsProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-document.1+json", authenticationManager);
        }

        @Override
        protected DocumentIdList processJsonString(String jsonString) throws JSONException {
            return new DocumentIdList(JsonParser.parseDocumentIds(jsonString), next, serverDate);
        }
    }
}
