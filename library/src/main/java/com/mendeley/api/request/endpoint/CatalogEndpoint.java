package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Document;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;


public class CatalogEndpoint {

    public static String CATALOG_BASE_URL = MENDELEY_API_BASE_URL + "catalog";
    private static String CATALOG_CONTENT_TYPE = DocumentEndpoint.DOCUMENTS_CONTENT_TYPE;

    public static Uri getGetCatalogDocumentUrl(String catalogId, DocumentEndpoint.DocumentRequestParameters.View view) {
        StringBuilder url = new StringBuilder();
        url.append(CATALOG_BASE_URL);
        url.append("/").append(catalogId);

        if (view != null) {
            url.append("?").append("view=" + view);
        }

        return Uri.parse(url.toString());
    }



    public static Uri getGetCatalogDocumentsUrl(CatalogDocumentRequestParameters params) {
        return getCatalogGetDocumentsUrl(CATALOG_BASE_URL, params);
    }

    private static Uri getCatalogGetDocumentsUrl(String baseUrl, CatalogDocumentRequestParameters params) {
        final Uri.Builder bld = Uri.parse(baseUrl).buildUpon();

        if (params != null) {
            if (params.view != null) {
                bld.appendQueryParameter("view", params.view.getValue());
            }
            if (params.arxiv != null) {
                bld.appendQueryParameter("arxiv", params.arxiv);
            }
            if (params.doi != null) {
                bld.appendQueryParameter("doi", params.doi);
            }
            if (params.isbn != null) {
                bld.appendQueryParameter("isbn", params.isbn);
            }
            if (params.issn != null) {
                bld.appendQueryParameter("issn", params.issn);
            }
            if (params.pmid != null) {
                bld.appendQueryParameter("pmid", params.pmid);
            }
            if (params.scopus != null) {
                bld.appendQueryParameter("scopus", params.scopus);
            }
            if (params.filehash != null) {
                bld.appendQueryParameter("filehash", params.filehash);
            }
        }

        return bld.build();
    }

    public static class GetCatalogDocumentsRequest extends GetAuthorizedRequest<List<Document>> {
        public GetCatalogDocumentsRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetCatalogDocumentsRequest(CatalogDocumentRequestParameters parameters, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(CatalogEndpoint.getGetCatalogDocumentsUrl(parameters), authTokenManager, clientCredentials);
        }

        @Override
        protected List<Document> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", CATALOG_CONTENT_TYPE);
        }
    }

    public static class GetCatalogDocumentRequest extends GetAuthorizedRequest<Document> {
        public GetCatalogDocumentRequest(String catalogId, DocumentEndpoint.DocumentRequestParameters.View view, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(CatalogEndpoint.getGetCatalogDocumentUrl(catalogId, view), authTokenManager, clientCredentials);
        }

        @Override
        protected Document manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocument(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", CATALOG_CONTENT_TYPE);
        }
    }

    /**
     * Parameters for requests to retrieve catalog documents.
     * <p>
     * Uninitialised properties will be ignored.
     */
    public static class CatalogDocumentRequestParameters {

        public String arxiv;

        public String doi;

        public String isbn;

        public String issn;

        public String pmid;

        public String scopus;

        public String filehash;

        public DocumentEndpoint.DocumentRequestParameters.View view;

    }
}
