package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Document;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.params.CatalogDocumentRequestParameters;
import com.mendeley.api.request.params.View;

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

    public static Uri getGetCatalogDocumentUrl(String catalogId, View view) {
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
        StringBuilder url = new StringBuilder();
        url.append(baseUrl);
        StringBuilder paramsString = new StringBuilder();

        if (params != null) {
            boolean firstParam = true;
            if (params.view != null) {
                paramsString.append(firstParam ? "?" : "&").append("view=" + params.view);
                firstParam = false;
            }
            if (params.arxiv != null) {
                paramsString.append(firstParam ? "?" : "&").append("arxiv=" + params.arxiv);
                firstParam = false;
            }
            if (params.doi != null) {
                paramsString.append(firstParam ? "?" : "&").append("doi=" + params.doi);
                firstParam = false;
            }
            if (params.isbn != null) {
                paramsString.append(firstParam ? "?" : "&").append("isbn=" + params.isbn);
                firstParam = false;
            }
            if (params.issn != null) {
                paramsString.append(firstParam ? "?" : "&").append("issn=" + params.issn);
                firstParam = false;
            }
            if (params.pmid != null) {
                paramsString.append(firstParam ? "?" : "&").append("pmid=" + params.pmid);
            }
            if (params.scopus != null) {
                paramsString.append(firstParam ? "?" : "&").append("scopus=" + params.scopus);
            }
            if (params.filehash != null) {
                paramsString.append(firstParam ? "?" : "&").append("filehash=" + params.filehash);
            }
        }

        url.append(paramsString.toString());
        return Uri.parse(url.toString());
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
        public GetCatalogDocumentRequest(String catalogId, View view, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
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
}
