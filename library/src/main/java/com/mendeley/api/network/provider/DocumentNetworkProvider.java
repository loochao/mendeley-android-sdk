package com.mendeley.api.network.provider;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.callbacks.RequestHandle;
import com.mendeley.api.callbacks.document.DeleteDocumentCallback;
import com.mendeley.api.callbacks.document.DocumentIdList;
import com.mendeley.api.callbacks.document.DocumentList;
import com.mendeley.api.callbacks.document.GetDeletedDocumentsCallback;
import com.mendeley.api.callbacks.document.GetDocumentCallback;
import com.mendeley.api.callbacks.document.GetDocumentTypesCallback;
import com.mendeley.api.callbacks.document.GetDocumentsCallback;
import com.mendeley.api.callbacks.document.PatchDocumentCallback;
import com.mendeley.api.callbacks.document.PostDocumentCallback;
import com.mendeley.api.callbacks.document.TrashDocumentCallback;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.exceptions.NoMorePagesException;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.DocumentId;
import com.mendeley.api.network.Environment;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.NullRequest;
import com.mendeley.api.network.procedure.GetNetworkProcedure;
import com.mendeley.api.network.procedure.PatchNetworkProcedure;
import com.mendeley.api.network.procedure.PostNetworkProcedure;
import com.mendeley.api.network.task.DeleteNetworkTask;
import com.mendeley.api.network.task.GetNetworkTask;
import com.mendeley.api.network.task.PatchNetworkTask;
import com.mendeley.api.network.task.PostNetworkTask;
import com.mendeley.api.network.task.PostNoBodyNetworkTask;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.api.params.View;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.network.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Documents API calls
 */
public class DocumentNetworkProvider {
	public static String DOCUMENTS_BASE_URL = API_URL + "documents";
	public static String DOCUMENT_TYPES_BASE_URL = API_URL + "document_types";
    public static String IDENTIFIER_TYPES_BASE_URL = API_URL + "identifier_types";
	
	public static SimpleDateFormat patchDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT' Z");

    private final Environment environment;
    private final AccessTokenProvider accessTokenProvider;

    public DocumentNetworkProvider(Environment environment, AccessTokenProvider accessTokenProvider) {
        this.environment = environment;
        this.accessTokenProvider = accessTokenProvider;
    }

    public RequestHandle doGetDocuments(DocumentRequestParameters params, GetDocumentsCallback callback) {
        try {
            String[] paramsArray = new String[] { getGetDocumentsUrl(params, null) };
            GetDocumentsTask getDocumentsTask = new GetDocumentsTask(callback, accessTokenProvider);
            getDocumentsTask.executeOnExecutor(environment.getExecutor(), paramsArray);
            return getDocumentsTask;
        }
        catch (UnsupportedEncodingException e) {
            callback.onDocumentsNotReceived(new MendeleyException(e.getMessage(), e));
            return NullRequest.get();
        }
    }

    public RequestHandle doGetDeletedDocuments(String deletedSince, DocumentRequestParameters params,
                                               GetDeletedDocumentsCallback callback) {
        try {
            String[] paramsArray = new String[] { getGetDocumentsUrl(params, deletedSince) };
            GetDeletedDocumentsTask getDocumentsTask = new GetDeletedDocumentsTask(callback);
            getDocumentsTask.executeOnExecutor(environment.getExecutor(), paramsArray);
            return getDocumentsTask;
        }
        catch (UnsupportedEncodingException e) {
            callback.onDeletedDocumentsNotReceived(new MendeleyException(e.getMessage(), e));
            return NullRequest.get();
        }
    }

    public RequestHandle doGetDocuments(Page next, GetDocumentsCallback callback) {
        if (Page.isValidPage(next)) {
            String[] paramsArray = new String[] { next.link };
            GetDocumentsTask getDocumentsTask = new GetDocumentsTask(callback, accessTokenProvider);
            getDocumentsTask.executeOnExecutor(environment.getExecutor(), paramsArray);
            return getDocumentsTask;
        } else {
            callback.onDocumentsNotReceived(new NoMorePagesException());
            return NullRequest.get();
        }
    }


    public RequestHandle doGetDeletedDocuments(Page next, GetDeletedDocumentsCallback callback) {
        if (Page.isValidPage(next)) {
            String[] paramsArray = new String[] { next.link };
            GetDeletedDocumentsTask getDocumentsTask = new GetDeletedDocumentsTask(callback);
            getDocumentsTask.executeOnExecutor(environment.getExecutor(), paramsArray);
            return getDocumentsTask;
        } else {
            callback.onDeletedDocumentsNotReceived(new NoMorePagesException());
            return NullRequest.get();
        }
    }

    public void doGetDocument(String documentId, View view, GetDocumentCallback callback) {
        String[] paramsArray = new String[] { getGetDocumentUrl(documentId, view) };
        new GetDocumentTask(callback).executeOnExecutor(environment.getExecutor(), paramsArray);
    }

	/**
	 * @param document the document to post
	 */
    public void doPostDocument(Document document, PostDocumentCallback callback) {
		try {
			String[] paramsArray = new String[] { DOCUMENTS_BASE_URL, JsonParser.jsonFromDocument(document) };
			new PostDocumentTask(callback).executeOnExecutor(environment.getExecutor(), paramsArray);
		} catch (JSONException e) {
            callback.onDocumentNotPosted(new JsonParsingException(e.getMessage()));
        }
	}

    /**
     * @param documentId the document id to be patched
     * @param date the date object
     * @param document the Document to patch
     */
    public RequestHandle doPatchDocument(String documentId, Date date, Document document, PatchDocumentCallback callback) {
        String dateString = formatDate(date);

        try {
            String[] paramsArray = new String[] { getPatchDocumentUrl(documentId), JsonParser.jsonFromDocument(document) };
            PatchDocumentTask patchDocumentTask = new PatchDocumentTask(callback, documentId, dateString);
            patchDocumentTask.executeOnExecutor(environment.getExecutor(), paramsArray);
            return patchDocumentTask;
        } catch (JSONException e) {
            callback.onDocumentNotPatched(new JsonParsingException(e.getMessage()));
            return NullRequest.get();
        }
    }

    public void doPostTrashDocument(String documentId, TrashDocumentCallback callback) {
		String[] paramsArray = new String[] { getTrashDocumentUrl(documentId) };
		new PostTrashDocumentTask(callback, documentId).executeOnExecutor(environment.getExecutor(), paramsArray);
	}

    /**
     * @param documentId the document to delete
     */
    public void doDeleteDocument(String documentId, DeleteDocumentCallback callback) {
        String[] paramsArray = new String[] { getDeleteDocumentUrl(documentId) };
        new DeleteDocumentTask(documentId, callback).executeOnExecutor(environment.getExecutor(), paramsArray);
    }

    public RequestHandle doGetDocumentTypes(GetDocumentTypesCallback callback) {
        String[] paramsArray = new String[] { DOCUMENT_TYPES_BASE_URL };
        GetDocumentTypesTask getDocumentTypesTask = new GetDocumentTypesTask(callback);
        getDocumentTypesTask.executeOnExecutor(environment.getExecutor(), paramsArray);
        return getDocumentTypesTask;
    }

    /* URLS */

    /**
     * Building the url for deleting document
     *
     * @param documentId the id of the document to delete
     * @return the url string
     */
    public static String getDeleteDocumentUrl(String documentId) {
        return DOCUMENTS_BASE_URL + "/" + documentId;
    }

    /**
     * Building the url for post trash document
     *
     * @param documentId the id of the document to trash
     * @return the url string
     */
    public static String getTrashDocumentUrl(String documentId) {
        return DOCUMENTS_BASE_URL + "/" + documentId + "/trash";
    }

    /**
     * Builds the url for get document
     *
     * @param documentId the document id
     * @return the url string
     */
    public static String getGetDocumentUrl(String documentId, View view) {
        StringBuilder url = new StringBuilder();
        url.append(DOCUMENTS_BASE_URL);
        url.append("/").append(documentId);

        if (view != null) {
            url.append("?").append("view=" + view);
        }

        return url.toString();
    }

    /**
	 * Building the url for get documents
	 * 
	 * @return the url string
	 * @throws UnsupportedEncodingException 
	 */
    public static String getGetDocumentsUrl(DocumentRequestParameters params, String deletedSince) throws UnsupportedEncodingException {
    	return getGetDocumentsUrl(DOCUMENTS_BASE_URL, params, deletedSince);
    }
    
    /**
	 * Building the url for get trashed documents
	 * 
	 * @return the url string
	 * @throws UnsupportedEncodingException 
	 */
    public static String getTrashDocumentsUrl(DocumentRequestParameters params, String deletedSince) throws UnsupportedEncodingException {
    	return getGetDocumentsUrl(TrashNetworkProvider.BASE_URL, params, deletedSince);
    }
    
	private static String getGetDocumentsUrl(String baseUrl, DocumentRequestParameters params, String deletedSince) throws UnsupportedEncodingException {
		StringBuilder url = new StringBuilder();
		url.append(baseUrl);
		StringBuilder paramsString = new StringBuilder();
		
		if (params != null) {
			boolean firstParam = true;		
			if (params.view != null) {
				paramsString.append(firstParam ? "?" : "&").append("view=" + params.view);
				firstParam = false;
			}
			if (params.groupId != null) {
				paramsString.append(firstParam ? "?" : "&").append("group_id=" + params.groupId);
				firstParam = false;
			}
			if (params.modifiedSince != null) {
				paramsString.append(firstParam ? "?" : "&").append("modified_since="
                        + URLEncoder.encode(params.modifiedSince, "ISO-8859-1"));
				firstParam = false;
			}
			if (params.limit != null) {
				paramsString.append(firstParam ? "?" : "&").append("limit=" + params.limit);
				firstParam = false;
			}
			if (params.reverse != null) {
				paramsString.append(firstParam ? "?" : "&").append("reverse=" + params.reverse);
				firstParam = false;
			}
			if (params.order != null) {
				paramsString.append(firstParam ? "?" : "&").append("order=" + params.order);
				firstParam = false;
			}
			if (params.sort != null) {
				paramsString.append(firstParam ? "?" : "&").append("sort=" + params.sort);
			}
            if (deletedSince != null) {
                paramsString.append(firstParam ? "?" : "&").append("deleted_since="
                        + URLEncoder.encode(deletedSince, "ISO-8859-1"));
                firstParam = false;
            }
		}
		
		url.append(paramsString.toString());
		return url.toString();
	}

	/**
	 * Building the url for patch document
	 * 
	 * @param documentId the id of the document to patch
	 * @return the url string
	 */
	public static String getPatchDocumentUrl(String documentId) {
		return DOCUMENTS_BASE_URL + "/" + documentId;
	}

    /**
     * @param date the date to format
     * @return date string in the specified format
     */
    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        } else {
            return patchDateFormat.format(date);
        }
    }

    /* TASKS */

    public static class GetDocumentsTask extends GetNetworkTask {
        private final GetDocumentsCallback callback;
        private final AccessTokenProvider accessTokenProvider;

        List<Document> documents;

        public GetDocumentsTask(GetDocumentsCallback callback, AccessTokenProvider accessTokenProvider) {
            this.callback = callback;
            this.accessTokenProvider = accessTokenProvider;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            documents = JsonParser.parseDocumentList(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-document.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onDocumentsReceived(documents, next, serverDate);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onDocumentsNotReceived(exception);
        }
    }

    private class GetDeletedDocumentsTask extends GetNetworkTask {
        private final GetDeletedDocumentsCallback callback;

        List<DocumentId> documentIds;

        private GetDeletedDocumentsTask(GetDeletedDocumentsCallback callback) {
            this.callback = callback;
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
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onDeletedDocumentsReceived(documentIds, next, serverDate);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onDeletedDocumentsNotReceived(exception);
        }
    }

    private class GetDocumentTask extends GetNetworkTask {
        private final GetDocumentCallback callback;

        Document document;

        private GetDocumentTask(GetDocumentCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            document = JsonParser.parseDocument(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-document.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onDocumentReceived(document);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onDocumentNotReceived(exception);
        }
    }

    /**
     * Executing the api call for posting a document in the background.
     * sending the data to the relevant callback method in the MendeleyDocumentInterface.
     * If the call response code is different than expected or an exception is being thrown in the process
     * the exception will be added to the MendeleyResponse which is passed to the application via the callback.
     */
    private class PostDocumentTask extends PostNetworkTask {
        private final PostDocumentCallback callback;

        Document document;

        private PostDocumentTask(PostDocumentCallback callback) {
            this.callback = callback;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onDocumentPosted(document);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onDocumentNotPosted(exception);
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            document = JsonParser.parseDocument(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-document.1+json";
        }
    }

	private class PatchDocumentTask extends PatchNetworkTask {
        private final PatchDocumentCallback callback;

		private final String documentId;
        private final String date;

        Document document;

        private PatchDocumentTask(PatchDocumentCallback callback, String documentId, String date) {
            this.callback = callback;
            this.documentId = documentId;
            this.date = date;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

		@Override
		protected void onSuccess() {
			callback.onDocumentPatched(document);
		}

		@Override
		protected void onFailure(MendeleyException exception) {
			callback.onDocumentNotPatched(exception);
		}

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            document = JsonParser.parseDocument(jsonString);
        }

        @Override
        protected String getDate() {
            return date;
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-document.1+json";
        }
    }
	
	private class PostTrashDocumentTask extends PostNoBodyNetworkTask {
        private final TrashDocumentCallback callback;

		private final String documentId;

        private PostTrashDocumentTask(TrashDocumentCallback callback, String documentId) {
            this.callback = callback;
            this.documentId = documentId;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

		@Override
		protected void onSuccess() {
			callback.onDocumentTrashed(documentId);
		}

		@Override
		protected void onFailure(MendeleyException exception) {
			callback.onDocumentNotTrashed(exception);
		}
	}

    /**
     * Executing the api call for deleting a document in the background.
     * sending the data to the relevant callback method in the MendeleyDocumentInterface.
     * If the call response code is different than expected or an exception is being thrown in the process
     * the exception will be added to the MendeleyResponse which is passed to the application via the callback.
     */
    private class DeleteDocumentTask extends DeleteNetworkTask {
        private final String documentId;
        private final DeleteDocumentCallback callback;

        public DeleteDocumentTask(String documentId, DeleteDocumentCallback callback) {
            super();
            this.callback = callback;
            this.documentId = documentId;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onDocumentDeleted(documentId);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onDocumentNotDeleted(exception);
        }
    }

	private class GetDocumentTypesTask extends GetNetworkTask {
        private final GetDocumentTypesCallback callback;

		Map<String, String> typesMap;

        private GetDocumentTypesTask(GetDocumentTypesCallback callback) {
            this.callback = callback;
        }

        protected void processJsonString(String jsonString) throws JSONException {
            typesMap = JsonParser.parseDocumentTypes(jsonString);
        }

        protected String getContentType() {
            return "application/vnd.mendeley-document-type.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
		protected void onSuccess() {
			callback.onDocumentTypesReceived(typesMap);
		}

		@Override
		protected void onFailure(MendeleyException exception) {
			callback.onDocumentTypesNotReceived(exception);
		}
	}

    /* PROCEDURES */

    public static class GetDocumentsProcedure extends GetNetworkProcedure<DocumentList> {
        public GetDocumentsProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-document.1+json", authenticationManager);
        }

        @Override
        protected DocumentList processJsonString(String jsonString) throws JSONException {
            return new DocumentList(JsonParser.parseDocumentList(jsonString), next, serverDate);
        }
   }

    public static class GetDeletedDocumentsProcedure extends GetNetworkProcedure<DocumentIdList> {
        public GetDeletedDocumentsProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-document.1+json", authenticationManager);
        }

        @Override
        protected DocumentIdList processJsonString(String jsonString) throws JSONException {
            return new DocumentIdList(JsonParser.parseDocumentIds(jsonString), next, serverDate);
        }
    }

    public static class GetDocumentProcedure extends GetNetworkProcedure<Document> {
        public GetDocumentProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-document.1+json", authenticationManager);
        }

        @Override
        protected Document processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseDocument(jsonString);
        }
    }

    public static class GetDocumentTypesProcedure extends GetNetworkProcedure<Map<String, String>> {
        public GetDocumentTypesProcedure(String url, AuthenticationManager authenticationManager) {
            super(url, "application/vnd.mendeley-document-type.1+json", authenticationManager);
        }

        protected Map<String, String> processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseDocumentTypes(jsonString);
        }
    }

    public static class PostDocumentProcedure extends PostNetworkProcedure<Document> {
        public PostDocumentProcedure(Document doc, AuthenticationManager authenticationManager) throws JSONException {
            super(DOCUMENTS_BASE_URL, "application/vnd.mendeley-document.1+json",
                    JsonParser.jsonFromDocument(doc), authenticationManager);
        }

        @Override
        protected Document processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseDocument(jsonString);
        }
    }

    public static class PatchDocumentProcedure extends PatchNetworkProcedure<Document> {
        public PatchDocumentProcedure(String documentId, String json, Date date,
                                      AuthenticationManager authenticationManager) {
            super(getPatchDocumentUrl(documentId), "application/vnd.mendeley-document.1+json",
                    json,
                    formatDate(date),
                    authenticationManager);
        }

        @Override
        protected Document processJsonString(String jsonString) throws JSONException {
            return JsonParser.parseDocument(jsonString);
        }
    }
}
