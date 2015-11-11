package com.mendeley.api.integration;

import com.mendeley.api.model.Document;
import com.mendeley.api.model.DocumentId;
import com.mendeley.api.model.Folder;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.request.params.FolderRequestParameters;
import com.mendeley.api.request.params.Page;
import com.mendeley.api.testUtils.AssertUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FolderEndpointBlockingTest extends EndpointBlockingTest {

    public void test_getFolders_withoutParameters_receivesCorrectFolders() throws Exception {
        // GIVEN some folders
        final List<Folder> expected = new LinkedList<Folder>();

        Folder parentFolder = getTestAccountSetupUtils().setupFolder(createParentFolder());
        expected.add(parentFolder);

        for (int i = 0; i < 3; i++) {
            final Folder subFolder = createSubFolder(parentFolder.id);
            getTestAccountSetupUtils().setupFolder(subFolder);
            expected.add(subFolder);
        }

        // WHEN getting folders
        final List<Folder> actual = getSdk().getFolders().run().resource;

        Comparator<Folder> comparator = new Comparator<Folder>() {
            @Override
            public int compare(Folder f1, Folder f2) {
                return f1.name.compareTo(f2.name);
            }
        };

        // THEN we have the expected folders
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getFolders_withParameters_receivesCorrectFolders() throws Exception {
        // GIVEN some folders
        final List<Folder> expected = new LinkedList<Folder>();

        Folder parentFolder = getTestAccountSetupUtils().setupFolder(createParentFolder());
        expected.add(parentFolder);

        for (int i = 0; i < 3; i++) {
            final Folder subFolder = createSubFolder(parentFolder.id);
            getTestAccountSetupUtils().setupFolder(subFolder);
            expected.add(subFolder);
        }

        // WHEN getting folders with parameters
        FolderRequestParameters params = new FolderRequestParameters();
        params.groupId = null;
        params.limit = 10;
        final List<Folder> actual = getSdk().getFolders(params).run().resource;

        Comparator<Folder> comparator = new Comparator<Folder>() {
            @Override
            public int compare(Folder f1, Folder f2) {
                return f1.name.compareTo(f2.name);
            }
        };

        // THEN we have the expected folders
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_geFolders_whenMoreThanOnePage_receivesCorrectFolders() throws Exception {

        // GIVEN a number of folders greater than the page size
        final int pageSize = 4;
        final int pageCount = 2;
        final List<Folder> expected = new LinkedList<Folder>();

        Folder parentFolder = getTestAccountSetupUtils().setupFolder(createParentFolder());
        expected.add(parentFolder);
        expected.add(getTestAccountSetupUtils().setupFolder(createParentFolder()));

        for (int i = 0; i < 5; i++) {
            final Folder subFolder = createSubFolder(parentFolder.id);
            getTestAccountSetupUtils().setupFolder(subFolder);
            expected.add(subFolder);
        }

        // WHEN getting folders
        final FolderRequestParameters params = new FolderRequestParameters();
        params.limit = pageSize;

        final List<Folder> actual = new LinkedList<Folder>();
        RequestResponse<List<Folder>> response = getSdk().getFolders(params).run();

        // THEN we receive a folder list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.resource);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", Page.isValidPage(response.next));
                response = getSdk().getFolders(response.next).run();
            }
        }

        Comparator<Folder> comparator = new Comparator<Folder>() {
            @Override
            public int compare(Folder f1, Folder f2) {
                return f1.name.compareTo(f2.name);
            }
        };

        // THEN we have the expected folders
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_postFolder_createsFolderInServer() throws Exception {

        // GIVEN a folder
        final Folder postingFolder = createParentFolder();

        // WHEN posting it
        final Folder returnedFolder = getSdk().postFolder(postingFolder).run().resource;

        // THEN we receive the same folder back, with id filled
        AssertUtils.assertFolder(postingFolder, returnedFolder);
        assertNotNull(returnedFolder.id);

        // ...and the folder exists in the server
        AssertUtils.assertFolders(getSdk().getFolders().run().resource, Arrays.asList(postingFolder));
    }

    public void test_postDocumentsToFolder_createsDocumentsInTheFolderInServer() throws Exception {

        // GIVEN a folder
        final Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // AND documents
        List<Document> documents = new LinkedList<Document>();
        final Set<DocumentId> expectedDeletedDocIds = new HashSet<DocumentId>();
        for (int i = 0; i < 4; i++) {
            Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title" + i));
            documents.add(document);
            expectedDeletedDocIds.add(new DocumentId.Builder().setDocumentId(document.id).build());
        }

        // WHEN posting the documents to the folder
        for (Document document : documents) {
            getSdk().postDocumentToFolder(folder.id, document.id).run();
        }

        RequestResponse<List<DocumentId>> response = getSdk().getFolderDocumentIds(new FolderRequestParameters(), folder.id).run();
        final Set<DocumentId> actualDeletedDocIds = new HashSet<DocumentId>(response.resource);

        Comparator<DocumentId> comparator = new Comparator<DocumentId>() {
            @Override
            public int compare(DocumentId lhs, DocumentId rhs) {
                return lhs.id.compareTo(rhs.id);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDeletedDocIds, actualDeletedDocIds, comparator);
    }

    public void test_deleteFolder_removesTheFolderFromServer() throws Exception {
        // GIVEN some folders
        final List<Folder> serverFoldersBefore = new LinkedList<Folder>();

        for (int i = 0; i < 5; i++) {
            Folder parentFolder = getTestAccountSetupUtils().setupFolder(createParentFolder());
            serverFoldersBefore.add(parentFolder);
        }

        // WHEN deleting one of them
        final String deletingFolderId = serverFoldersBefore.get(0).id;
        getSdk().deleteFolder(deletingFolderId).run();

        // THEN the server does not have the deleted folder any more
        final List<Folder> serverFoldersAfter = getSdk().getFolders().run().resource;
        for (Folder folder : serverFoldersAfter) {
            assertFalse(deletingFolderId.equals(folder.id));
        }
    }

    public void test_patchFolder_updatesTheFolderOnServer() throws Exception {
        // GIVEN a folder
        Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // WHEN patched
        final Folder folderPatched = new Folder.Builder(folder)
                .setName(folder.name + "updated")
                .build();

        final Folder returnedFolder = getSdk().patchFolder(folder.id, folderPatched).run().resource;

        // THEN we receive the patched folder
        AssertUtils.assertFolder(folderPatched, returnedFolder);

        // ...and the server has updated the folder
        final Folder folderAfter = getSdk().getFolder(folderPatched.id).run().resource;
        AssertUtils.assertFolder(folderPatched, folderAfter);
    }

    private Folder createParentFolder() {
        Folder folder = new Folder.Builder()
                .setName("parent folder" + getRandom().nextInt())
                .build();

        return folder;
    }

    private Folder createSubFolder(String parentFolderId) {
        Folder folder = new Folder.Builder()
                .setName("sub folder" + getRandom().nextInt())
                .setParentId(parentFolderId)
                .build();

        return folder;
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

}
