package com.mendeley.integration;

import com.mendeley.api.callbacks.document.GetDocumentsCallback;
import com.mendeley.api.callbacks.file.DeleteFileCallback;
import com.mendeley.api.callbacks.file.GetFilesCallback;
import com.mendeley.api.callbacks.file.PostFileCallback;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.FileRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.api.params.Sort;
import com.mendeley.testUtils.SignInException;
import com.mendeley.testUtils.TestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class FileNetworkProviderTest extends DocumentNetworkProviderTest {
    // Names of files associated with each document (null if none)
    private static final String[] FILES = {
            "api.pdf",
            "contact.pdf",
            null,
            "google.pdf"
    };
    // SHA-1 hashes of files associated with each document
    private static final String HASHES[] = {
            "5401faa659733989bc6151eb1b6b027c1ec759eb",
            "2333bd164a7a60c756c44eb56d80ad9db1522d03",
            null,
            "9538225eb639e8450d6dfcd415c3453f74981502"
    };

    private GetFilesCallback getFilesCallback;
    private PostFileCallback postFileCallback;
    private DeleteFileCallback deleteFileCallback;

    private GetDocumentsCallback getDocumentsCallback;

    private List<File> filesRcvd;
    private File postedFileRcvd;
    private List<Document> documentsRcvd;

    @Override
    protected void setUp() throws SignInException, InterruptedException {
        super.setUp();
        getFilesCallback = new GetFilesCallback() {
            @Override
            public void onFilesReceived(List<File> files, Page next, Date serverDate) {
                setFiles(files);
                reportSuccess();
            }

            @Override
            public void onFilesNotReceived(MendeleyException mendeleyException) {
                fail("files not received: " + mendeleyException);
            }
        };
        postFileCallback = new PostFileCallback() {
            @Override public void onFilePosted(File file) {
                setPostedFile(file);
                reportSuccess();
            }

            @Override public void onFileNotPosted(MendeleyException mendeleyException) {
                fail("file not posted: " + mendeleyException);
            }
        };
        deleteFileCallback = new DeleteFileCallback() {
            @Override
            public void onFileDeleted(String fileId) {
                reportSuccess();
            }

            @Override
            public void onFileNotDeleted(MendeleyException mendeleyException) {
                fail("file not deleted: " + mendeleyException);
            }
        };

        getDocumentsCallback = new GetDocumentsCallback() {
            @Override
            public void onDocumentsReceived(List<Document> documents, Page page, Date serverDate) {
                setDocuments(documents);
                reportSuccess();
            }

            @Override
            public void onDocumentsNotReceived(MendeleyException mendeleyException) {
                fail("documents not received: " + mendeleyException);
            }
        };

        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetAllFiles() throws IOException {
        ensureCorrectDocumentsExist();
        ensureCorrectFilesExist();

        getFiles();

        assertEquals("expected 3 files", 3, filesRcvd.size());
    }

    public void testGetFilesForDocument() throws IOException {
        ensureCorrectDocumentsExist();
        ensureCorrectFilesExist();
        getSortedDocuments();

        final int docIndex = 1;

        getFilesForDocument(documentsRcvd.get(docIndex).id);

        assertEquals("expected 1 file", 1, filesRcvd.size());
        assertEquals("file hash doesn't match", HASHES[docIndex], filesRcvd.get(0).fileHash);
    }

    public void testGetFilesForDocumentWithNoFiles() {
        getSortedDocuments();

        final int docIndex = 2;

        getFilesForDocument(documentsRcvd.get(docIndex).id);

        assertEquals("expected no files", 0, filesRcvd.size());
    }

    public void testPostAndDeleteFile() throws IOException {
        expectSdkCall();
        DocumentRequestParameters params = new DocumentRequestParameters();
        params.limit = 1;
        sdk.getDocuments(params, getDocumentsCallback);
        waitForSdkResponse("getting documents");
        assertEquals("expected to get first document", 1, documentsRcvd.size());

        postFile("android.pdf", documentsRcvd.get(0).id);
        assertEquals("file hash doesn't match",
                "e16dca02160b847e8ed2c2d798e62aa6dd331b70", postedFileRcvd.fileHash);

        deleteFile(postedFileRcvd.id);
    }

    private void ensureCorrectFilesExist() throws IOException {
        getSortedDocuments();
        assertTrue("need at least four documents for file tests", documentsRcvd.size() >= 4);

        for(int i = 0; i < 4; i++) {
            final String docId = documentsRcvd.get(i).id;
            final String expectedFileName = FILES[i];

            getFilesForDocument(docId);

            if (expectedFileName == null) {
                deleteFilesRcvd();
            } else if (filesRcvd.size() != 1 || !expectedFileName.equals(filesRcvd.get(0).fileName)) {
                deleteFilesRcvd();
                postFile(expectedFileName, docId);
            }
        }
    }

    private void deleteAllFiles() {
        getFiles();
        deleteFilesRcvd();
    }

    private void getFilesForDocument(String docId) {
        expectSdkCall();
        FileRequestParameters params = new FileRequestParameters();
        params.documentId = docId;
        sdk.getFiles(params, getFilesCallback);
        waitForSdkResponse("getting files for document");
    }

    private void getFiles() {
        expectSdkCall();
        sdk.getFiles(getFilesCallback);
        waitForSdkResponse("getting files");
    }

    private void getSortedDocuments() {
        DocumentRequestParameters params = new DocumentRequestParameters();
        params.sort = Sort.TITLE;
        expectSdkCall();
        sdk.getDocuments(params, getDocumentsCallback);
        waitForSdkResponse("getting documents");
    }

    private void deleteFilesRcvd() {
        for (File file : filesRcvd) {
            deleteFile(file.id);
        }
    }

    private void postFile(String fileName, String docId) throws IOException {
        InputStream inputStream = getContext().getAssets().open(fileName);
        expectSdkCall();
        sdk.postFile("application/pdf", docId, inputStream, fileName, postFileCallback);
        waitForSdkResponse("posting file");
    }

    private void deleteFile(String fileId) {
        expectSdkCall();
        sdk.deleteFile(fileId, deleteFileCallback);
        waitForSdkResponse("deleting file");
    }

    private void setFiles(List<File> files) {
        filesRcvd = files;
    }

    private void setPostedFile(File file) {
        postedFileRcvd = file;
    }

    private void setDocuments(List<Document> documents) {
        documentsRcvd = documents;
    }

}
