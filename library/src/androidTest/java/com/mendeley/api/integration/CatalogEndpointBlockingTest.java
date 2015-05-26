package com.mendeley.api.integration;

import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;
import com.mendeley.api.request.params.CatalogDocumentRequestParameters;

import java.io.IOException;
import java.util.List;

/**
 * These tests are using a file hash of an existing document file, MENDELEY: Getting Started with Mendeley
 * If this file will change the file hash will not return the catalog document id and the test will fail.
 */
public class CatalogEndpointBlockingTest extends EndpointBlockingTest {

    public void test_getCatalogDocuments_receivesCorrectCatalogDocuments() throws MendeleyException, IOException {
        //GIVEN a file hash
        final String fileHash = "2064e86683343709cc3ff535587a4580bbb1b251";
        final String catalogDocumentId = "bcded033-52b5-370d-ac27-a3ec23146f88";
        CatalogDocumentRequestParameters catalogueParams = new CatalogDocumentRequestParameters();
        catalogueParams.filehash = fileHash;

        //WHEN getting a catalog document with this file hash
        List<Document> receivedDocs = getSdk().getCatalogDocuments(catalogueParams).run().resource;

        //THEN the correct catalog document received
        Document catalogDocument = receivedDocs.get(0);
        assertEquals("wrong catalog document", catalogDocumentId, catalogDocument.id);
    }

    public void test_getCatalogDocumentById_receivesCorrectCatalogDocument() throws MendeleyException, IOException {
        //GIVEN a catalog document id
        final String catalogDocumentId = "bcded033-52b5-370d-ac27-a3ec23146f88";

        //WHEN getting a catalog document with this id
        Document receivedDoc = getSdk().getCatalogDocument(catalogDocumentId, null).run().resource;

        //THEN the correct catalog document received
        assertEquals("wrong catalog document", catalogDocumentId, receivedDoc.id);
    }
}
