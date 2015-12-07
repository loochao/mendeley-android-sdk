package com.mendeley.sdk;

import android.net.Uri;

import com.mendeley.sdk.model.Annotation;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.model.Folder;
import com.mendeley.sdk.model.Group;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.model.ReadPosition;
import com.mendeley.sdk.model.UserRole;
import com.mendeley.sdk.request.Request;
import com.mendeley.sdk.request.endpoint.AnnotationsEndpoint;
import com.mendeley.sdk.request.endpoint.CatalogEndpoint;
import com.mendeley.sdk.request.endpoint.DocumentEndpoint;
import com.mendeley.sdk.request.endpoint.FilesEndpoint;
import com.mendeley.sdk.request.endpoint.FoldersEndpoint;
import com.mendeley.sdk.request.endpoint.GroupsEndpoint;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating typical {@link Request}s to interact against the Mendeley API.
 *
 * <p/>
 *
 * While apps using the Mendeley SDK may obtain their {@link Request}s using this factory, it is
 * still possible to instantiate them directly using their public constructor.
 */
public interface RequestsFactory {

    /**
     * Obtains a {@link Request} to retrieve the list of valid document types. This is, the
     * possible values for {@link Document#type}
     *
     * @return the request
     */
    Request<Map<String, String>> newGetDocumentTypesRequest();

    /**
     * Obtains a {@link Request} to retrieve the list of valid document identifies. This is, the
     * possible values for {@link Document#identifiers}
     *
     * @return the request
     */
    Request<Map<String, String>> newGetDocumentIdentifierTypesRequest();

    /**
     * Obtains a {@link Request} to retrieve the list of {@link Document}s.
     *
     * @param parameters used  to configure the query. Can be null.
     * @return the requests
     */
    Request<List<Document>> newGetDocumentsRequest(DocumentEndpoint.DocumentRequestParameters parameters);

    /**
     * Obtains a {@link Request} to retrieve the list of {@link Document}s.
     *
     * @param url the URL of the request.
     *            May be the {@link com.mendeley.sdk.request.Request.Response#next} field of a previous request.
     * @return the request
     */
    Request<List<Document>> newGetDocumentsRequest(Uri url);

    /**
     * Obtains a {@link Request} to retrieve one single {@link Document} by its id.
     *
     * @param documentId documentId the id of the document to get
     * @param view used to configure which fields the server will return
     * @return the request
     */
    Request<Document> newGetDocumentRequest(String documentId, DocumentEndpoint.DocumentRequestParameters.View view);

    /**
     * Obtains a {@link Request} to create a new  {@link Document} in the user's library.
     *
     * @param document the document to create
     * @return the request
     */
    Request<Document> newPostDocumentRequest(Document document);

    /**
     * Obtains a {@link Request} to update an existing document in the user's library.
     *
     * @param documentId the id of the document to be updated.
     * @param date sets an optional "if unmodified since" condition on the request. Ignored if null.
     * @param document a document object containing the fields to be updated.
     *                 Missing fields are left unchanged (not cleared).
     * @return the request
     */
    Request<Document> newPatchDocumentRequest(String documentId, Date date, Document document);

    /**
     * Obtains a {@link Request} to move an existing document to the trash of the user's library.
     *
     * @param documentId the id of the document to trash
     * @return the request
     */
    Request<Void> newTrashDocumentRequest(String documentId);

    /**
     * Obtains a {@link Request} to permanently delete an existing document which is NOT in the trash.
     *
     * @param documentId the id of the document to delete
     * @return the request
     */
    Request<Void> newDeleteDocumentRequest(String documentId);

    /**
     * Obtains a {@link Request} to permanently delete an existing document which is in the trash.
     *
     * @param documentId the id of the document to delete
     * @return the request
     */
    Request<Void> newDeleteTrashedDocumentRequest(String documentId);

    /**
     * Obtains a {@link Request} to get a list of the @{link File}s in the user's library.
     *
     * @param parameters used  to configure the query. Can be null.
     * @return the request
     */
    Request<List<File>> newGetFilesRequest(FilesEndpoint.FileRequestParameters parameters);

    /**
     *
     * Obtains a {@link Request} to get a list of the @{link File}s in the user's library.
     *
     * @param uri the URL of the request.
     *            May be the {@link com.mendeley.sdk.request.Request.Response#next} field of a previous request.
     * @return the request
     */
    Request<List<File>> newGetFilesRequest(Uri uri);

    /**
     * Obtains a {@link Request} to download the data related to a {@link File}. Normally,
     * this is the pdf file that belongs to the {@link File}.
     *
     * @param fileId the id of the file
     * @param targetFile the {@link File} in the file system where the data will be saved to
     * @return the request
     */
    FilesEndpoint.GetFileBinaryRequest newGetFileBinaryRequest(String fileId, java.io.File targetFile);

    /**
     *
     * Obtains a {@link Request} to create a {@link File} in the server linked to the data posted
     * by the request.
     *
     * @param contentType the content type of the data to be posted
     * @param documentId the id of the {@link Document} the created file will belong to
     * @param inputStream used to read the data posted to the server.
     * @param fileName the name of the file.
     * @return the request
     */
    Request<File> newPostFileWithBinaryRequest(String contentType, String documentId, InputStream inputStream, String fileName);

    /**
     * Obtains a {@link Request} to delete the {@link File} with the passed id.
     *
     * @param fileId the id of the file to delete
     * @return the request
     */
    Request<Void> newDeleteFileRequest(String fileId);

    /**
     * Obtains a {@link Request} to get the list {@link Folder}s in the user's library.
     *
     * @param parameters used to configure the query. Can be null.
     * @return the request
     */
    Request<List<Folder>> newGetFoldersRequest(FoldersEndpoint.FolderRequestParameters parameters);

    /**
     * Obtains a {@link Request} to get the list {@link Folder}s in the user's library.
     *
     * @param uri the URL of the request.
     *            May be the {@link com.mendeley.sdk.request.Request.Response#next} field of a previous request.
     * @return the request
     */
    Request<List<Folder>> newGetFoldersRequest(Uri uri);

    /**
     * Returns metadata for a single folder, specified by ID.
     *
     * @param folderId ID of the folder to retrieve metadata for.
     */
    /**
     * Obtains a {@link Request} to get one existing {@link Folder} with the passed id.
     *
     * @param folderId the id of the folder
     * @return the request
     */
    Request<Folder> newGetFolderRequest(String folderId);

    /**
     * Obtains a {@link Request} to create a new  {@link Folder} in the user's library.
     *
     * @param folder the foler to create
     * @return the request
     */
    Request<Folder> newPostFolderRequest(Folder folder);

    /**
     * Obtains a {@link Request} to update an existing folder in the user's library.
     *
     * @param folderId the id of the folder to be updated.
     * @param folder a folder object containing the fields to be updated.
     *                 Missing fields are left unchanged (not cleared).
     * @return the request
     */
    Request<Folder> newPatchFolderRequest(String folderId, Folder folder);

    /**
     * Obtains a {@link Request} to permanently delete an existing {@link Folder}
     *
     * @param folderId the id of the document to delete
     * @return the request
     */
    Request<Void> newDeleteFolderRequest(String folderId);

    /**
     * Obtains a {@link Request} to retrieve the content of one specific {@link Folder} in the form
     * of the ids of the {@link Document}s in it
     *
     * @param parameters used to configure the query. Can be null.
     * @param folderId the id of the folder to query
     * @return the request
     */
    Request<List<String>> newGetFolderDocumentsRequest(FoldersEndpoint.FolderRequestParameters parameters, String folderId);

    /**
     * Obtains a {@link Request} to retrieve the content of one specific {@link Folder} in the form
     * of the ids of the {@link Document}s in it
     *
     * @param uri the URL of the request.
     *            May be the {@link com.mendeley.sdk.request.Request.Response#next} field of a previous request.
     * @return the request
     */
    Request<List<String>> newGetFolderDocumentsRequest(Uri uri);

    /**
     * Obtains a {@link Request} to insert one document into one folder.
     *
     * @param folderId the id the folder.
     * @param documentId the id of the document to be added to the folder.
     * @return the request
     */
    Request<Void> newPostDocumentToFolderRequest(String folderId, String documentId);

    /**
     * Obtains a {@link Request} to delete one document from one folder.
     * This just deletes the document from the folder, but the document won't be deleted and will
     * continue to exist in other folders it might be.
     *
     * @param folderId the id of the folder
     * @param documentId the id of the document
     * @return the request
     */
    Request<Void> newDeleteDocumentFromFolderRequest(String folderId, String documentId);

    /**
     * Return metadata for all the user's groups.
     */
    Request<List<Group>> newGetGroupsRequest(GroupsEndpoint.GroupRequestParameters parameters);

    /**
     * Returns the next page of group metadata entries.
     *
     * @param uri returned from a previous getGroups() call.
     */
    Request<List<Group>> newGetGroupsRequest(Uri uri);

    /**
     * Returns metadata for a single group, specified by ID.
     *
     * @param groupId ID of the group to retrieve metadata for.
     */
    Request<Group> newGetGroupRequest(String groupId);

    /**
     * Return a list of members user roles of a particular group.
     *
     * @param groupId ID of the group to inspect.
     */
    Request<List<UserRole>> newGetGroupMembersRequest(GroupsEndpoint.GroupRequestParameters parameters, String groupId);

    /**
     * Return a list of members user roles of a particular group.
     *
     * @param url returned from a previous getGroupMembers() call.
     */
    Request<List<UserRole>> newGetGroupMembersRequest(Uri url);

    /* TRASH */

    /**
     * Retrieve a list of documents in the user's trash.
     */
    Request<List<Document>> newGetTrashedDocumentsRequest(DocumentEndpoint.DocumentRequestParameters parameters);

    /**
     * Retrieve a list of documents in the user's trash.
     */
    Request<List<Document>> newGetTrashedDocumentsRequest();

    /**
     * Retrieve subsequent pages of documents from the user's trash.
     *
     * @param uri reference to next page returned by a previous DocumentList from getTrashedDocuments().
     */
    Request<List<Document>> newGetTrashedDocumentsRequest(Uri uri);

    /**
     * Move a document from trash into the user's library.
     *
     * @param documentId id of the document to restore.
     */
    Request<Void> newRestoreTrashedDocumentRequest(String documentId);

    /* PROFILES */

    /**
     * Return the user's profile information.
     */
    Request<Profile> newGetMyProfileRequest();

    /**
     * Return profile information for another user.
     *
     * @param  profileId ID of the profile to be fetched.
     */
    Request<Profile> newGetProfileRequest(String profileId);

    /* CATALOG */

    /**
     * Retrieve a list of catalog documents
     */
    Request<List<Document>> newGetCatalogDocumentsRequest(CatalogEndpoint.CatalogDocumentRequestParameters parameters);

    /**
     * Retrieve a single catalog document, specified by ID.
     *
     * @param catalogId the catalog document id to get.
     * @param view extended catalog document view. If null, only core fields are returned.
     */
    Request<Document> newGetCatalogDocumentRequest(String catalogId, DocumentEndpoint.DocumentRequestParameters.View view);



    /* ANNOTATIONS */

    Request<List<Annotation>> newGetAnnotationsRequest();

    Request<List<Annotation>> newGetAnnotationsRequest(AnnotationsEndpoint.AnnotationRequestParameters parameters);

    Request<List<Annotation>> newGetAnnotationsRequest(Uri url);

    Request<Annotation> newGetAnnotationRequest(String annotationId);

    Request<Annotation> newPostAnnotationRequest(Annotation annotation);

    Request<Annotation> newPatchAnnotationRequest(String annotationId, Annotation annotation);

    Request<Void> newDeleteAnnotationRequest(String annotationId);

    /* RECENTLY READ POSITIONS */

    /**
     * Queries the last {@link ReadPosition}s for some files
     *
     * @param groupId id of the group of the documents of the files of which read positions we want. If null it will return only the ones in the user's library.
     * @param fileId id of the file of which read position we want. If null it will return positions for all the files in the specified group.
     * @param limit
     * @return a list of {@link ReadPosition} with the last {@link ReadPosition} for some files
     */
    Request<List<ReadPosition>> newGetRecentlyReadRequest(String groupId, String fileId, int limit);

    /**
     * Posts the passed {@link ReadPosition} to the Mendeley Web API
     *
     * @param readPosition
     * @return the posted {@link ReadPosition} as it's now in the server
     */
    Request<ReadPosition> newPostRecentlyReadRequest(ReadPosition readPosition);




}
