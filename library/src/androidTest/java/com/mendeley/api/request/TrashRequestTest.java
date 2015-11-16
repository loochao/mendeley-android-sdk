package com.mendeley.api.request;


import com.mendeley.api.model.Document;
import com.mendeley.api.request.params.DocumentRequestParameters;
import com.mendeley.api.testUtils.AssertUtils;
import com.mendeley.api.util.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class TrashRequestTest extends RequestTest {

    public void test_trashDocument_trashDocumentInServer() throws Exception {
        // GIVEN some documents in the server
        final List<Document> serverDocsBefore = setUpDocumentsInServer(4);

        // WHEN trashing one of them
        final String trashingDocId = serverDocsBefore.get(0).id;
        getSdk().trashDocument(trashingDocId).run();

        // THEN the server does not list the document as non trashed documents
        final List<Document> serverDocsAfter= getSdk().getDocuments().run().resource;
        for (Document doc : serverDocsAfter) {
            assertFalse(trashingDocId.equals(doc.id));
        }
    }

    public void test_getTrashedDocuments_receivesCorrectDocuments() throws Exception {
        // GIVEN some documents trashed after one date
        final List<Document> existingDocs = setUpDocumentsInServer(6);

        final List<Document> expectedTrashedDocs = new ArrayList<Document>();
        for (int i = 0; i < existingDocs.size() / 2; i++) {
            final Document doc = existingDocs.get(i);
            getSdk().trashDocument(doc.id).run();
            expectedTrashedDocs.add(doc);
        }

        // WHEN requesting trashed docs
        final List<Document> actualTrashedDocs = getSdk().getTrashedDocuments().run().resource;


        // THEN we receive the trashed docs
        Comparator<Document> comparator = new Comparator<Document>() {
            @Override
            public int compare(Document d1, Document d2) {
                return d1.title.compareTo(d2.title);
            }
        };

        // THEN we have the expected documents
        AssertUtils.assertSameElementsInCollection(expectedTrashedDocs, actualTrashedDocs, comparator);
    }


    public void test_restoreTrashedDocument_restoresDocumentInServer() throws Exception {
        // GIVEN one trashed document in the server
        final Document restoredDoc = setUpDocumentsInServer(1).get(0);
        getSdk().trashDocument(restoredDoc.id);

        // WHEN restoring it
        getSdk().restoreDocument(restoredDoc.id);

        // THEN the document is no longer trashed
        final List<Document> actual   = getSdk().getDocuments().run().resource;
        final List<Document> expected = Arrays.asList(restoredDoc);

        Comparator<Document> comparator = new Comparator<Document>() {
            @Override
            public int compare(Document d1, Document d2) {
                return d1.title.compareTo(d2.title);
            }
        };
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_deleteTrashedDocument_deletesDocumentFromServer() throws Exception {
        // GIVEN one trashed document in the server
        final String deletedSince = DateUtils.formatMendeleyApiTimestamp(getServerDate());
        final Document deletingDoc = setUpDocumentsInServer(1).get(0);
        getSdk().trashDocument(deletingDoc.id).run();

        // WHEN deleting it
        getSdk().deleteTrashedDocument(deletingDoc.id).run();

        // THEN the document is permanently deleted
        final DocumentRequestParameters params = new DocumentRequestParameters();

        final List<String> expectedDeletedDocIds = Arrays.asList(deletingDoc.id);
        final List<String> actualDeletedDocIds = getSdk().getDeletedDocuments(deletedSince, params).run().resource;

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDeletedDocIds, actualDeletedDocIds, comparator);
    }

    private List<Document> setUpDocumentsInServer(int docCount) throws Exception {
        // GIVEN some documents in the server
        final List<Document> docs = new LinkedList<Document>();
        for (int i = 0; i < docCount; i++) {
            final String title = "title" + getRandom().nextInt();
            final Document doc = new Document.Builder().
                    setType("book").
                    setTitle(title).
                    setYear(getRandom().nextInt(2000)).
                    setAbstractString("abstract" + getRandom().nextInt()).
                    setSource("source" + getRandom().nextInt()).
                    build();

            getTestAccountSetupUtils().setupDocument(doc);
            docs.add(doc);
        }

        return getSdk().getDocuments().run().resource;
    }
}
