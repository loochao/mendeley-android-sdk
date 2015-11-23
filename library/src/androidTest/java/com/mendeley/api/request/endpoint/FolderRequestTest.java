package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.model.Document;
import com.mendeley.api.model.Folder;
import com.mendeley.api.request.Request;
import com.mendeley.api.request.SignedInTest;
import com.mendeley.api.testUtils.AssertUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;

public class FolderRequestTest extends SignedInTest {

    @SmallTest
    public void test_GetFoldersRequest_usesCorrectUrl_withParameters() throws Exception {

        String groupId = "test-group_id";

        String paramsString = "?group_id=" + groupId;
        Uri expectedUrl = Uri.parse(MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(paramsString).build();

        FoldersEndpoint.FolderRequestParameters params = new FoldersEndpoint.FolderRequestParameters();
        params.groupId = groupId;

        Uri actualUri = getRequestFactory().getFolders(params).getUrl();

        assertEquals("Get folders url with parameters is wrong", expectedUrl, actualUri);
    }


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
        final List<Folder> actual = getRequestFactory().getFolders().run().resource;

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
        FoldersEndpoint.FolderRequestParameters params = new FoldersEndpoint.FolderRequestParameters();
        params.groupId = null;
        params.limit = 10;
        final List<Folder> actual = getRequestFactory().getFolders(params).run().resource;

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
        final FoldersEndpoint.FolderRequestParameters params = new FoldersEndpoint.FolderRequestParameters();
        params.limit = pageSize;

        final List<Folder> actual = new LinkedList<Folder>();
        Request<List<Folder>>.Response response = getRequestFactory().getFolders(params).run();

        // THEN we receive a folder list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.resource);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", response.next != null);
                response = getRequestFactory().getFolders(response.next).run();
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
        final Folder returnedFolder = getRequestFactory().postFolder(postingFolder).run().resource;

        // THEN we receive the same folder back, with id filled
        AssertUtils.assertFolder(postingFolder, returnedFolder);
        assertNotNull(returnedFolder.id);

        // ...and the folder exists in the server
        AssertUtils.assertFolders(getRequestFactory().getFolders().run().resource, Arrays.asList(postingFolder));
    }

    @SmallTest
    public void test_DeleteFolderUrl_usesrightUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String folderId = "theFolderId";

        final Uri expectedUrl = Uri.parse(MENDELEY_API_BASE_URL).buildUpon().appendPath("folder").appendPath(folderId).build();
        final Uri url = getRequestFactory().deleteFolder(folderId).getUrl();

        assertEquals("Delete folder url is wrong", expectedUrl, url);
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
        getRequestFactory().deleteFolder(deletingFolderId).run();

        // THEN the server does not have the deleted folder any more
        final List<Folder> serverFoldersAfter = getRequestFactory().getFolders().run().resource;
        for (Folder folder : serverFoldersAfter) {
            assertFalse(deletingFolderId.equals(folder.id));
        }
    }

    @SmallTest
    public void test_patchFolder_usesTheRightUrl() throws Exception {
        final String folderId = "theFolderId";

        final Uri expectedUrl = Uri.parse(MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(folderId).build();
        final Uri actualUrl = getRequestFactory().patchFolder(folderId, null).getUrl();

        assertEquals("Patch folder url is wrong", expectedUrl, actualUrl);
    }

    public void test_patchFolder_updatesTheFolderOnServer() throws Exception {
        // GIVEN a folder
        Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // WHEN patched
        final Folder folderPatched = new Folder.Builder(folder)
                .setName(folder.name + "updated")
                .build();

        final Folder returnedFolder = getRequestFactory().patchFolder(folder.id, folderPatched).run().resource;

        // THEN we receive the patched folder
        AssertUtils.assertFolder(folderPatched, returnedFolder);

        // ...and the server has updated the folder
        final Folder folderAfter = getRequestFactory().getFolder(folderPatched.id).run().resource;
        AssertUtils.assertFolder(folderPatched, folderAfter);
    }

    public void test_getDocumentsInFolder_receivesTheCorrectDocumentIds() throws Exception {

        // GIVEN a folder
        final Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // AND documents in that folder
        final List<String> expectedDocIds = new LinkedList<String>();

        for (int i = 0; i < 4; i++) {
            Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title" + i));
            getRequestFactory().postDocumentToFolder(folder.id, document.id).run();
            expectedDocIds.add(document.id);
        }

        // WHEN getting the documents in the folder
        final List<String> actualDocIds = getRequestFactory().getFolderDocumentIds(null, folder.id).run().resource;

        Request<List<String>>.Response response = getRequestFactory().getFolderDocumentIds(new FoldersEndpoint.FolderRequestParameters(), folder.id).run();
        final Set<String> actualDeletedDocIds = new HashSet<String>(response.resource);

        // THEN we have received the documents in that folder
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDocIds, actualDocIds, comparator);
    }


    public void test_postDocumentsToFolder_createsDocumentsInTheFolderInServer() throws Exception {

        // GIVEN a folder
        final Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // AND documents
        List<Document> documents = new LinkedList<Document>();
        final Set<String> expectedDocIds = new HashSet<String>();
        for (int i = 0; i < 4; i++) {
            Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title" + i));
            documents.add(document);
            expectedDocIds.add(document.id);
        }

        // WHEN posting the documents to the folder
        for (Document document : documents) {
            getRequestFactory().postDocumentToFolder(folder.id, document.id).run();
        }

        Request<List<String>>.Response response = getRequestFactory().getFolderDocumentIds(new FoldersEndpoint.FolderRequestParameters(), folder.id).run();
        final Set<String> actualDocIds = new HashSet<String>(response.resource);

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDocIds, actualDocIds, comparator);
    }

    @SmallTest
    public void test_GetFolderRequest_usesCorrectUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String folderId = "folderId";

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(folderId).build();
        final Uri actualUrl = getRequestFactory().getFolder(folderId).getUrl();

        assertEquals("Get folder url is wrong", expectedUrl, actualUrl);
    }

    // TODO create test for getting one specific folder


    @SmallTest
    public void test_getGetFolderDocumentIdsUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String folderId = "theFolderId";
        final String groupdId = "theGroupId";
        final int limit = 69;

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon()
                .appendPath("folders")
                .appendPath(folderId)
                .appendPath("documents")
                .appendQueryParameter("groupId", groupdId)
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();

        final FoldersEndpoint.FolderRequestParameters params = new FoldersEndpoint.FolderRequestParameters();
        params.groupId = groupdId;
        params.limit = limit;

        final Uri actualUrl = getRequestFactory().getFolderDocumentIds(params, folderId).getUrl();

        assertEquals("Get folder document ids url is wrong", expectedUrl, actualUrl);
    }

    // TODO create tests for getting contents of one specific folder

    @SmallTest
    public void test_PostDocumentToFolderRequest_usesCorrectUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String folderId = "theFolderId";

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(folderId).appendPath("documents").build();
        final Uri actualUrl = getRequestFactory().postDocumentToFolder(folderId, "theDocumentId").getUrl();

        assertEquals("Post document to folder url is wrong", expectedUrl, actualUrl);
    }

    // TODO create test for posting document to folder


    @SmallTest
    public void test_getDeleteDocumentFromFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String folderId = "theFolderId";
        final String documentId = "test-document_id";

        final Uri expectedUrl = Uri.parse(MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(folderId).appendPath("documents").appendPath(documentId).build();
        final Uri actualUrl = getRequestFactory().deleteDocumentFromFolder(folderId, documentId).getUrl();

        assertEquals("Delete document from folder url is wrong", expectedUrl, actualUrl);
    }


    // TODO create test for deleting document from folder

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
