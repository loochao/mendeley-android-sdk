package com.mendeley.integration;

import com.mendeley.api.MendeleySdk;
import com.mendeley.api.callbacks.document.GetDocumentsCallback;
import com.mendeley.api.callbacks.document.TrashDocumentCallback;
import com.mendeley.api.callbacks.trash.RestoreDocumentCallback;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;
import com.mendeley.api.params.Page;
import com.mendeley.testUtils.SignInException;
import com.mendeley.testUtils.TestUtils;

import java.util.Date;
import java.util.List;

public class TrashNetworkProviderTest extends BaseNetworkProviderTest {
    private MendeleySdk sdk;

    private GetDocumentsCallback getTrashedDocumentsCallback;
    private TrashDocumentCallback trashDocumentCallback;
    private RestoreDocumentCallback restoreDocumentCallback;
    private GetDocumentsCallback getLibraryDocumentsCallback;

    private List<Document> trashedDocumentsRcvd;
    private List<Document> libraryDocumentsRcvd;
    private Page pageRcvd;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        getTrashedDocumentsCallback = new GetDocumentsCallback() {
            @Override
            public void onDocumentsReceived(List<Document> documents, Page page, Date serverDate) {
                setTrashedDocuments(documents, page);
                reportSuccess();
            }

            @Override
            public void onDocumentsNotReceived(MendeleyException e) {
                fail("trashed documents not received: " + e);
            }
        };
        getLibraryDocumentsCallback = new GetDocumentsCallback() {
            @Override
            public void onDocumentsReceived(List<Document> documents, Page page, Date serverDate) {
                setLibraryDocuments(documents, page);
                reportSuccess();
            }

            @Override
            public void onDocumentsNotReceived(MendeleyException e) {
                fail("library documents not received: " + e);
            }
        };
        trashDocumentCallback = new TrashDocumentCallback() {
            @Override
            public void onDocumentTrashed(String documentId) {
                reportSuccess();
            }

            @Override
            public void onDocumentNotTrashed(MendeleyException e) {
                fail("document not received trashed: " + e);
            }
        };
        restoreDocumentCallback = new RestoreDocumentCallback() {
            @Override
            public void onDocumentRestored(String documentId) {
                reportSuccess();
            }

            @Override
            public void onDocumentNotRestored(MendeleyException e) {
                fail("document not received trashed: " + e);
            }
        };
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetTrashedDocuments() {
        restoreAnyTrashedDocuments();

        getTrashedDocuments();

        assertNotNull(trashedDocumentsRcvd);
        assertEquals("expected no trashed documents", 0, trashedDocumentsRcvd.size());
    }

    public void testTrashAndRestore() {
        getTrashedDocuments();
        assertEquals("expected no trashed documents", 0, trashedDocumentsRcvd.size());

        getLibraryDocuments();
        final int numLibraryDocs = libraryDocumentsRcvd.size();
        assertTrue("need at least one document to trash", numLibraryDocs > 0);
        String docId = libraryDocumentsRcvd.get(0).id;

        trashDocument(docId);

        getTrashedDocuments();
        assertEquals("expected one trashed document", 1, trashedDocumentsRcvd.size());
        assertEquals(docId, trashedDocumentsRcvd.get(0).id);

        getLibraryDocuments();
        assertEquals("expected one fewer document than before trash operation",
                numLibraryDocs - 1, libraryDocumentsRcvd.size());

        restoreDocument(docId);

        getTrashedDocuments();
        assertEquals("expected no trashed documents", 0, trashedDocumentsRcvd.size());

        getLibraryDocuments();
        assertEquals("expected same documents as before trash and restore",
                numLibraryDocs, libraryDocumentsRcvd.size());
    }

    private void trashDocument(String id) {
        expectSdkCall();
        sdk.trashDocument(id, trashDocumentCallback);
        waitForSdkResponse("trashing document");
    }

    private void restoreDocument(String id) {
        expectSdkCall();
        sdk.restoreDocument(id, restoreDocumentCallback);
        waitForSdkResponse("restoring trashed document");
    }

    private void getTrashedDocuments() {
        expectSdkCall();
        sdk.getTrashedDocuments(getTrashedDocumentsCallback);
        waitForSdkResponse("getting trashed documents");
    }

    private void getLibraryDocuments() {
        expectSdkCall();
        sdk.getDocuments(getLibraryDocumentsCallback);
        waitForSdkResponse("getting documents");
    }

    private void setTrashedDocuments(List<Document> documents, Page page) {
        trashedDocumentsRcvd = documents;
        pageRcvd = page;
    }

    private void setLibraryDocuments(List<Document> documents, Page page) {
        libraryDocumentsRcvd = documents;
        pageRcvd = page;
    }

    private void restoreAnyTrashedDocuments() {
        getTrashedDocuments();
        for (Document doc : trashedDocumentsRcvd) {
            restoreDocument(doc.id);
        }
    }
}
