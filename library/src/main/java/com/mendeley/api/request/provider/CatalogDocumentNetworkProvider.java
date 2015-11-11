package com.mendeley.api.request.provider;

import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Document;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.params.CatalogDocumentRequestParameters;
import com.mendeley.api.request.params.View;
import com.mendeley.api.request.procedure.GetNetworkRequest;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.mendeley.api.request.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Documents API calls
 */
public class CatalogDocumentNetworkProvider {
    public static String CATALOG_BASE_URL = API_URL + "catalog";

    public static SimpleDateFormat patchDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT' Z");


    private final AuthTokenManager accessTokenProvider;

    public CatalogDocumentNetworkProvider(AuthTokenManager accessTokenProvider) {
        this.accessTokenProvider = accessTokenProvider;
    }

    public static String getGetCatalogDocumentUrl(String catalogId, View view) {
        StringBuilder url = new StringBuilder();
        url.append(CATALOG_BASE_URL);
        url.append("/").append(catalogId);

        if (view != null) {
            url.append("?").append("view=" + view);
        }

        return url.toString();
    }


    /**
     * Building the url for get catalog document
     *
     * @return the url string
     */
    public static String getGetCatalogDocumentsUrl(CatalogDocumentRequestParameters params) {
        return getCatalogGetDocumentsUrl(CATALOG_BASE_URL, params);
    }

    private static String getCatalogGetDocumentsUrl(String baseUrl, CatalogDocumentRequestParameters params) {
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
        return url.toString();
    }

    public static class GetCatalogDocumentsRequest extends GetNetworkRequest<List<Document>> {
        public GetCatalogDocumentsRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-document.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected List<Document> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentList(reader);
        }
    }

    public static class GetCatalogDocumentRequest extends GetNetworkRequest<Document> {
        public GetCatalogDocumentRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-document.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected Document manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocument(reader);
        }
    }
}
