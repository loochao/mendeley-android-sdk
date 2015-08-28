/*
 * Copyright 2014 Mendeley Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mendeley.api;

import android.app.Activity;

import com.mendeley.api.callbacks.MendeleySignInInterface;
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
 * Main entry points for making calls to the Mendeley SDK.
 */
public interface MendeleySdk {

    /* SIGNING IN */

    /**
     * Sign the user in.
     * @param activity used for creating the sign-in activity.
     * @param signInCallback used to receive sign in/out events. May be null.
     * @param showSignUpScreen whether to show the screen with UI to create a new account before the sign-in dialog
     */
    void signIn(Activity activity, MendeleySignInInterface signInCallback, boolean showSignUpScreen);

    /**
     * Determine if the user is signed in.
     * <p>
     * If the user has successfully signed in at any point in the past, they will remain signed
     * in, even if the application is stopped and removed from memory, because the credentials
     * are stored persistently.
     */
    boolean isSignedIn();

    /**
     * Sign out from the user's account.
     * <p>
     * All stored credentials are erased.
     */
    void signOut();

    /**
     * @returns the access token to authenticate against the Mendeley API
     */
    String getAccessToken();

    /* DOCUMENTS */

    /**
     * Retrieve a list of documents in the user's library.
     */
    RequestHandle getDocuments(DocumentRequestParameters parameters, GetDocumentsCallback callback);

    /**
     * Retrieve a list of documents in the user's library.
     */
    RequestHandle getDocuments(GetDocumentsCallback callback);

    /**
     * Retrieve subsequent pages of documents in the user's library.
     *
     * @param next reference to next page returned by a previous onDocumentsReceived() callback.
     */
    RequestHandle getDocuments(Page next, GetDocumentsCallback callback);

    /**
     * Retrieve a single document, specified by ID.
     *
     * @param documentId the document id to get.
     * @param view extended document view. If null, only core fields are returned.
     */
    void getDocument(String documentId, View view, GetDocumentCallback callback);

    /**
     * Retrieve a list of deleted documents in the user's library.
     *
     * @param deletedSince only return documents deleted since this timestamp. Should be supplied in ISO 8601 format.
     * @param parameters holds optional query parameters, will be ignored if null
     */
    RequestHandle getDeletedDocuments(String deletedSince, DocumentRequestParameters parameters, GetDeletedDocumentsCallback callback);

    /**
     * Retrieve subsequent pages of deleted documents in the user's library.
     *
     * @param next reference to next page returned by a previous onDeletedDocumentsReceived() callback.
     */
    RequestHandle getDeletedDocuments(Page next, GetDeletedDocumentsCallback callback);

    /**
     * Add a new document to the user's library.
     *
     * @param document the document object to be added.
     */
    void postDocument(Document document, PostDocumentCallback callback);

    /**
     * Modify an existing document in the user's library.
     *
     * @param documentId the id of the document to be modified.
     * @param date sets an optional "if unmodified since" condition on the request. Ignored if null.
     * @param document a document object containing the fields to be updated.
     *                 Missing fields are left unchanged (not cleared).
     */
    RequestHandle patchDocument(String documentId, Date date, Document document, PatchDocumentCallback callback);

    /**
     * Move an existing document into the user's trash collection.
     *
     * @param documentId id of the document to be trashed.
     */
    void trashDocument(String documentId, TrashDocumentCallback callback);

    /**
     * Delete a document.
     *
     * @param documentId id of the document to be deleted.
     */
    void deleteDocument(String documentId, DeleteDocumentCallback callback);

    /**
     * Return a list of valid document types.
     */
    RequestHandle getDocumentTypes(GetDocumentTypesCallback callback);

    /* FILES */

    /**
     * Return metadata for a user's files, subject to specified query parameters.
     */
    RequestHandle getFiles(FileRequestParameters parameters, GetFilesCallback callback);

    /**
     * Return metadata for all files associated with all of the user's documents.
     */
    RequestHandle getFiles(GetFilesCallback callback);

    /**
     * Return the next page of file metadata entries.
     *
     * @param next returned from previous getFiles() call.
     */
    RequestHandle getFiles(Page next, GetFilesCallback callback);

    /**
     * Download the content of a file.
     *
     * @param fileId the id of the file to download.
     * @param targetFile the file name to store the file as.
     */
    void getFile(String fileId, java.io.File targetFile, GetFileCallback callback);

    /**
     * Cancel an in-progress file download.
     *
     * @param fileId the id of the file being downloaded.
     */
    void cancelDownload(String fileId);

    /**
     * Upload a new file.
     *
     * @param contentType MIME type of the file, e.g "application/pdf".
     * @param documentId the ID of the document the file should be attached to.
     * @param filePath the absolute local filesystem path where the file can be found.
     */
    void postFile(String contentType, String documentId, String filePath, PostFileCallback callback);

    /**
     * Upload a new file.
     *
     * @param contentType MIME type of the file, e.g "application/pdf".
     * @param documentId the ID of the document the file should be attached to.
     * @param inputStream stream providing the data to be uploaded.
     * @param fileName the name to be used for the file.
     */
    void postFile(String contentType, String documentId, InputStream inputStream, String fileName, PostFileCallback callback);

    /**
     * Delete a file.
     *
     * @param fileId the ID of the file to be deleted.
     */
    void deleteFile(String fileId, DeleteFileCallback callback);

    /* PROFILES */

    /**
     * Return the user's profile information.
     */
    void getMyProfile(GetProfileCallback callback);

    /**
     * Return profile information for another user.
     *
     * @param  profileId ID of the profile to be fetched.
     */
    void getProfile(String profileId, GetProfileCallback callback);

    /* FOLDERS */

    /**
     * Return metadata for all the user's folders.
     */
    RequestHandle getFolders(FolderRequestParameters parameters, GetFoldersCallback callback);

    /**
     * Return metadata for all the user's folders.
     */
    RequestHandle getFolders(GetFoldersCallback callback);

    /**
     * Returns the next page of folder metadata entries.
     *
     * @param next returned from a previous getFolders() call.
     */
    RequestHandle getFolders(Page next, GetFoldersCallback callback);

    /**
     * Returns metadata for a single folder, specified by ID.
     *
     * @param folderId ID of the folder to retrieve metadata for.
     */
    void getFolder(String folderId, GetFolderCallback callback);

    /**
     * Create a new folder.
     *
     * @param folder metadata for the folder to create.
     */
    void postFolder(Folder folder, PostFolderCallback callback);

    /**
     * Update a folder's metadata.
     * <p>
     * This can be used to rename the folder, and/or to move it to a new parent.
     *
     * @param folderId the id of the folder to modify.
     * @param folder metadata object that provides the new name and parentId.
     */
    RequestHandle patchFolder(String folderId, Folder folder, PatchFolderCallback callback);

    /**
     * Return a list of IDs of the documents stored in a particular folder.
     *
     * @param folderId ID of the folder to inspect.
     */
    void getFolderDocumentIds(FolderRequestParameters parameters, String folderId, GetFolderDocumentIdsCallback callback);

    /**
     * Returns the next page of document IDs stored in a particular folder.
     *
     * @param next returned by a previous call to getFolderDocumentIds().
     * @param folderId provides an ID to return in the callback (the value is not actually
     *                 checked by this call).
     */
    void getFolderDocumentIds(Page next, String folderId, GetFolderDocumentIdsCallback callback);

    /**
     * Add a document to a folder.
     *
     * @param folderId the ID the folder.
     * @param documentId the ID of the document to add to the folder.
     */
    void postDocumentToFolder(String folderId, String documentId, PostDocumentToFolderCallback callback);

    /**
     * Delete a folder.
     * <p>
     * This does not delete the documents inside the folder.
     *
     * @param folderId the ID of the folder to delete.
     */
    void deleteFolder(String folderId, DeleteFolderCallback callback);

    /**
     * Remove a document from a folder.
     * <p>
     * This does not delete the documents itself.
     *
     * @param folderId the ID of the folder.
     * @param documentId the ID of the document to remove.
     */
    void deleteDocumentFromFolder(String folderId, String documentId, DeleteFolderDocumentCallback callback);

    /* UTILITIES */

    /**
     * Download an image.
     */
    void getImage(String url, GetImageCallback callback);

    /* GROUPS */

    /**
     * Return metadata for all the user's groups.
     */
    RequestHandle getGroups(GroupRequestParameters parameters, GetGroupsCallback callback);

    /**
     * Returns the next page of group metadata entries.
     *
     * @param next returned from a previous getGroups() call.
     */
    RequestHandle getGroups(Page next, GetGroupsCallback callback);

    /**
     * Returns metadata for a single group, specified by ID.
     *
     * @param groupId ID of the group to retrieve metadata for.
     */
    void getGroup(String groupId, GetGroupCallback callback);

    /**
     * Return a list of members user roles of a particular group.
     *
     * @param groupId ID of the group to inspect.
     */
    void getGroupMembers(GroupRequestParameters parameters, String groupId, GetGroupMembersCallback callback);

    /**
     * Returns the next page of members user roles of a group.
     *
     * @param next returned by a previous call to getGroupMembers().
     * @param groupId provides an ID to return in the callback (the value is not actually
     *                 checked by this call).
     */
    void getGroupMembers(Page next, String groupId, GetGroupMembersCallback callback);

    /* TRASH */

    /**
     * Retrieve a list of documents in the user's trash.
     */
    RequestHandle getTrashedDocuments(DocumentRequestParameters parameters, GetDocumentsCallback callback);

    /**
     * Retrieve a list of documents in the user's trash.
     */
    RequestHandle getTrashedDocuments(GetDocumentsCallback callback);

    /**
     * Retrieve subsequent pages of documents from the user's trash.
     *
     * @param next reference to next page returned by a previous onDocumentsReceived() callback from getTrashedDocuments().
     */
    RequestHandle getTrashedDocuments(Page next, GetDocumentsCallback callback);

    /**
     * Move a document from trash into the user's library.
     *
     * @param documentId id of the document to restore.
     */
    void restoreDocument(String documentId, RestoreDocumentCallback callback);

    /* CONTROL */

    /**
     * Specify the executor used to run background tasks.
     */
    MendeleySdk setExecutor(Executor executor);
}
