package com.mendeley.api.request.endpoint;

import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.request.SignedInTest;
import com.mendeley.api.request.params.FileRequestParameters;
import com.mendeley.api.request.params.Page;
import com.mendeley.api.testUtils.AssertUtils;
import com.mendeley.api.util.DateUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class FileRequestTest extends SignedInTest {

    public void test_getFiles_withoutParameters_receivesCorrectFiles() throws Exception {
        // GIVEN a document with files
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));

        final List<File> expected = new LinkedList<File>();
        String[] fileNames = new String[]{"android.pdf", "api.pdf", "contact.pdf", "google.pdf"};
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = getTestAccountSetupUtils().setupFile(document.id, fileName, getContext().getAssets().open(fileName));
            expected.add(file);
        }

        // WHEN getting files
        final List<File> actual = getRequestFactory().getFiles().run().resource;

        Comparator<File> comparator = new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.fileHash.compareTo(f2.fileHash);
            }
        };

        // THEN we have the expected files
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getFiles_withParameters_receivesCorrectFiles() throws Exception {
        // GIVEN a document with files
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        final String currentDate = DateUtils.formatMendeleyApiTimestamp(getServerDate());

        final List<File> expected = new LinkedList<File>();
        String[] fileNames = new String[]{"android.pdf", "api.pdf", "contact.pdf", "google.pdf"};
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = getTestAccountSetupUtils().setupFile(document.id, fileName, getContext().getAssets().open(fileName));
            expected.add(file);
        }

        // WHEN getting files with parameters
        FileRequestParameters params = new FileRequestParameters();
        params.addedSince = currentDate;
        params.documentId = document.id;

        final List<File> actual = getRequestFactory().getFiles(params).run().resource;

        Comparator<File> comparator = new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.fileHash.compareTo(f2.fileHash);
            }
        };

        // THEN we have the expected files
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getFiles_whenMoreThanOnePage_receivesCorrectFiles() throws Exception {

        // GIVEN a number of files greater than the page size
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        final int pageSize = 3;
        final int pageCount = 2;

        final List<File> expected = new LinkedList<File>();
        String[] fileNames = new String[]{"android.pdf", "api.pdf", "contact.pdf", "google.pdf"};
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = getTestAccountSetupUtils().setupFile(document.id, fileName, getContext().getAssets().open(fileName));
            expected.add(file);
        }

        // WHEN getting files
        final FileRequestParameters params = new FileRequestParameters();
        params.limit = pageSize;

        RequestResponse<List<File>> response = getRequestFactory().getFiles(params).run();

        final List<File> actual = new LinkedList<File>();
        // THEN we receive a files list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.resource);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", Page.isValidPage(response.next));
                response = getRequestFactory().getFiles(response.next).run();
            }
        }

        Comparator<File> comparator = new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.fileHash.compareTo(f2.fileHash);
            }
        };

        // THEN we have the expected files
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }


    public void test_postFile_createsFileInServer() throws Exception {

        // GIVEN a file
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        String fileName = "android.pdf";
        File postingFile = createFile(document.id);

        // WHEN posting it
        final File returnedFile = getRequestFactory().postFileBinary(postingFile.mimeType, document.id, getContext().getAssets().open(fileName), fileName).run().resource;

        // THEN we receive the same file back, with id filled
        AssertUtils.assertFile(postingFile, returnedFile);
        assertNotNull(returnedFile.id);

        // ...and the file exists in the server
        AssertUtils.assertFiles(getRequestFactory().getFiles().run().resource, Arrays.asList(postingFile));
    }

    public void test_deleteFile_removesTheFileFromServer() throws Exception {
        // GIVEN some files
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));

        final List<File> serverFilesBefore = new LinkedList<File>();
        String[] fileNames = new String[]{"android.pdf", "api.pdf", "contact.pdf", "google.pdf"};
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = getTestAccountSetupUtils().setupFile(document.id, fileName, getContext().getAssets().open(fileName));
            serverFilesBefore.add(file);
        }

        // WHEN deleting one of them
        final String deletingFileId = serverFilesBefore.get(0).id;
        getRequestFactory().deleteFile(deletingFileId).run();;

        // THEN the server does not have the deleted file any more
        final List<File> serverFilesAfter = getRequestFactory().getFiles().run().resource;
        for (File file : serverFilesAfter) {
            assertFalse(deletingFileId.equals(file.id));
        }
    }

    private Document createDocument(String title) throws Exception {
        final Document doc = new Document.Builder().
                setType("book").
                setTitle(title).
                setYear(getRandom().nextInt(2000)).
                setAbstractString("abstract" + getRandom().nextInt()).
                setSource("source" + getRandom().nextInt()).
                build();

        return doc;
    }

    private File createFile(String documentId) {
        final File file = new File.Builder()
                .setDocumentId(documentId)
                .setMimeType("application/pdf")
                .build();

        return file;
    }
}
