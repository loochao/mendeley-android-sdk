package com.mendeley.integration;

import com.mendeley.api.MendeleySdk;
import com.mendeley.api.callbacks.document.DeleteDocumentCallback;
import com.mendeley.api.callbacks.document.GetDeletedDocumentsCallback;
import com.mendeley.api.callbacks.document.GetDocumentCallback;
import com.mendeley.api.callbacks.document.GetDocumentTypesCallback;
import com.mendeley.api.callbacks.document.GetDocumentsCallback;
import com.mendeley.api.callbacks.document.PatchDocumentCallback;
import com.mendeley.api.callbacks.document.PostDocumentCallback;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.DocumentId;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.api.params.Sort;
import com.mendeley.testUtils.SignInException;
import com.mendeley.testUtils.TestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class DocumentNetworkProviderTest extends BaseNetworkProviderTest {
    private static final Document[] DOCUMENTS = {
            new Document.Builder()
                    .setTitle("How to avoid huge ships")
                    .setType("book")
                    .setYear(1993)
                    .build(),
            new Document.Builder()
                    .setTitle("Cheese problems solved")
                    .setType("book")
                    .setYear(2007)
                    .build(),
            new Document.Builder()
                    .setTitle("Across Europe by Kangaroo")
                    .setType("book")
                    .setYear(2003)
                    .build(),
            new Document.Builder()
                    .setTitle("How to Be Pope: What to Do and Where to Go Once You're in the Vatican")
                    .setType("book")
                    .setYear(2005)
                    .build(),
            new Document.Builder()
                    .setTitle("Be Bold with Bananas")
                    .setType("book")
                    .setYear(1972)
                    .build()
    };

    protected MendeleySdk sdk;

    private GetDocumentsCallback getDocumentsCallback;
    private GetDocumentCallback getDocumentCallback;
    private PostDocumentCallback postDocumentCallback;
    private DeleteDocumentCallback deleteDocumentCallback;
    private GetDocumentTypesCallback getDocumentTypesCallback;
    private PatchDocumentCallback patchDocumentCallback;
    private GetDeletedDocumentsCallback getDeletedDocumentsCallback;

    private List<Document> documentsRcvd;
    private Page pageRcvd;
    private Document documentRcvd;
    private Document patchedDocumentRcvd;
    private Document postedDocumentRcvd;
    private Map<String, String> typesRcvd;
    private List<DocumentId> documentIdsRcvd;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        getDocumentsCallback = new GetDocumentsCallback() {
            @Override
            public void onDocumentsReceived(List<Document> documents, Page page, Date serverDate) {
                setDocuments(documents, page);
                reportSuccess();
            }

            @Override
            public void onDocumentsNotReceived(MendeleyException e) {
                fail("documents not received: " + e);
            }
        };
        getDocumentCallback = new GetDocumentCallback() {
            @Override
            public void onDocumentReceived(Document document) {
                setDocument(document);
                reportSuccess();
            }

            @Override
            public void onDocumentNotReceived(MendeleyException e) {
                fail("document not received: " + e);
            }
        };
        postDocumentCallback = new PostDocumentCallback() {
            @Override public void onDocumentPosted(Document document) {
                setPostedDocumentRcvd(document);
                reportSuccess();
            }

            @Override public void onDocumentNotPosted(MendeleyException e) {
                fail("document not posted: " + e);
            }
        };
        deleteDocumentCallback = new DeleteDocumentCallback() {
            @Override public void onDocumentDeleted(String s) {
                reportSuccess();
            }

            @Override public void onDocumentNotDeleted(MendeleyException e) {
                fail("document not deleted: " + e);
            }
        };
        getDocumentTypesCallback = new GetDocumentTypesCallback() {
            @Override public void onDocumentTypesReceived(Map<String, String> types) {
                setTypes(types);
                reportSuccess();
            }

            @Override public void onDocumentTypesNotReceived(MendeleyException e) {
                fail("document types not received: " + e);
            }
        };
        patchDocumentCallback = new PatchDocumentCallback() {
            @Override
            public void onDocumentPatched(Document document) {
                setPatched(document);
                reportSuccess();
            }

            @Override
            public void onDocumentNotPatched(MendeleyException e) {
                fail("document not patched: " + e);
            }
        };
        getDeletedDocumentsCallback = new GetDeletedDocumentsCallback() {
            @Override
            public void onDeletedDocumentsReceived(List<DocumentId> documentIds, Page page, Date serverDate) {
                setDeletedDocuments(documentIds, page);
                reportSuccess();
            }

            @Override
            public void onDeletedDocumentsNotReceived(MendeleyException e) {
                fail("deleted documents not received: " + e);
            }
        };
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetDocuments() {
        ensureCorrectDocumentsExist();

        getSortedDocuments();

        assertNotNull(documentsRcvd);

        assertEquals("expected five documents", 5, documentsRcvd.size());
        assertEquals("title incorrect",
                "Across Europe by Kangaroo", documentsRcvd.get(0).title);
    }

    public void testGetDocumentsInPages() {
        DocumentRequestParameters params = new DocumentRequestParameters();
        params.limit = 3;
        params.sort = Sort.TITLE;
        expectSdkCall();
        sdk.getDocuments(params, getDocumentsCallback);
        waitForSdkResponse("getting documents");
        assertNotNull(documentsRcvd);
        assertEquals("expected three documents", 3, documentsRcvd.size());
        assertTrue("page must be valid", Page.isValidPage(pageRcvd));

        expectSdkCall();
        sdk.getDocuments(pageRcvd, getDocumentsCallback);
        waitForSdkResponse("getting documents");
        assertNotNull(documentsRcvd);
        assertEquals("expected two documents", 2, documentsRcvd.size());
        assertTrue("page must be invalid", !Page.isValidPage(pageRcvd));
    }

    public void testPostAndDeleteDocument() {
        Document doc = new Document.Builder()
                .setTitle("abc")
                .setType("book")
                .build();

        postDocument(doc);

        assertEquals("abc", postedDocumentRcvd.title);

        deleteDocument(postedDocumentRcvd.id);
    }

    public void testPatchDocument() {
        getSortedDocuments();

        assertEquals("incorrect year", documentsRcvd.get(0).year.intValue(), 2003);

        Document doc = new Document.Builder(documentsRcvd.get(0)).setYear(1066).build();

        patchDocument(doc);
        assertEquals("incorrect title", patchedDocumentRcvd.title, doc.title);
        assertEquals("incorrect year", patchedDocumentRcvd.year.intValue(), 1066);

        getDocumentById(doc.id);
        assertEquals("incorrect year", documentRcvd.year.intValue(), 1066);

        doc = new Document.Builder(documentsRcvd.get(0)).setYear(2003).build();

        patchDocument(doc);
        assertEquals("incorrect title", patchedDocumentRcvd.title, doc.title);
        assertEquals("incorrect year", patchedDocumentRcvd.year.intValue(), 2003);

        getDocumentById(doc.id);
        assertEquals("incorrect year", documentsRcvd.get(0).year.intValue(), 2003);
    }

    public void testGetDocumentTypes() {
        expectSdkCall();
        sdk.getDocumentTypes(getDocumentTypesCallback);
        waitForSdkResponse("getting document types");

        assertNotNull("types must not be null", typesRcvd);
        Set<String> keys = typesRcvd.keySet();
        assertTrue("document types must contain journal", keys.contains("journal"));
        assertTrue("document types must contain book", keys.contains("book"));
    }

    public void testGetDeletedDocuments() {
        ensureCorrectDocumentsExist();

        getDocuments();

        assertTrue("no documents received to delete", documentsRcvd.size() > 0);

        DocumentRequestParameters params = new DocumentRequestParameters();
        SimpleDateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar date = Calendar.getInstance();
        String deletedSince = dateFormat.format(date.getTime());

        List<DocumentId> expectedDocumentIds = new ArrayList <DocumentId>();

        for (Document document : documentsRcvd) {
            DocumentId.Builder docBuilder = new DocumentId.Builder();
            docBuilder.setDocumentId(document.id);
            DocumentId documentId = docBuilder.build();

            expectedDocumentIds.add(documentId);
            deleteDocument(documentId.id);
        }

        getDeletedDocuments(deletedSince, params);

        assertTrue("deleted document id not received", documentIdsRcvd.size() >= expectedDocumentIds.size() );

        for (DocumentId expectedDocumentId : expectedDocumentIds) {
            assertTrue("deleted document id not received", documentIdsRcvd.contains(expectedDocumentId));
        }
    }


    private void setDocuments(List<Document> documents, Page page) {
        documentsRcvd = documents;
        pageRcvd = page;
    }

    private void setDocument(Document document) {
        documentRcvd = document;
    }

    private void setPostedDocumentRcvd(Document document) {
        postedDocumentRcvd = document;
    }

    private void setTypes(Map<String, String> types) {
        typesRcvd = types;
    }

    private void setPatched(Document document) {
        patchedDocumentRcvd = document;
    }

    private void setDeletedDocuments(List<DocumentId> documentIds, Page page) {
        documentIdsRcvd = documentIds;
        pageRcvd = page;
    }

    /**
     * Verify we have the correct set of documents to run the tests, and substitute any that
     * are incorrect. Don't post anything if they are already OK.
     *
     * This method has quadratic logic complexity, but the number of documents is small.
     */
    void ensureCorrectDocumentsExist() {
        getDocuments();

        // Delete any incorrect documents:
        for (Document doc: documentsRcvd) {
            if (!isInFixture(doc)) {
                deleteDocument(doc.id);
            }
        }

        // Post any missing documents:
        for (Document doc : DOCUMENTS) {
            if (!wasReceived(doc)) {
                postDocument(doc);
            }
        }
    }

    private void getDocuments() {
        expectSdkCall();
        sdk.getDocuments(getDocumentsCallback);
        waitForSdkResponse("getting documents");
    }

    private void getSortedDocuments() {
        DocumentRequestParameters params = new DocumentRequestParameters();
        params.sort = Sort.TITLE;
        expectSdkCall();
        sdk.getDocuments(params, getDocumentsCallback);
        waitForSdkResponse("getting documents");
    }

    private void getDeletedDocuments(String deletedSince, DocumentRequestParameters params) {
        expectSdkCall();
        sdk.getDeletedDocuments(deletedSince, params, getDeletedDocumentsCallback);
        waitForSdkResponse("getting deleted documents");
    }

    private void patchDocument(Document doc) {
        expectSdkCall();
        sdk.patchDocument(doc.id, null, doc, patchDocumentCallback);
        waitForSdkResponse("patching document");
    }

    private void getDocumentById(String id) {
        expectSdkCall();
        sdk.getDocument(id, null, getDocumentCallback);
        waitForSdkResponse("getting document");
    }

    private void deleteDocument(String docId) {
        expectSdkCall();
        sdk.deleteDocument(docId, deleteDocumentCallback);
        waitForSdkResponse("deleting document");
    }

    private void postDocument(Document doc) {
        expectSdkCall();
        sdk.postDocument(doc, postDocumentCallback);
        waitForSdkResponse("posting document");
    }

    private boolean isInFixture(Document doc) {
        for (Document fixture : DOCUMENTS) {
            if (documentsEqual(doc, fixture)) {
                return true;
            }
        }
        return false;
    }

    private boolean wasReceived(Document doc) {
        for (Document rcvd : documentsRcvd) {
            if (documentsEqual(doc, rcvd)) {
                return true;
            }
        }
        return false;
    }

    private boolean documentsEqual(Document doc1, Document doc2) {
        return doc1.title.equals(doc2.title)
                && doc1.type.equals(doc2.title)
                && doc1.year.equals(doc2.year);
    }
}
