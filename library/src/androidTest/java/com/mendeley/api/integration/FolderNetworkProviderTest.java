package com.mendeley.api.integration;

import com.mendeley.api.MendeleySdk;
import com.mendeley.api.callbacks.folder.DeleteFolderCallback;
import com.mendeley.api.callbacks.folder.GetFoldersCallback;
import com.mendeley.api.callbacks.folder.PatchFolderCallback;
import com.mendeley.api.callbacks.folder.PostFolderCallback;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Folder;
import com.mendeley.api.params.Page;
import com.mendeley.api.testUtils.SignInException;
import com.mendeley.api.testUtils.TestUtils;

import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;

public class FolderNetworkProviderTest extends BaseNetworkProviderTest {
    private static final String[] FOLDERS = {
            "Chocolate",
            "Rocket Science",
            "Nuclear Physics"
    };

    private static final Comparator<Folder> FOLDER_COMPARATOR = new Comparator<Folder>() {
        @Override
        public int compare(Folder folder1, Folder folder2) {
            return folder1.name.compareTo(folder2.name);
        }
    };

    private MendeleySdk sdk;

    private List<Folder> foldersRcvd;
    private Folder postedFolderRcvd;
    private Folder patchedFolderRcvd;

    private GetFoldersCallback getFoldersCallback;
    private PostFolderCallback postFolderCallback;
    private DeleteFolderCallback deleteFolderCallback;
    private PatchFolderCallback patchFolderCallback;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        getFoldersCallback = new GetFoldersCallback() {
            @Override
            public void onFoldersReceived(List<Folder> folders, Page next) {
                setFolders(folders);
                reportSuccess();
            }

            @Override
            public void onFoldersNotReceived(MendeleyException mendeleyException) {
                fail("folders not received");
            }
        };
        postFolderCallback = new PostFolderCallback() {
            @Override
            public void onFolderPosted(Folder folder) {
                setPostedFolder(folder);
                reportSuccess();
            }

            @Override
            public void onFolderNotPosted(MendeleyException mendeleyException) {
                fail("folder not posted");
            }
        };
        deleteFolderCallback = new DeleteFolderCallback() {
            @Override
            public void onFolderDeleted(String folderId) {
                reportSuccess();
            }

            @Override
            public void onFolderNotDeleted(MendeleyException mendeleyException) {
                fail("folder not deleted");
            }
        };
        patchFolderCallback = new PatchFolderCallback() {
            @Override
            public void onFolderPatched(Folder folder) {
                setPatchedFolder(folder);
                reportSuccess();
            }

            @Override
            public void onFolderNotPatched(MendeleyException mendeleyException) {
                fail("folder not patched");
            }
        };

        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetFolders() throws InterruptedException {
        ensureCorrectFoldersExist();

        getFolders();

        assertEquals("expected three foldersRcvd", 3, foldersRcvd.size());
        assertEquals("folder name incorrect", "Chocolate", foldersRcvd.get(0).name);
        assertEquals("folder name incorrect", "Nuclear Physics", foldersRcvd.get(1).name);
        assertEquals("folder name incorrect", "Rocket Science", foldersRcvd.get(2).name);
    }

    public void testPostAndDeleteFolder() {
        Folder folder = new Folder.Builder("Lampposts").build();

        postFolder(folder);

        assertEquals("folder name incorrect", "Lampposts", postedFolderRcvd.name);

        deleteFolder(postedFolderRcvd.id);
    }

    public void testPatchFolder() {
        getFolders();

        String originalName = foldersRcvd.get(0).name;
        assertEquals("folder name incorrect", originalName, "Chocolate");

        Folder folder = new Folder.Builder(foldersRcvd.get(0))
                .setName("Zero gravity croquet").build();

        patchFolder(folder);
        assertEquals("folder name incorrect", patchedFolderRcvd.name, "Zero gravity croquet");

        getFolders();
        assertEquals("folder name incorrect", "Zero gravity croquet", foldersRcvd.get(2).name);

        folder = new Folder.Builder(foldersRcvd.get(2))
                .setName(originalName).build();

        patchFolder(folder);
        assertEquals("folder name incorrect", patchedFolderRcvd.name, "Chocolate");
    }

    private void patchFolder(Folder folder) {
        expectSdkCall();
        sdk.patchFolder(folder.id, folder, patchFolderCallback);
        waitForSdkResponse("patching folder");
    }

    private void deleteFolder(String folderId) {
        expectSdkCall();
        sdk.deleteFolder(folderId, deleteFolderCallback);
        waitForSdkResponse("deleting folder");
    }

    private void postFolder(Folder folder) {
        expectSdkCall();
        sdk.postFolder(folder, postFolderCallback);
        waitForSdkResponse("posting folder");
    }

    private void ensureCorrectFoldersExist() {
        getFolders();

        // Delete any incorrect folders:
        for (Folder folder : foldersRcvd) {
            if (!isInFixture(folder)) {
                deleteFolder(folder.id);
            }
        }

        // Post any missing folders:
        for (String folderName : FOLDERS) {
            if (!wasReceived(folderName)) {
                Folder folder = new Folder.Builder(folderName).build();
                postFolder(folder);
            }
        }
    }

    private void getFolders() {
        expectSdkCall();
        sdk.getFolders(getFoldersCallback);
        waitForSdkResponse("getting foldersRcvd");
    }

    private void setPostedFolder(Folder folder) {
        this.postedFolderRcvd = folder;
    }

    private void setPatchedFolder(Folder folder) {
        this.patchedFolderRcvd = folder;
    }

    private void setFolders(List<Folder> folders) {
        sort(folders, FOLDER_COMPARATOR);
        this.foldersRcvd = folders;
    }

    private boolean isInFixture(Folder folder) {
        for (String fixture : FOLDERS) {
            if (folder.name.equals(fixture)) {
                return true;
            }
        }
        return false;
    }

    private boolean wasReceived(String folderName) {
        for (Folder rcvd : foldersRcvd) {
            if (rcvd.name.equals(folderName)) {
                return true;
            }
        }
        return false;
    }
}
