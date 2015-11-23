package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static com.mendeley.sdk.request.Request.MENDELEY_API_BASE_URL;


public class CatalogEndpoint {

    public static String CATALOG_BASE_URL = MENDELEY_API_BASE_URL + "catalog";
    private static String CATALOG_CONTENT_TYPE = DocumentEndpoint.DOCUMENTS_CONTENT_TYPE;


    public static class GetCatalogDocumentsRequest extends GetAuthorizedRequest<List<Document>> {
        private static Uri getCatalogDocumentUrl(CatalogDocumentRequestParameters params) {
            final Uri.Builder bld = Uri.parse(CATALOG_BASE_URL).buildUpon();
            if (params == null) {
                return bld.build();
            }
            return params.appendToUi(bld.build());
        }

        public GetCatalogDocumentsRequest(CatalogDocumentRequestParameters params, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getCatalogDocumentUrl(params), authTokenManager, clientCredentials);
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
        private static Uri getGetCatalogDocumentUrl(String catalogId, DocumentEndpoint.DocumentRequestParameters.View view) {
            StringBuilder url = new StringBuilder();
            url.append(CATALOG_BASE_URL);
            url.append("/").append(catalogId);

            if (view != null) {
                url.append("?").append("view=" + view);
            }

            return Uri.parse(url.toString());
        }

        public GetCatalogDocumentRequest(String catalogId, DocumentEndpoint.DocumentRequestParameters.View view, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getGetCatalogDocumentUrl(catalogId, view), authTokenManager, clientCredentials);
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

        Uri appendToUi(Uri uri) {
            final Uri.Builder bld = uri.buildUpon();

            if (view != null) {
                bld.appendQueryParameter("view", view.getValue());
            }
            if (arxiv != null) {
                bld.appendQueryParameter("arxiv", arxiv);
            }
            if (doi != null) {
                bld.appendQueryParameter("doi", doi);
            }
            if (isbn != null) {
                bld.appendQueryParameter("isbn", isbn);
            }
            if (issn != null) {
                bld.appendQueryParameter("issn", issn);
            }
            if (pmid != null) {
                bld.appendQueryParameter("pmid", pmid);
            }
            if (scopus != null) {
                bld.appendQueryParameter("scopus", scopus);
            }
            if (filehash != null) {
                bld.appendQueryParameter("filehash", filehash);
            }

            return bld.build();
        }

    }
}
