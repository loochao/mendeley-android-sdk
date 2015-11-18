package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Document;
import com.mendeley.api.request.DeleteAuthorizedRequest;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.PatchAuthorizedRequest;
import com.mendeley.api.request.PostAuthorizedRequest;
import com.mendeley.api.request.params.DocumentRequestParameters;
import com.mendeley.api.request.params.View;
import com.mendeley.api.util.DateUtils;

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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class DocumentEndpoint {

	public static String DOCUMENTS_BASE_URL = MENDELEY_API_BASE_URL + "documents";
    public static String  DOCUMENTS_CONTENT_TYPE = "application/vnd.mendeley-document.1+json";

    /* URLS */

    /**
     * Building the url for deleting document
     *
     * @param documentId the id of the document to delete
     * @return the url string
     */
    public static Uri getDeleteDocumentUrl(String documentId) {
        return Uri.parse(DOCUMENTS_BASE_URL + "/" + documentId);
    }

    /**
     * Building the url for post trash document
     *
     * @param documentId the id of the document to trash
     * @return the url string
     */
    public static Uri getTrashDocumentUrl(String documentId) {
        return Uri.parse(DOCUMENTS_BASE_URL + "/" + documentId + "/trash");
    }

    /**
     * Builds the url for get document
     *
     * @param documentId the document id
     * @return the url string
     */
    public static Uri getGetDocumentUrl(String documentId, View view) {
        StringBuilder url = new StringBuilder();
        url.append(DOCUMENTS_BASE_URL);
        url.append("/").append(documentId);

        if (view != null) {
            url.append("?").append("view=" + view);
        }

        return Uri.parse(url.toString());
    }

    /**
	 * Building the url for get documents
	 * 
	 * @return the url string
	 */
    public static Uri getGetDocumentsUrl(DocumentRequestParameters params, Date deletedSince) {
    	return getGetDocumentsUrl(DOCUMENTS_BASE_URL, params, deletedSince);
    }
    
    /**
	 * Building the url for get trashed documents
	 * 
	 * @return the url string
	 */
    public static Uri getTrashDocumentsUrl(DocumentRequestParameters params, Date deletedSince) {
    	return getGetDocumentsUrl(TrashEndpoint.BASE_URL, params, deletedSince);
    }
    
	private static Uri getGetDocumentsUrl(String baseUrl, DocumentRequestParameters params, Date deletedSince)  {
        try {
            StringBuilder url = new StringBuilder();
            url.append(baseUrl);
            StringBuilder paramsString = new StringBuilder();

            if (params != null) {
                boolean firstParam = true;
                if (params.view != null) {
                    paramsString.append("?").append("view=").append(params.view);
                    firstParam = false;
                }
                if (params.groupId != null) {
                    paramsString.append(firstParam ? "?" : "&").append("group_id=").append(params.groupId);
                    firstParam = false;
                }
                if (params.modifiedSince != null) {
                    paramsString.append(firstParam ? "?" : "&").append("modified_since=").append(URLEncoder.encode(DateUtils.formatMendeleyApiTimestamp(params.modifiedSince), "ISO-8859-1"));
                    firstParam = false;
                }
                if (params.limit != null) {
                    paramsString.append(firstParam ? "?" : "&").append("limit=").append(params.limit);
                    firstParam = false;
                }
                if (params.reverse != null) {
                    paramsString.append(firstParam ? "?" : "&").append("reverse=").append(params.reverse);
                    firstParam = false;
                }
                if (params.order != null) {
                    paramsString.append(firstParam ? "?" : "&").append("order=").append(params.order);
                    firstParam = false;
                }
                if (params.sort != null) {
                    paramsString.append(firstParam ? "?" : "&").append("sort=").append(params.sort);
                }
                if (deletedSince != null) {
                    final String deletedSinceStr = DateUtils.formatMendeleyApiTimestamp(deletedSince);
                    paramsString.append(firstParam ? "?" : "&").append("deleted_since=").append(URLEncoder.encode(deletedSinceStr, "ISO-8859-1"));
                }
            }

            url.append(paramsString.toString());
            return Uri.parse(url.toString());

        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
	}

	/**
	 * Building the url for patch document
	 * 
	 * @param documentId the id of the document to patch
	 * @return the url string
	 */
	public static Uri getPatchDocumentUrl(String documentId) {
		return Uri.parse(DOCUMENTS_BASE_URL + "/" + documentId);
	}

    /* PROCEDURES */

    public static class GetDocumentsRequest extends GetAuthorizedRequest<List<Document>> {
        public GetDocumentsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        // TODO: put trashed as a field in the parameters
        public GetDocumentsRequest(DocumentRequestParameters parameters, boolean trashed, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(trashed ? DocumentEndpoint.getTrashDocumentsUrl(parameters, null) : getGetDocumentsUrl(parameters, null), authTokenManager, clientCredentials);
        }

        @Override
        protected List<Document> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DOCUMENTS_CONTENT_TYPE);
        }
   }

    public static class GetDeletedDocumentsRequest extends GetAuthorizedRequest<List<String>> {
        public GetDeletedDocumentsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetDeletedDocumentsRequest(DocumentRequestParameters parameters, Date deletedSince, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(getGetDocumentsUrl(parameters, deletedSince), authTokenManager, clientCredentials);
        }

        @Override
        protected List<String> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentIds(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DOCUMENTS_CONTENT_TYPE);
        }
    }


    public static class GetDocumentRequest extends GetAuthorizedRequest<Document> {

        public GetDocumentRequest(String documentId, View view, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getGetDocumentUrl(documentId, view), authTokenManager, clientCredentials);
        }

        @Override
        protected Document manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocument(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DOCUMENTS_CONTENT_TYPE);
        }
    }

    public static class PostDocumentRequest extends PostAuthorizedRequest<Document> {

        final private Document doc;

        public PostDocumentRequest(Document doc, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(DOCUMENTS_BASE_URL), authTokenManager, clientCredentials);
            this.doc = doc;
        }

        @Override
        protected void writePostBody(OutputStream os) throws Exception {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(JsonParser.jsonFromDocument(doc));
            writer.flush();
        }

        @Override
        protected Document manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.parseDocument(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", "application/vnd.mendeley-document.1+json");
        }
    }

    public static class PatchDocumentAuthorizedRequest extends PatchAuthorizedRequest<Document> {
        private final Document document;

        public PatchDocumentAuthorizedRequest(String documentId, Document document, Date date, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getPatchDocumentUrl(documentId), date, authTokenManager, clientCredentials);
            this.document = document;
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DOCUMENTS_CONTENT_TYPE);
        }

        @Override
        protected HttpEntity createPatchingEntity() throws Exception {
            final String json = JsonParser.jsonFromDocument(document);
            return new StringEntity(json, "UTF-8");
        }

        @Override
        protected Document manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.parseDocument(reader);
        }
    }

    public static class TrashDocumentRequest extends PostAuthorizedRequest<Void> {
        public TrashDocumentRequest(String documentId,  AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getTrashDocumentUrl(documentId), authTokenManager, clientCredentials);
        }

        @Override
        protected Void manageResponse(InputStream is) throws Exception {
            return null;
        }

        @Override
        protected void writePostBody(OutputStream os) throws Exception {

        }
    }

    public static class DeleteDocumentRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteDocumentRequest(String documentId,  AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(DocumentEndpoint.getDeleteDocumentUrl(documentId), authTokenManager, clientCredentials);
        }
    }

}
