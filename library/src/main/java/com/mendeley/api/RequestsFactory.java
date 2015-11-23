package com.mendeley.api;

import android.net.Uri;

import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.Folder;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.Profile;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.request.GetFileNetworkRequest;
import com.mendeley.api.request.Request;
import com.mendeley.api.request.endpoint.AnnotationsEndpoint;
import com.mendeley.api.request.endpoint.CatalogEndpoint;
import com.mendeley.api.request.endpoint.DocumentEndpoint;
import com.mendeley.api.request.endpoint.FilesEndpoint;
import com.mendeley.api.request.endpoint.FoldersEndpoint;
import com.mendeley.api.request.endpoint.GroupsEndpoint;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface RequestsFactory {

    /**
     * Retrieve a list of documents in the user's library.
     */
    Request<List<Document>> getDocuments();

    /**
     * Retrieve a list of documents in the user's library.
     */
    Request<List<Document>> getDocuments(DocumentEndpoint.DocumentRequestParameters parameters);

    /**
     * Retrieve subsequent pages of documents in the user's library.
     *
     * @param url reference to next page returned in a previous DocumentList.
     */
    Request<List<Document>> getDocuments(Uri url);

    /**
     * Retrieve a single document, specified by ID.
     *
     * @param documentId the document id to get.
     * @param view extended document view. If null, only core fields are returned.
     */
    Request<Document> getDocument(String documentId, DocumentEndpoint.DocumentRequestParameters.View view);

    /**
     * Add a new document to the user's library.
     *
     * @param document the document object to be added.
     */
    Request<Document> postDocument(Document document);

    /**
     * Modify an existing document in the user's library.
     *
     * @param documentId the id of the document to be modified.
     * @param date sets an optional "if unmodified since" condition on the request. Ignored if null.
     * @param document a document object containing the fields to be updated.
     *                 Missing fields are left unchanged (not cleared).
     */
    Request<Document> patchDocument(String documentId, Date date, Document document);

    /**
     * Move an existing document into the user's trash collection.
     *
     * @param documentId id of the document to be trashed.
     */
    Request<Void> trashDocument(String documentId);

    /**
     * Delete a document which is NOT trashed.
     *
     * @param documentId id of the document to be deleted.
     */
    Request<Void> deleteDocument(String documentId);

    /**
     * Delete a document which is alreare trashed.
     *
     * @param documentId id of the document to be deleted.
     */
    Request<Void> deleteTrashedDocument(String documentId);

    /**
     * Return a list of valid document types.
     */
    Request<Map<String, String>> getDocumentTypes();

    /**
     * Return a list of valid identifiers types.
     */
    Request<Map<String, String>> getDocumentIdentifierTypes();

    /* FILES */

    /**
     * Return metadata for a user's files, subject to specified query parameters.
     */
    Request<List<File>> getFiles(FilesEndpoint.FileRequestParameters parameters);

    /**
     * Return metadata for all files associated with all of the user's documents.
     */
    Request<List<File>> getFiles();

    /**
     * Return the next page of file metadata entries.
     *
     * @param uri returned from previous getFiles() call.
     */
    Request<List<File>> getFiles(Uri uri);


    /**
     * Download the content of a file.
     *
     * @param fileId the id of the file to download.
     * @param targetFile the file name to store the file as.
     *
     * @return bytes downloaded
     */
    GetFileNetworkRequest getFileBinary(String fileId, java.io.File targetFile);


    /**
     * Upload the content of a file
     *
     * @param contentType of the file
     * @param documentId the id of the document this file belongs to
     * @param inputStream the file input stream
     * @param fileName the file name
     * @return the file metadata
     * @
     */
    Request<File> postFileBinary(String contentType, String documentId, InputStream inputStream, String fileName);

    /**
     * Delete file with the given id
     * @param fileId
     */
    Request<Void> deleteFile(String fileId);

    /* FOLDERS */

    /**
     * Return metadata for all the user's folders.
     */
    Request<List<Folder>> getFolders(FoldersEndpoint.FolderRequestParameters parameters);

    /**
     * Return metadata for all the user's folders.
     */
    Request<List<Folder>> getFolders();

    /**
     * Returns the next page of folder metadata entries.
     *
     * @param uri returned from a previous getFolders() call.
     */
    Request<List<Folder>> getFolders(Uri uri);

    /**
     * Returns metadata for a single folder, specified by ID.
     *
     * @param folderId ID of the folder to retrieve metadata for.
     */
    Request<Folder> getFolder(String folderId);


    /**
     * Create a new folder.
     *
     * @param folder metadata for the folder to create.
     */
    Request<Folder> postFolder(Folder folder);

    /**
     * Update a folder's metadata.
     * <p>
     * This can be used to rename the folder, and/or to move it to a new parent.
     *
     * @param folderId the id of the folder to modify.
     * @param folder metadata object that provides the new name and parentId.
     */
    Request<Folder> patchFolder(String folderId, Folder folder);

    /**
     * Return a list of IDs of the documents stored in a particular folder.
     *
     * @param folderId ID of the folder to inspect.
     */
    Request<List<String>> getFolderDocumentIds(FoldersEndpoint.FolderRequestParameters parameters, String folderId);

    /**
     * Returns the next page of document IDs stored in a particular folder.
     * @param uri returned by a previous call to getFolderDocumentIds().
     *
     */
    Request<List<String>> getFolderDocumentIds(Uri uri);

    /**
     * Add a document to a folder.
     *
     * @param folderId the ID the folder.
     * @param documentId the ID of the document to add to the folder.
     */
    Request<Void> postDocumentToFolder(String folderId, String documentId);

    /**
     * Delete a folder.
     * <p>
     * This does not delete the documents inside the folder.
     *
     * @param folderId the ID of the folder to delete.
     */
    Request<Void> deleteFolder(String folderId);

    /**
     * Remove a document from a folder.
     * <p>
     * This does not delete the documents itself.
     *
     * @param folderId the ID of the folder.
     * @param documentId the ID of the document to remove.
     */
    Request<Void> deleteDocumentFromFolder(String folderId, String documentId);

    /* GROUPS */

    /**
     * Return metadata for all the user's groups.
     */
    Request<List<Group>> getGroups(GroupsEndpoint.GroupRequestParameters parameters);

    /**
     * Returns the next page of group metadata entries.
     *
     * @param uri returned from a previous getGroups() call.
     */
    Request<List<Group>> getGroups(Uri uri);

    /**
     * Returns metadata for a single group, specified by ID.
     *
     * @param groupId ID of the group to retrieve metadata for.
     */
    Request<Group> getGroup(String groupId);

    /**
     * Return a list of members user roles of a particular group.
     *
     * @param groupId ID of the group to inspect.
     */
    Request<List<UserRole>> getGroupMembers(GroupsEndpoint.GroupRequestParameters parameters, String groupId);

    /**
     * Return a list of members user roles of a particular group.
     *
     * @param url returned from a previous getGroupMembers() call.
     */
    Request<List<UserRole>> getGroupMembers(Uri url);

    /* TRASH */

    /**
     * Retrieve a list of documents in the user's trash.
     */
    Request<List<Document>> getTrashedDocuments(DocumentEndpoint.DocumentRequestParameters parameters);

    /**
     * Retrieve a list of documents in the user's trash.
     */
    Request<List<Document>> getTrashedDocuments();

    /**
     * Retrieve subsequent pages of documents from the user's trash.
     *
     * @param uri reference to next page returned by a previous DocumentList from getTrashedDocuments().
     */
    Request<List<Document>> getTrashedDocuments(Uri uri);

    /**
     * Move a document from trash into the user's library.
     *
     * @param documentId id of the document to restore.
     */
    Request<Void> restoreTrashedDocument(String documentId);

    /* PROFILES */

    /**
     * Return the user's profile information.
     */
    Request<Profile> getMyProfile();

    /**
     * Return profile information for another user.
     *
     * @param  profileId ID of the profile to be fetched.
     */
    Request<Profile> getProfile(String profileId);

    /* CATALOG */

    /**
     * Retrieve a list of catalog documents
     */
    Request<List<Document>> getCatalogDocuments(CatalogEndpoint.CatalogDocumentRequestParameters parameters);

    /**
     * Retrieve a single catalog document, specified by ID.
     *
     * @param catalogId the catalog document id to get.
     * @param view extended catalog document view. If null, only core fields are returned.
     */
    Request<Document> getCatalogDocument(String catalogId, DocumentEndpoint.DocumentRequestParameters.View view);



    /* ANNOTATIONS */

    Request<List<Annotation>> getAnnotations();

    Request<List<Annotation>> getAnnotations(AnnotationsEndpoint.AnnotationRequestParameters parameters);

    Request<List<Annotation>> getAnnotations(Uri url);

    Request<Annotation> getAnnotation(String annotationId);

    Request<Annotation> postAnnotation(Annotation annotation);

    Request<Annotation> patchAnnotation(String annotationId, Annotation annotation);

    Request<Void> deleteAnnotation(String annotationId);

    /* RECENTLY READ POSITIONS */

    /**
     * Queries the last {@link ReadPosition}s for some files
     *
     * @param groupId id of the group of the documents of the files of which read positions we want. If null it will return only the ones in the user's library.
     * @param fileId id of the file of which read position we want. If null it will return positions for all the files in the specified group.
     * @param limit
     * @return a list of {@link ReadPosition} with the last {@link ReadPosition} for some files
     */
    Request<List<ReadPosition>> getRecentlyRead(String groupId, String fileId, int limit);

    /**
     * Posts the passed {@link ReadPosition} to the Mendeley Web API
     *
     * @param readPosition
     * @return the posted {@link ReadPosition} as it's now in the server
     */
    Request<ReadPosition> postRecentlyRead(ReadPosition readPosition);

    /**
     *
     * This method is intended for internal development and should not be used by third party
     * users of the SDK.
     *
     * TODO: remove this method from the public interface of the SDK.
     *
     * Retrieves a list of features from the web API with experimental features enabled.
     * @return a list of features from the web API with experimental features enabled.
     */
    Request<List<String>> getApplicationFeatures();

}
