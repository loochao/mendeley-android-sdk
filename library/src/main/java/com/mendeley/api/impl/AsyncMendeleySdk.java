package com.mendeley.api.impl;

import android.os.AsyncTask;

import com.mendeley.api.MendeleySdk;
import com.mendeley.api.callbacks.RequestHandle;
import com.mendeley.api.callbacks.document.DeleteDocumentCallback;
import com.mendeley.api.callbacks.document.GetDeletedDocumentsCallback;
import com.mendeley.api.callbacks.document.GetDocumentCallback;
import com.mendeley.api.callbacks.document.GetDocumentTypesCallback;
import com.mendeley.api.callbacks.document.GetDocumentsCallback;
import com.mendeley.api.callbacks.document.PatchDocumentCallback;
import com.mendeley.api.callbacks.document.PostDocumentCallback;
import com.mendeley.api.callbacks.document.TrashDocumentCallback;
import com.mendeley.api.callbacks.file.DeleteFileCallback;
import com.mendeley.api.callbacks.file.GetFileCallback;
import com.mendeley.api.callbacks.file.GetFilesCallback;
import com.mendeley.api.callbacks.file.PostFileCallback;
import com.mendeley.api.callbacks.folder.DeleteFolderCallback;
import com.mendeley.api.callbacks.folder.DeleteFolderDocumentCallback;
import com.mendeley.api.callbacks.folder.GetFolderCallback;
import com.mendeley.api.callbacks.folder.GetFolderDocumentIdsCallback;
import com.mendeley.api.callbacks.folder.GetFoldersCallback;
import com.mendeley.api.callbacks.folder.PatchFolderCallback;
import com.mendeley.api.callbacks.folder.PostDocumentToFolderCallback;
import com.mendeley.api.callbacks.folder.PostFolderCallback;
import com.mendeley.api.callbacks.group.GetGroupCallback;
import com.mendeley.api.callbacks.group.GetGroupMembersCallback;
import com.mendeley.api.callbacks.group.GetGroupsCallback;
import com.mendeley.api.callbacks.profile.GetProfileCallback;
import com.mendeley.api.callbacks.trash.RestoreDocumentCallback;
import com.mendeley.api.callbacks.utils.GetImageCallback;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.Folder;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.FileRequestParameters;
import com.mendeley.api.params.FolderRequestParameters;
import com.mendeley.api.params.GroupRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.api.params.View;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Executor;

/**
 * Adds async calls to BaseMendeleySdk.
 */
public abstract class AsyncMendeleySdk extends BaseMendeleySdk implements MendeleySdk {
    private Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;

    @Override
    public boolean isSignedIn() {
        return authenticationManager != null && authenticationManager.isSignedIn();
    }

    @Override
    public void signOut() {
        if (authenticationManager != null) {
            authenticationManager.clearCredentials();
        }
    }

    /* DOCUMENTS ASYNC */

    @Override
    public RequestHandle getDocuments(final DocumentRequestParameters parameters, final GetDocumentsCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return documentNetworkProvider.doGetDocuments(parameters, callback);
            }
        });
    }

    @Override
    public RequestHandle getDocuments(GetDocumentsCallback callback) {
        return getDocuments((DocumentRequestParameters) null, callback);
    }

    @Override
    public RequestHandle getDocuments(final Page next, final GetDocumentsCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return documentNetworkProvider.doGetDocuments(next, callback);
            }
        });
    }

    @Override
    public void getDocument(final String documentId, final View view, final GetDocumentCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                documentNetworkProvider.doGetDocument(documentId, view, callback);
                return null;
            }
        });
    }

    @Override
    public RequestHandle getDeletedDocuments(final String deletedSince, final DocumentRequestParameters parameters, final GetDeletedDocumentsCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return documentNetworkProvider.doGetDeletedDocuments(deletedSince, parameters, callback);
            }
        });
    }

    @Override
    public RequestHandle getDeletedDocuments(final Page next, final GetDeletedDocumentsCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return documentNetworkProvider.doGetDeletedDocuments(next, callback);
            }
        });
    }

    @Override
    public void postDocument(final Document document, final PostDocumentCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                documentNetworkProvider.doPostDocument(document, callback);
                return null;
            }
        });
    }

    @Override
    public RequestHandle patchDocument(final String documentId, final Date date, final Document document, final PatchDocumentCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return documentNetworkProvider.doPatchDocument(documentId, date, document, callback);
            }
        });
    }

    @Override
    public void trashDocument(final String documentId, final TrashDocumentCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                documentNetworkProvider.doPostTrashDocument(documentId, callback);
                return null;
            }
        });
    }

    @Override
    public void deleteDocument(final String documentId, final DeleteDocumentCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                documentNetworkProvider.doDeleteDocument(documentId, callback);
                return null;
            }
        });
    }

    @Override
    public RequestHandle getDocumentTypes(final GetDocumentTypesCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return documentNetworkProvider.doGetDocumentTypes(callback);
            }
        });
    }

    /* FILES */

    @Override
    public RequestHandle getFiles(final FileRequestParameters parameters, final GetFilesCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return fileNetworkProvider.doGetFiles(parameters, callback);
            }
        });
    }

    @Override
    public RequestHandle getFiles(GetFilesCallback callback) {
        return getFiles((FileRequestParameters) null, callback);
    }

    @Override
    public RequestHandle getFiles(final Page next, final GetFilesCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return fileNetworkProvider.doGetFiles(next, callback);
            }
        });
    }

    @Override
    public void getFile(final String fileId, final java.io.File targetFile, final GetFileCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                fileNetworkProvider.doGetFile(fileId, targetFile, callback);
                return null;
            }
        });
    }

    @Override
    public void cancelDownload(final String fileId) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                fileNetworkProvider.cancelDownload(fileId);
                return null;
            }
        });
    }

    @Override
    public void postFile(final String contentType, final String documentId, final String filePath, final PostFileCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                fileNetworkProvider.doPostFile(contentType, documentId, filePath, callback);
                return null;
            }
        });
    }

    @Override
    public void postFile(final String contentType, final String documentId, final InputStream inputStream, final String fileName, final PostFileCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                fileNetworkProvider.doPostFile(contentType, documentId, inputStream, fileName, callback);
                return null;
            }
        });
    }

    @Override
    public void deleteFile(final String fileId, final DeleteFileCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                fileNetworkProvider.doDeleteFile(fileId, callback);
                return null;
            }
        });
    }

    /* PROFILES ASYNC */

    @Override
    public void getMyProfile(final GetProfileCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                profileNetworkProvider.doGetMyProfile(callback);
                return null;
            }
        });
    }

    @Override
    public void getProfile(final String profileId, final GetProfileCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                profileNetworkProvider.doGetProfile(profileId, callback);
                return null;
            }
        });
    }

    /* FOLDERS */

    @Override
    public RequestHandle getFolders(final FolderRequestParameters parameters, final GetFoldersCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return folderNetworkProvider.doGetFolders(parameters, callback);
            }
        });
    }

    @Override
    public RequestHandle getFolders(GetFoldersCallback callback) {
        return getFolders((FolderRequestParameters) null, callback);
    }

    @Override
    public RequestHandle getFolders(final Page next, final GetFoldersCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return folderNetworkProvider.doGetFolders(next, callback);
            }
        });
    }

    @Override
    public void getFolder(final String folderId, final GetFolderCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                folderNetworkProvider.doGetFolder(folderId, callback);
                return null;
            }
        });
    }

    @Override
    public void postFolder(final Folder folder, final PostFolderCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                folderNetworkProvider.doPostFolder(folder, callback);
                return null;
            }
        });
    }

    @Override
    public RequestHandle patchFolder(final String folderId, final Folder folder, final PatchFolderCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return folderNetworkProvider.doPatchFolder(folderId, folder, callback);
            }
        });
    }

    @Override
    public void getFolderDocumentIds(final FolderRequestParameters parameters, final String folderId, final GetFolderDocumentIdsCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                folderNetworkProvider.doGetFolderDocumentIds(parameters, folderId, callback);
                return null;
            }
        });
    }

    @Override
    public void getFolderDocumentIds(final Page next, final String folderId, final GetFolderDocumentIdsCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                folderNetworkProvider.doGetFolderDocumentIds(next, folderId, callback);
                return null;
            }
        });
    }

    @Override
    public void postDocumentToFolder(final String folderId, final String documentId, final PostDocumentToFolderCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                folderNetworkProvider.doPostDocumentToFolder(folderId, documentId, callback);
                return null;
            }
        });
    }

    @Override
    public void deleteFolder(final String folderId, final DeleteFolderCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                folderNetworkProvider.doDeleteFolder(folderId, callback);
                return null;
            }
        });
    }

    @Override
    public void deleteDocumentFromFolder(final String folderId, final String documentId, final DeleteFolderDocumentCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                folderNetworkProvider.doDeleteDocumentFromFolder(folderId, documentId, callback);
                return null;
            }
        });
    }

    /* UTILITIES */

    @Override
    public void getImage(final String url, final GetImageCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                utilsNetworkProvider.doGetImage(url, callback);
                return null;
            }
        });
    }

    /* GROUPS ASYNC */

    @Override
    public RequestHandle getGroups(final GroupRequestParameters parameters, final GetGroupsCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return groupNetworkProvider.doGetGroups(parameters, callback);
            }
        });
    }

    @Override
    public RequestHandle getGroups(final Page next, final GetGroupsCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return groupNetworkProvider.doGetGroups(next, callback);
            }
        });
    }

    @Override
    public void getGroup(final String groupId, final GetGroupCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                groupNetworkProvider.doGetGroup(groupId, callback);
                return null;
            }
        });
    }

    @Override
    public void getGroupMembers(final GroupRequestParameters parameters, final String groupId, final GetGroupMembersCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                groupNetworkProvider.doGetGroupMembers(parameters, groupId, callback);
                return null;
            }
        });
    }

    @Override
    public void getGroupMembers(final Page next, final String groupId, final GetGroupMembersCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                groupNetworkProvider.doGetGroupMembers(next, groupId, callback);
                return null;
            }
        });
    }

    /* TRASH ASYNC */

    @Override
    public RequestHandle getTrashedDocuments(final DocumentRequestParameters parameters, final GetDocumentsCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return trashNetworkProvider.doGetDocuments(parameters, callback);
            }
        });
    }

    @Override
    public RequestHandle getTrashedDocuments(final GetDocumentsCallback callback) {
        return getTrashedDocuments((DocumentRequestParameters) null, callback);
    }

    @Override
    public RequestHandle getTrashedDocuments(final Page next, final GetDocumentsCallback callback) {
        return run(new Command() {
            @Override
            public RequestHandle exec() {
                return trashNetworkProvider.doGetDocuments(next, callback);
            }
        });
    }

    @Override
    public void restoreDocument(final String documentId, final RestoreDocumentCallback callback) {
        run(new Command() {
            @Override
            public RequestHandle exec() {
                trashNetworkProvider.doPostRecoverDocument(documentId, callback);
                return null;
            }
        });
    }

    /* CONTROL */

    @Override
    public MendeleySdk setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Return the executor used to run background tasks.
     */
    @Override
    public Executor getExecutor() {
        return executor;
    }
}
