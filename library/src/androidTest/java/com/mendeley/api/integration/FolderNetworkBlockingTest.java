package com.mendeley.api.integration;

import android.test.AndroidTestCase;

import com.mendeley.api.BlockingSdk;
import com.mendeley.api.callbacks.folder.FolderList;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Folder;
import com.mendeley.api.testUtils.SignInException;
import com.mendeley.api.testUtils.TestUtils;

import java.util.Comparator;

import static java.util.Collections.sort;

public class FolderNetworkBlockingTest extends AndroidTestCase {
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

    private BlockingSdk sdk;

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetFolders() throws MendeleyException {
        ensureCorrectFoldersExist();

        FolderList folderList = getFolders();

        assertEquals("expected three folders", 3, folderList.folders.size());
        assertEquals("folder name incorrect", "Chocolate", folderList.folders.get(0).name);
        assertEquals("folder name incorrect", "Nuclear Physics", folderList.folders.get(1).name);
        assertEquals("folder name incorrect", "Rocket Science", folderList.folders.get(2).name);
    }

    public void testPostAndDeleteFolder() throws MendeleyException {
        Folder folder = new Folder.Builder("Lampposts").build();

        Folder rcvd = sdk.postFolder(folder);

        assertEquals("folder name incorrect", "Lampposts", rcvd.name);

        sdk.deleteFolder(rcvd.id);

        int responseCode = -1;

        try {
            sdk.getFolder(rcvd.id);
        } catch (HttpResponseException e) {
            responseCode = e.httpReturnCode;
        }

        assertEquals("folder not deleted", 404, responseCode);
    }

    public void testPatchFolder() throws MendeleyException {
        FolderList folderList = getFolders();

        String originalName = folderList.folders.get(0).name;
        assertEquals("folder name incorrect", originalName, "Chocolate");

        Folder folder = new Folder.Builder(folderList.folders.get(0))
                .setName("Zero gravity croquet").build();

        Folder patched = sdk.patchFolder(folder.id, folder);
        assertEquals("folder name incorrect", patched.name, "Zero gravity croquet");

        folderList = getFolders();
        assertEquals("folder name incorrect", "Zero gravity croquet", folderList.folders.get(2).name);

        folder = new Folder.Builder(folderList.folders.get(2))
                .setName(originalName).build();

        patched = sdk.patchFolder(folder.id, folder);
        assertEquals("folder name incorrect", patched.name, "Chocolate");
    }

    private void ensureCorrectFoldersExist() throws MendeleyException {
        FolderList folderList = getFolders();

        // Delete any incorrect folders:
        for (Folder folder : folderList.folders) {
            if (!isInFixture(folder)) {
                sdk.deleteFolder(folder.id);
            }
        }

        // Post any missing folders:
        for (String folderName : FOLDERS) {
            if (!wasReceived(folderName, folderList)) {
                Folder folder = new Folder.Builder(folderName).build();
                sdk.postFolder(folder);
            }
        }
    }

    private FolderList getFolders() throws MendeleyException {
        FolderList folderList = sdk.getFolders();
        sort(folderList.folders, FOLDER_COMPARATOR);
        return folderList;
    }

    private boolean isInFixture(Folder folder) {
        for (String fixture : FOLDERS) {
            if (folder.name.equals(fixture)) {
                return true;
            }
        }
        return false;
    }

    private boolean wasReceived(String folderName, FolderList folderList) {
        for (Folder rcvd : folderList.folders) {
            if (rcvd.name.equals(folderName)) {
                return true;
            }
        }
        return false;
    }
}
