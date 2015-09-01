package com.mendeley.api.integration;


import com.mendeley.api.callbacks.document.DocumentList;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;

public class TrashNetworkBlockingTest extends DocumentNetworkBlockingTest {


    public void testGetTrashedDocuments() throws MendeleyException {
        restoreAnyTrashedDocuments();

        DocumentList docList = sdk.getTrashedDocuments();

        assertEquals("expected no trashed documents", 0, docList.documents.size());
    }

    public void testTrashAndRestore() throws MendeleyException {
        ensureCorrectDocumentsExist();

        DocumentList trashedDocList;
        DocumentList libraryDocList;

        trashedDocList = sdk.getTrashedDocuments();
        assertEquals("expected no trashed documents", 0, trashedDocList.documents.size());

        libraryDocList = sdk.getDocuments();
        final int numLibraryDocs = libraryDocList.documents.size();
        assertTrue("need at least one document to trash", numLibraryDocs > 0);
        String docId = libraryDocList.documents.get(0).id;

        sdk.trashDocument(docId);

        trashedDocList = sdk.getTrashedDocuments();
        assertEquals("expected one trashed document", 1, trashedDocList.documents.size());
        assertEquals(docId, trashedDocList.documents.get(0).id);

        libraryDocList = sdk.getDocuments();
        assertEquals("expected one fewer document than before trash operation",
                numLibraryDocs - 1, libraryDocList.documents.size());

        sdk.restoreDocument(docId);

        trashedDocList = sdk.getTrashedDocuments();
        assertEquals("expected no trashed documents", 0, trashedDocList.documents.size());

        libraryDocList = sdk.getDocuments();
        assertEquals("expected same documents as before trash and restore",
                numLibraryDocs, libraryDocList.documents.size());
    }

    public void testTrashAndDelete() throws MendeleyException {
        ensureCorrectDocumentsExist();

        DocumentList trashedDocList;
        DocumentList libraryDocList;

        trashedDocList = sdk.getTrashedDocuments();
        assertEquals("expected no trashed documents", 0, trashedDocList.documents.size());

        libraryDocList = sdk.getDocuments();
        final int numLibraryDocs = libraryDocList.documents.size();
        assertTrue("need at least one document to trash", numLibraryDocs > 0);
        String docId = libraryDocList.documents.get(0).id;

        sdk.trashDocument(docId);

        trashedDocList = sdk.getTrashedDocuments();
        assertEquals("expected one trashed document", 1, trashedDocList.documents.size());
        assertEquals(docId, trashedDocList.documents.get(0).id);

        libraryDocList = sdk.getDocuments();
        assertEquals("expected one fewer document than before trash operation",
                numLibraryDocs - 1, libraryDocList.documents.size());

        sdk.deleteTrashedDocument(docId);

        trashedDocList = sdk.getTrashedDocuments();
        assertEquals("expected no trashed documents", 0, trashedDocList.documents.size());

        libraryDocList = sdk.getDocuments();
        assertEquals("expected one document less than before trash and delete",
                numLibraryDocs - 1, libraryDocList.documents.size());
    }

    private void restoreAnyTrashedDocuments() throws MendeleyException {
        DocumentList docList = sdk.getTrashedDocuments();
        for (Document doc : docList.documents) {
            sdk.restoreDocument(doc.id);
        }
    }
}
