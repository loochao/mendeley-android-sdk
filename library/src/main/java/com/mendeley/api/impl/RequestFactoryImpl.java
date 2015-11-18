package com.mendeley.api.impl;

import android.net.Uri;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.Folder;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.Profile;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.request.DeleteAuthorizedRequest;
import com.mendeley.api.request.GetFileNetworkRequest;
import com.mendeley.api.request.PostFileAuthorizedRequest;
import com.mendeley.api.request.Request;
import com.mendeley.api.request.PostAuthorizedRequest;
import com.mendeley.api.request.params.AnnotationRequestParameters;
import com.mendeley.api.request.params.CatalogDocumentRequestParameters;
import com.mendeley.api.request.params.DocumentRequestParameters;
import com.mendeley.api.request.params.FileRequestParameters;
import com.mendeley.api.request.params.FolderRequestParameters;
import com.mendeley.api.request.params.GroupRequestParameters;
import com.mendeley.api.request.params.Page;
import com.mendeley.api.request.params.View;
import com.mendeley.api.request.endpoint.AnnotationsEndpoint;
import com.mendeley.api.request.endpoint.ApplicationFeaturesEndpoint;
import com.mendeley.api.request.endpoint.CatalogEndpoint;
import com.mendeley.api.request.endpoint.DocumentEndpoint;
import com.mendeley.api.request.endpoint.FilesEndpoint;
import com.mendeley.api.request.endpoint.FolderEndpoint;
import com.mendeley.api.request.endpoint.GroupsEndpoint;
import com.mendeley.api.request.endpoint.ProfilesEndpoint;
import com.mendeley.api.request.endpoint.RecentlyReadEndpoint;
import com.mendeley.api.request.endpoint.TrashEndpoint;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.endpoint.AnnotationsEndpoint.deleteAnnotationUrl;
import static com.mendeley.api.request.endpoint.AnnotationsEndpoint.getAnnotationUrl;
import static com.mendeley.api.request.endpoint.AnnotationsEndpoint.getAnnotationsUrl;
import static com.mendeley.api.request.endpoint.DocumentEndpoint.getGetDocumentUrl;
import static com.mendeley.api.request.endpoint.DocumentEndpoint.getGetDocumentsUrl;
import static com.mendeley.api.request.endpoint.DocumentEndpoint.getTrashDocumentUrl;
import static com.mendeley.api.request.endpoint.FolderEndpoint.getDeleteFolderUrl;
import static com.mendeley.api.request.endpoint.FolderEndpoint.getGetFolderDocumentIdsUrl;
import static com.mendeley.api.request.endpoint.FolderEndpoint.getGetFolderUrl;
import static com.mendeley.api.request.endpoint.FolderEndpoint.getGetFoldersUrl;
import static com.mendeley.api.request.endpoint.FolderEndpoint.getPostDocumentToFolderUrl;
import static com.mendeley.api.request.endpoint.GroupsEndpoint.getGetGroupMembersUrl;
import static com.mendeley.api.request.endpoint.GroupsEndpoint.getGetGroupsUrl;

/**
 * Implementation of the blocking API calls.
 */
public class RequestFactoryImpl implements RequestsFactory {

    public static final String TAG = RequestFactoryImpl.class.getSimpleName();

    private final ClientCredentials clientCredentials;
    private final AuthTokenManager authTokenManager;

    public RequestFactoryImpl(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        this.authTokenManager = authTokenManager;
        this.clientCredentials = clientCredentials;
    }

    @Override
    public Request<List<Document>> getDocuments() {
        return getDocuments((DocumentRequestParameters) null);
    }

    @Override
    public Request<List<Document>> getDocuments(DocumentRequestParameters parameters) {
        Uri url = getGetDocumentsUrl(parameters, null);
        return new DocumentEndpoint.GetDocumentsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<Document>> getDocuments(Page next) {
        return new DocumentEndpoint.GetDocumentsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Document> getDocument(String documentId, View view) {
        Uri url = getGetDocumentUrl(documentId, view);
        return new DocumentEndpoint.GetDocumentRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getDeletedDocuments(String deletedSince, DocumentRequestParameters parameters)  {
        Uri url = getGetDocumentsUrl(parameters, deletedSince);
        return new DocumentEndpoint.GetDeletedDocumentsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getDeletedDocuments(Page next) {
        return new DocumentEndpoint.GetDeletedDocumentsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Document> postDocument(Document document) {
        return new DocumentEndpoint.PostDocumentRequest(document, authTokenManager, clientCredentials);
    }


    @Override
    public Request<Document> patchDocument(String documentId, Date date, Document document) {
        return new DocumentEndpoint.PatchDocumentAuthorizedRequest(documentId, document, date, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> trashDocument(String documentId) {
        return new PostAuthorizedRequest<Void>(getTrashDocumentUrl(documentId), authTokenManager, clientCredentials) {
            @Override
            protected Void manageResponse(InputStream is) throws Exception {
                return null;
            }

            @Override
            protected void writePostBody(OutputStream os) throws Exception {

            }
        };
    }

    @Override
    public Request<Void> deleteDocument(String documentId) {
        return new DeleteAuthorizedRequest(DocumentEndpoint.getDeleteDocumentUrl(documentId), authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> deleteTrashedDocument(String documentId) {
        return new DeleteAuthorizedRequest(TrashEndpoint.getDeleteUrl(documentId), authTokenManager, clientCredentials);
    }

    @Override
    public Request<Map<String, String>> getDocumentTypes()  {
        return new DocumentEndpoint.GetDocumentTypesRequest(Uri.parse(DocumentEndpoint.DOCUMENT_TYPES_BASE_URL), authTokenManager, clientCredentials);
    }

    @Override
    public Request<Map<String, String>> getIdentifierTypes() {
        return new DocumentEndpoint.GetDocumentTypesRequest(Uri.parse(DocumentEndpoint.IDENTIFIER_TYPES_BASE_URL), authTokenManager, clientCredentials);
    }

    /* ANNOTATIONS BLOCKING */

    @Override
    public Request<List<Annotation>> getAnnotations() {
        return getAnnotations((AnnotationRequestParameters) null);
    }

    @Override
    public Request<List<Annotation>> getAnnotations(AnnotationRequestParameters parameters) {
        try {
            Uri url = getAnnotationsUrl(parameters);
            return new AnnotationsEndpoint.GetAnnotationsRequest(url, authTokenManager, clientCredentials);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode the ANNOTATIONS_BASE_URL for getting annotations", e);
        }
    }

    @Override
    public Request<List<Annotation>> getAnnotations(Page next) {
        return new AnnotationsEndpoint.GetAnnotationsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Annotation> getAnnotation(String annotationId) {
        Uri url = getAnnotationUrl(annotationId);
        return new AnnotationsEndpoint.GetAnnotationRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Annotation>  postAnnotation(Annotation annotation) {
        return new AnnotationsEndpoint.PostAnnotationRequest(annotation, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Annotation>  patchAnnotation(String annotationId, Annotation annotation) {
       return new AnnotationsEndpoint.PatchAnnotationRequest(annotationId, annotation, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> deleteAnnotation(String annotationId) {
        return new DeleteAuthorizedRequest(deleteAnnotationUrl(annotationId), authTokenManager, clientCredentials);
    }

    /* FILES BLOCKING */

    @Override
    public Request<List<File>> getFiles() {
        return getFiles((FileRequestParameters) null);
    }

    @Override
    public Request<List<File>> getFiles(FileRequestParameters parameters) {
        try {
            Uri url = FilesEndpoint.getGetFilesUrl(parameters);
            return new FilesEndpoint.GetFilesRequest(url, authTokenManager, clientCredentials);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Request<List<File>> getFiles(Page next) {
        return new FilesEndpoint.GetFilesRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public GetFileNetworkRequest getFileBinary(String fileId, java.io.File targetFile) {
        return new GetFileNetworkRequest(fileId, targetFile, authTokenManager, clientCredentials);
    }

    @Override
    public Request<File> postFileBinary(String contentType, String documentId, InputStream inputStream, String fileName) {
        return new PostFileAuthorizedRequest(contentType, documentId, fileName, inputStream, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> deleteFile(String fileId) {
        return new DeleteAuthorizedRequest(FilesEndpoint.getDeleteFileUrl(fileId), authTokenManager, clientCredentials);
    }

    /* FOLDERS BLOCKING */

    @Override
    public Request<List<Folder>> getFolders() {
        return getFolders((FolderRequestParameters) null);
    }

    @Override
    public Request<List<Folder>> getFolders(FolderRequestParameters parameters) {
        Uri url = getGetFoldersUrl(parameters);
        return new FolderEndpoint.GetFoldersRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<Folder>> getFolders(Page next) {
        return new FolderEndpoint.GetFoldersRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Folder> getFolder(String folderId) {
        Uri url = getGetFolderUrl(folderId);
        return new FolderEndpoint.GetFolderRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Folder> postFolder(Folder folder) {
        Uri url = Uri.parse(FolderEndpoint.FOLDERS_BASE_URL);
        return new FolderEndpoint.PostFolderRequest(url, folder, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Folder> patchFolder(String folderId, Folder folder) {
        Uri url = FolderEndpoint.getPatchFolderUrl(folderId);
        return new FolderEndpoint.PatchFolderAuthorizedRequest(url, folder, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getFolderDocumentIds(FolderRequestParameters parameters, String folderId) {
        Uri url = getGetFoldersUrl(parameters, getGetFolderDocumentIdsUrl(folderId));
        return new FolderEndpoint.GetFolderDocumentIdsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getFolderDocumentIds(Page next) {
        return new FolderEndpoint.GetFolderDocumentIdsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> postDocumentToFolder(String folderId, String documentId) {
        Uri url = getPostDocumentToFolderUrl(folderId);
        return new FolderEndpoint.PostDocumentToFolderRequest(url, documentId, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> deleteFolder(String folderId) {
        Uri url = getDeleteFolderUrl(folderId);
        return new DeleteAuthorizedRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> deleteDocumentFromFolder(String folderId, String documentId) {
        Uri url = FolderEndpoint.getDeleteDocumentFromFolderUrl(folderId, documentId);
        return new DeleteAuthorizedRequest(url, authTokenManager, clientCredentials);
    }


    /* PROFILES BLOCKING */

    @Override
    public Request<Profile> getMyProfile() {
        return new ProfilesEndpoint.GetProfileRequest(Uri.parse(ProfilesEndpoint.PROFILES_URL + "me"), authTokenManager, clientCredentials);
    }

    @Override
    public Request<Profile> getProfile(final String profileId) {
        return new ProfilesEndpoint.GetProfileRequest(Uri.parse(ProfilesEndpoint.PROFILES_URL + profileId), authTokenManager, clientCredentials);
    }

    /* GROUPS BLOCKING */

    @Override
    public Request<List<Group>> getGroups(GroupRequestParameters parameters) {
        Uri url = getGetGroupsUrl(parameters);
        return new GroupsEndpoint.GetGroupsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<Group>>  getGroups(Page next) {
        return new GroupsEndpoint.GetGroupsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Group> getGroup(String groupId) {
        Uri url = GroupsEndpoint.getGetGroupUrl(groupId);
        return new GroupsEndpoint.GetGroupRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<UserRole>> getGroupMembers(GroupRequestParameters parameters, String groupId) {
        Uri url = getGetGroupsUrl(parameters, getGetGroupMembersUrl(groupId));
        return new GroupsEndpoint.GetGroupMembersRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<UserRole>> getGroupMembers(Page next){
        return new GroupsEndpoint.GetGroupMembersRequest(next.link, authTokenManager, clientCredentials);

    }

    /* TRASH BLOCKING */

    @Override
    public Request<List<Document>> getTrashedDocuments(){
        return getTrashedDocuments((DocumentRequestParameters) null);
    }

    @Override
    public Request<List<Document>> getTrashedDocuments(DocumentRequestParameters parameters) {
        Uri url = DocumentEndpoint.getTrashDocumentsUrl(parameters, null);
        return new DocumentEndpoint.GetDocumentsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<Document>> getTrashedDocuments(Page next) {
        return new DocumentEndpoint.GetDocumentsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> restoreDocument(String documentId) {
        return new PostAuthorizedRequest<Void>(TrashEndpoint.getRecoverUrl(documentId), authTokenManager, clientCredentials) {
            @Override
            protected Void manageResponse(InputStream is) throws Exception {
                return null;
            }

            @Override
            protected void writePostBody(OutputStream os) throws Exception {

            }
        };
    }

    /* CATALOG BLOCKING */

    @Override
    public Request<List<Document>> getCatalogDocuments(CatalogDocumentRequestParameters parameters) {
        Uri url = CatalogEndpoint.getGetCatalogDocumentsUrl(parameters);
        return new CatalogEndpoint.GetCatalogDocumentsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Document> getCatalogDocument(String catalogId, View view) {
        Uri url = CatalogEndpoint.getGetCatalogDocumentUrl(catalogId, view);
        return new CatalogEndpoint.GetCatalogDocumentRequest(url, authTokenManager, clientCredentials);
    }


    /* RECENTLY READ POSITIONS */

    @Override
    public Request<List<ReadPosition>> getRecentlyRead(String groupId, String fileId, int limit) {
        Uri url = RecentlyReadEndpoint.getGetRecentlyReadUrl(groupId, fileId, limit);
        return new RecentlyReadEndpoint.GetRecentlyReadRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<ReadPosition> postRecentlyRead(ReadPosition readPosition) {
        return new RecentlyReadEndpoint.PostRecentlyReadRequest(readPosition, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getApplicationFeatures() {
        return new ApplicationFeaturesEndpoint.GetApplicationFeaturesProcedure(authTokenManager, clientCredentials);
    }


}
