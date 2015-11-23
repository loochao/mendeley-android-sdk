package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;
import com.mendeley.api.request.Request;
import com.mendeley.api.request.SignedInTest;

import java.io.IOException;
import java.util.List;

/**
 * These tests are using a file hash of an existing document file, MENDELEY: Getting Started with Mendeley
 * If this file will change the file hash will not return the catalog document id and the test will fail.
 */
public class CatalogRequestTest extends SignedInTest {

    @SmallTest
    public void test_getCatalogDocument_usesRightUrl_without_view() throws Exception {
        final String catalogId = "catalogId";
        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("catalog").
                appendPath(catalogId).
                build();

        Uri actual = getRequestFactory().getCatalogDocument(catalogId, null).getUrl();

        assertEquals("Get catalog document url without parameters is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_getCatalogDocument_usesRightUrl_with_view() throws Exception {

        final String catalogId = "catalogId";

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("catalog").
                appendPath(catalogId).
                appendQueryParameter("view", "client").
                build();

        Uri actual = getRequestFactory().getCatalogDocument(catalogId, DocumentEndpoint.DocumentRequestParameters.View.CLIENT).getUrl();

        assertEquals("Get catalog document url with parameters is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_getCatalogDocuments_usesTheRightUrl_withParams() throws Exception {

        DocumentEndpoint.DocumentRequestParameters.View view = DocumentEndpoint.DocumentRequestParameters.View.ALL;
        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("catalog").
                appendQueryParameter("view", "all").
                appendQueryParameter("arxiv", "arxivId").
                appendQueryParameter("doi", "doiId").
                appendQueryParameter("isbn", "isbnId").
                appendQueryParameter("issn", "issnId").
                appendQueryParameter("pmid", "pmidId").
                appendQueryParameter("scopus", "scopusId").
                appendQueryParameter("filehash", "filehash").
                build();

        CatalogEndpoint.CatalogDocumentRequestParameters params = new CatalogEndpoint.CatalogDocumentRequestParameters();
        params.view = view;
        params.arxiv = "arxivId";
        params.doi = "doiId";
        params.isbn = "isbnId";
        params.issn = "issnId";
        params.pmid = "pmidId";
        params.scopus = "scopusId";
        params.filehash = "filehash";


        final Uri url = getRequestFactory().getCatalogDocuments(params).getUrl();

        assertEquals("Get catalog documents url with parameters is wrong", expectedUrl, url);
    }

    @SmallTest
    public void test_getDocument_usesTheRightUrl_withoutView() throws Exception {

        final String documentId = "docId";

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL + "documents/" + documentId);

        Uri actual = getRequestFactory().getDocument(documentId, null).getUrl();

        assertEquals("Get document url without parameters is wrong", expectedUrl, actual);
    }

    public void test_getCatalogDocuments_receivesCorrectCatalogDocuments() throws MendeleyException, IOException {
        //GIVEN a file hash
        final String fileHash = "2064e86683343709cc3ff535587a4580bbb1b251";
        final String catalogDocumentId = "bcded033-52b5-370d-ac27-a3ec23146f88";
        CatalogEndpoint.CatalogDocumentRequestParameters catalogueParams = new CatalogEndpoint.CatalogDocumentRequestParameters();
        catalogueParams.filehash = fileHash;

        //WHEN getting a catalog document with this file hash
        List<Document> receivedDocs = getRequestFactory().getCatalogDocuments(catalogueParams).run().resource;

        //THEN the correct catalog document received
        Document catalogDocument = receivedDocs.get(0);
        assertEquals("wrong catalog document", catalogDocumentId, catalogDocument.id);
    }

    public void test_getCatalogDocumentById_receivesCorrectCatalogDocument() throws MendeleyException, IOException {
        //GIVEN a catalog document id
        final String catalogDocumentId = "bcded033-52b5-370d-ac27-a3ec23146f88";

        //WHEN getting a catalog document with this id
        Document receivedDoc = getRequestFactory().getCatalogDocument(catalogDocumentId, null).run().resource;

        //THEN the correct catalog document received
        assertEquals("wrong catalog document", catalogDocumentId, receivedDoc.id);
    }
}
