package com.mendeley.integration;

import android.test.AndroidTestCase;

import com.mendeley.api.BlockingSdk;
import com.mendeley.api.callbacks.document.DocumentList;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.FileRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.api.util.DateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Collections.sort;

public class FileNetworkBlockingTest extends AndroidTestCase {

    private static final Document[] DOCUMENTS = {
            new Document.Builder()
                    .setTitle("Les Chants de Maldoror")
                    .setType("book")
                    .setYear(1868).build(),
            new Document.Builder()
                    .setTitle("Les Fleurs du mal")
                    .setType("book")
                    .setYear(1857).build(),
            new Document.Builder()
                    .setTitle("Naked Lunch")
                    .setType("book")
                    .setYear(1959).build()
    };

    private BlockingSdk sdk;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testPostFile() throws MendeleyException, IOException {
        ensureCorrectDocumentsExist();

        //GIVEN a document
        final DocumentRequestParameters params = new DocumentRequestParameters();
        params.limit = 1;
        final List<Document> docs = sdk.getDocuments(params).documents;
        assertEquals("expected to get first document", 1, docs.size());

        //WHEN posting a file with this document id
        final String fileName ="android.pdf";
        final Document doc = docs.get(0);
        final InputStream inputStream = getContext().getAssets().open(fileName);
        final File receivedFile = sdk.postFile("application/pdf", doc.id, inputStream, fileName);

        //THEN we receive a File object with the correct document id
        assertNotNull("received file meta data is null", receivedFile);
        assertEquals("received file document id", doc.id, receivedFile.documentId);

        sdk.deleteDocument(doc.id);
    }

    public void testDeleteFile() throws MendeleyException, IOException {
        ensureCorrectDocumentsExist();
        final int expectedReturnCode = 404;

        //GIVEN a document
        final DocumentRequestParameters params = new DocumentRequestParameters();
        params.limit = 1;
        final List<Document> docs = sdk.getDocuments(params).documents;
        assertEquals("expected to get first document", 1, docs.size());

        //and a related file
        final String fileName ="android.pdf";
        final Document doc = docs.get(0);
        final InputStream inputStream = getContext().getAssets().open(fileName);
        final File receivedFile = sdk.postFile("application/pdf", doc.id, inputStream, fileName);

        //WHEN deleting the file
        sdk.deleteFile(receivedFile.id);

        //THEN the file does not exist anymore
        try {
            sdk.deleteFile(receivedFile.id);
            fail("file was not deleted on first call to deleteFile()");
        } catch (HttpResponseException httpResponseException) {
            assertEquals("file should have been deleted already", expectedReturnCode, httpResponseException.httpReturnCode);
        }
    }

    public void testGetFiles() throws MendeleyException, IOException {
        ensureCorrectDocumentsExist();
        final int numFiles = 3;

        //GIVEN a few existing documents
        DocumentRequestParameters params = new DocumentRequestParameters();
        params.limit = 3;
        List<Document> docs = sdk.getDocuments(params).documents;
        assertEquals("expected to get first document", 3, docs.size());

        //and files related to these documents
        String fileName ="android.pdf";
        InputStream inputStream = getContext().getAssets().open(fileName);
        sdk.postFile("application/pdf", docs.get(0).id, inputStream, fileName);
        inputStream = getContext().getAssets().open(fileName);
        sdk.postFile("application/pdf", docs.get(1).id, inputStream, fileName);
        inputStream = getContext().getAssets().open(fileName);
        sdk.postFile("application/pdf", docs.get(2).id, inputStream, fileName);

        //WHEN getting files
        List<File> receivedFiles = sdk.getFiles().files;

        //THEN the correct number of files received
        assertEquals("number of received files", numFiles, receivedFiles.size());

        for (Document doc : docs) {
            sdk.deleteDocument(doc.id);
        }
    }

    public void testGetFilesWithParams() throws MendeleyException, IOException {
        ensureCorrectDocumentsExist();

        final String addedSinceDate = DateUtils.formatMendeleyApiTimestamp(new Date());

        //GIVEN a few existing documents
        DocumentRequestParameters docParams = new DocumentRequestParameters();
        docParams.limit = 3;
        List<Document> docs = sdk.getDocuments(docParams).documents;
        List<String> receivedFilesIds = new ArrayList<>(docs.size());
        assertEquals("expected to get first document", 3, docs.size());

        //and files related to these documents
        String fileName ="android.pdf";
        InputStream inputStream = getContext().getAssets().open(fileName);
        receivedFilesIds.add(sdk.postFile("application/pdf", docs.get(0).id, inputStream, fileName).id);
        inputStream = getContext().getAssets().open(fileName);
        final File fileOfDocument1 = sdk.postFile("application/pdf", docs.get(1).id, inputStream, fileName);
        receivedFilesIds.add(fileOfDocument1.id);
        inputStream = getContext().getAssets().open(fileName);
        receivedFilesIds.add(sdk.postFile("application/pdf", docs.get(2).id, inputStream, fileName).id);

        //WHEN getting files with a document ID
        FileRequestParameters fileParams = new FileRequestParameters();
        fileParams.documentId = docs.get(1).id;
        List<File> receivedFiles = sdk.getFiles(fileParams).files;

        //THEN the right file with the correct document if is received
        assertEquals("number of received files", 1, receivedFiles.size());
        assertEquals("wrong file received", docs.get(1).id, fileOfDocument1.documentId);

        //and WHEN getting files with params indicated limit and addedSince date
        fileParams = new FileRequestParameters();
        fileParams.limit = 3;
        fileParams.addedSince = addedSinceDate;
        receivedFiles = sdk.getFiles(fileParams).files;

        //THEN the correct number of documents with the correct ids were received
        assertEquals("number of received files", 3, receivedFiles.size());
        for (File file : receivedFiles) {
            assertTrue("required file was not received", receivedFilesIds.indexOf(file.id) > -1);
        }

        for (Document doc : docs) {
            sdk.deleteDocument(doc.id);
        }
    }

    public void testGetFilesWithPage() throws MendeleyException, IOException {
        ensureCorrectDocumentsExist();

        //GIVEN a few existing documents
        DocumentRequestParameters docParams = new DocumentRequestParameters();
        docParams.limit = 3;
        List<Document> docs = sdk.getDocuments(docParams).documents;
        assertEquals("expected to get first document", 3, docs.size());

        //and files related to these documents
        String fileName ="android.pdf";
        InputStream inputStream = getContext().getAssets().open(fileName);
        sdk.postFile("application/pdf", docs.get(0).id, inputStream, fileName);
        inputStream = getContext().getAssets().open(fileName);
        sdk.postFile("application/pdf", docs.get(1).id, inputStream, fileName);
        inputStream = getContext().getAssets().open(fileName);
        sdk.postFile("application/pdf", docs.get(2).id, inputStream, fileName);

        //and a Page object
        FileRequestParameters fileParams = new FileRequestParameters();
        fileParams.limit = 2;
        Page nextPage = sdk.getFiles(fileParams).next;

        //WHEN getting files with Page object
        List<File> secondPageFiles = sdk.getFiles(nextPage).files;

        //THEN the correct number of files received
        assertEquals("number of received files", 1, secondPageFiles.size());

        for (Document doc : docs) {
            sdk.deleteDocument(doc.id);
        }
    }

    /**
     * Verify we have the correct set of documents to run the tests, and substitute any that
     * are incorrect. Don't post anything if they are already OK.
     *
     * This method has quadratic logic complexity, but the number of documents is small.
     */
    protected final void ensureCorrectDocumentsExist() throws MendeleyException {
        DocumentList docList = sdk.getDocuments();

        // Delete any incorrect documents:
        for (Document doc: docList.documents) {
            if (!isInFixture(doc)) {
                sdk.deleteDocument(doc.id);
            }
        }

        // Post any missing documents:
        for (Document doc : DOCUMENTS) {
            if (!wasReceived(doc, docList)) {
                sdk.postDocument(doc);
            }
        }
    }

    private boolean isInFixture(Document doc) {
        for (Document fixture : DOCUMENTS) {
            if (documentsEqual(doc, fixture)) {
                return true;
            }
        }
        return false;
    }

    private boolean wasReceived(Document doc, DocumentList docList) {
        for (Document rcvd : docList.documents) {
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
