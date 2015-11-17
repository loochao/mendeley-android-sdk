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
import com.mendeley.api.request.PostNetworkRequest;
import com.mendeley.api.request.Request;
import com.mendeley.api.request.params.AnnotationRequestParameters;
import com.mendeley.api.request.params.CatalogDocumentRequestParameters;
import com.mendeley.api.request.params.DocumentRequestParameters;
import com.mendeley.api.request.params.FileRequestParameters;
import com.mendeley.api.request.params.FolderRequestParameters;
import com.mendeley.api.request.params.GroupRequestParameters;
import com.mendeley.api.request.params.Page;
import com.mendeley.api.request.params.View;
import com.mendeley.api.request.provider.AnnotationsNetworkProvider;
import com.mendeley.api.request.provider.ApplicationFeaturesNetworkProvider;
import com.mendeley.api.request.provider.CatalogDocumentNetworkProvider;
import com.mendeley.api.request.provider.DocumentNetworkProvider;
import com.mendeley.api.request.provider.FileNetworkProvider;
import com.mendeley.api.request.provider.FolderNetworkProvider;
import com.mendeley.api.request.provider.GroupNetworkProvider;
import com.mendeley.api.request.provider.ProfileNetworkProvider;
import com.mendeley.api.request.provider.RecentlyReadNetworkProvider;
import com.mendeley.api.request.provider.TrashNetworkProvider;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.provider.AnnotationsNetworkProvider.deleteAnnotationUrl;
import static com.mendeley.api.request.provider.AnnotationsNetworkProvider.getAnnotationUrl;
import static com.mendeley.api.request.provider.AnnotationsNetworkProvider.getAnnotationsUrl;
import static com.mendeley.api.request.provider.DocumentNetworkProvider.getGetDocumentUrl;
import static com.mendeley.api.request.provider.DocumentNetworkProvider.getGetDocumentsUrl;
import static com.mendeley.api.request.provider.DocumentNetworkProvider.getTrashDocumentUrl;
import static com.mendeley.api.request.provider.FolderNetworkProvider.getDeleteFolderUrl;
import static com.mendeley.api.request.provider.FolderNetworkProvider.getGetFolderDocumentIdsUrl;
import static com.mendeley.api.request.provider.FolderNetworkProvider.getGetFolderUrl;
import static com.mendeley.api.request.provider.FolderNetworkProvider.getGetFoldersUrl;
import static com.mendeley.api.request.provider.FolderNetworkProvider.getPostDocumentToFolderUrl;
import static com.mendeley.api.request.provider.GroupNetworkProvider.getGetGroupMembersUrl;
import static com.mendeley.api.request.provider.GroupNetworkProvider.getGetGroupsUrl;

/**
 * Implementation of the blocking API calls.
 */
public class RequestFactoryImpl implements RequestsFactory {

    public static final String TAG = RequestFactoryImpl.class.getSimpleName();

    private final ClientCredentials clientCredentials;
    private final AuthTokenManager authTokenManager;

    protected FileNetworkProvider fileNetworkProvider;
    protected ProfileNetworkProvider profileNetworkProvider;
    protected FolderNetworkProvider folderNetworkProvider;
    protected GroupNetworkProvider groupNetworkProvider;
    protected TrashNetworkProvider trashNetworkProvider;
    protected AnnotationsNetworkProvider annotationsNetworkProvider;

    public RequestFactoryImpl(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        this.authTokenManager = authTokenManager;
        this.clientCredentials = clientCredentials;
        fileNetworkProvider = new FileNetworkProvider();
        profileNetworkProvider = new ProfileNetworkProvider();
        folderNetworkProvider = new FolderNetworkProvider();
        groupNetworkProvider = new GroupNetworkProvider();
        trashNetworkProvider = new TrashNetworkProvider();
        annotationsNetworkProvider = new AnnotationsNetworkProvider();
    }

    /* DOCUMENTS BLOCKING */

    @Override
    public Request<List<Document>> getDocuments() {
        return getDocuments((DocumentRequestParameters) null);
    }

    @Override
    public Request<List<Document>> getDocuments(DocumentRequestParameters parameters) {
        Uri url = getGetDocumentsUrl(parameters, null);
        return new DocumentNetworkProvider.GetDocumentsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<Document>> getDocuments(Page next) {
        return new DocumentNetworkProvider.GetDocumentsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Document> getDocument(String documentId, View view) {
        Uri url = getGetDocumentUrl(documentId, view);
        return new DocumentNetworkProvider.GetDocumentRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getDeletedDocuments(String deletedSince, DocumentRequestParameters parameters)  {
        Uri url = getGetDocumentsUrl(parameters, deletedSince);
        return new DocumentNetworkProvider.GetDeletedDocumentsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getDeletedDocuments(Page next) {
        return new DocumentNetworkProvider.GetDeletedDocumentsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Document> postDocument(Document document) {
        return new DocumentNetworkProvider.PostDocumentRequest(document, authTokenManager, clientCredentials);
    }


    @Override
    public Request<Document> patchDocument(String documentId, Date date, Document document) {
        return new DocumentNetworkProvider.PatchDocumentAuthorizedRequest(documentId, document, date, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> trashDocument(String documentId) {
        return new PostNetworkRequest<Void>(getTrashDocumentUrl(documentId), authTokenManager, clientCredentials) {
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
        return new DeleteAuthorizedRequest(DocumentNetworkProvider.getDeleteDocumentUrl(documentId), authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> deleteTrashedDocument(String documentId) {
        return new DeleteAuthorizedRequest(TrashNetworkProvider.getDeleteUrl(documentId), authTokenManager, clientCredentials);
    }

    @Override
    public Request<Map<String, String>> getDocumentTypes()  {
        return new DocumentNetworkProvider.GetDocumentTypesRequest(Uri.parse(DocumentNetworkProvider.DOCUMENT_TYPES_BASE_URL), authTokenManager, clientCredentials);
    }

    @Override
    public Request<Map<String, String>> getIdentifierTypes() {
        return new DocumentNetworkProvider.GetDocumentTypesRequest(Uri.parse(DocumentNetworkProvider.IDENTIFIER_TYPES_BASE_URL), authTokenManager, clientCredentials);
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
            return new AnnotationsNetworkProvider.GetAnnotationsRequest(url, authTokenManager, clientCredentials);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode the URL for getting annotations", e);
        }
    }

    @Override
    public Request<List<Annotation>> getAnnotations(Page next) {
        return new AnnotationsNetworkProvider.GetAnnotationsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Annotation> getAnnotation(String annotationId) {
        Uri url = getAnnotationUrl(annotationId);
        return new AnnotationsNetworkProvider.GetAnnotationRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Annotation>  postAnnotation(Annotation annotation) {
        return new AnnotationsNetworkProvider.PostAnnotationRequest(annotation, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Annotation>  patchAnnotation(String annotationId, Annotation annotation) {
       return new AnnotationsNetworkProvider.PatchAnnotationAuthorizedRequest(annotationId, annotation, authTokenManager, clientCredentials);
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
            Uri url = FileNetworkProvider.getGetFilesUrl(parameters);
            return new FileNetworkProvider.GetFilesRequest(url, authTokenManager, clientCredentials);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Request<List<File>> getFiles(Page next) {
        return new FileNetworkProvider.GetFilesRequest(next.link, authTokenManager, clientCredentials);
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
        return new DeleteAuthorizedRequest(FileNetworkProvider.getDeleteFileUrl(fileId), authTokenManager, clientCredentials);
    }

    /* FOLDERS BLOCKING */

    @Override
    public Request<List<Folder>> getFolders() {
        return getFolders((FolderRequestParameters) null);
    }

    @Override
    public Request<List<Folder>> getFolders(FolderRequestParameters parameters) {
        Uri url = getGetFoldersUrl(parameters);
        return new FolderNetworkProvider.GetFoldersRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<Folder>> getFolders(Page next) {
        return new FolderNetworkProvider.GetFoldersRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Folder> getFolder(String folderId) {
        Uri url = getGetFolderUrl(folderId);
        return new FolderNetworkProvider.GetFolderRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Folder> postFolder(Folder folder) {
        Uri url = Uri.parse(FolderNetworkProvider.FOLDERS_URL);
        return new FolderNetworkProvider.PostFolderRequest(url, folder, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Folder> patchFolder(String folderId, Folder folder) {
        Uri url = FolderNetworkProvider.getPatchFolderUrl(folderId);
        return new FolderNetworkProvider.PatchFolderAuthorizedRequest(url, folder, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getFolderDocumentIds(FolderRequestParameters parameters, String folderId) {
        Uri url = getGetFoldersUrl(parameters, getGetFolderDocumentIdsUrl(folderId));
        return new FolderNetworkProvider.GetFolderDocumentIdsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getFolderDocumentIds(Page next) {
        return new FolderNetworkProvider.GetFolderDocumentIdsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> postDocumentToFolder(String folderId, String documentId) {
        Uri url = getPostDocumentToFolderUrl(folderId);
        return new FolderNetworkProvider.PostDocumentToFolderRequest(url, documentId, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> deleteFolder(String folderId) {
        Uri url = getDeleteFolderUrl(folderId);
        return new DeleteAuthorizedRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> deleteDocumentFromFolder(String folderId, String documentId) {
        Uri url = FolderNetworkProvider.getDeleteDocumentFromFolderUrl(folderId, documentId);
        return new DeleteAuthorizedRequest(url, authTokenManager, clientCredentials);
    }


    /* PROFILES BLOCKING */

    @Override
    public Request<Profile> getMyProfile() {
        return new ProfileNetworkProvider.GetProfileRequest(Uri.parse(ProfileNetworkProvider.PROFILES_URL + "me"), authTokenManager, clientCredentials);
    }

    @Override
    public Request<Profile> getProfile(final String profileId) {
        return new ProfileNetworkProvider.GetProfileRequest(Uri.parse(ProfileNetworkProvider.PROFILES_URL + profileId), authTokenManager, clientCredentials);
    }

    /* GROUPS BLOCKING */

    @Override
    public Request<List<Group>> getGroups(GroupRequestParameters parameters) {
        Uri url = getGetGroupsUrl(parameters);
        return new GroupNetworkProvider.GetGroupsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<Group>>  getGroups(Page next) {
        return new GroupNetworkProvider.GetGroupsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Group> getGroup(String groupId) {
        Uri url = GroupNetworkProvider.getGetGroupUrl(groupId);
        return new GroupNetworkProvider.GetGroupRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<UserRole>> getGroupMembers(GroupRequestParameters parameters, String groupId) {
        Uri url = getGetGroupsUrl(parameters, getGetGroupMembersUrl(groupId));
        return new GroupNetworkProvider.GetGroupMembersRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<UserRole>> getGroupMembers(Page next){
        return new GroupNetworkProvider.GetGroupMembersRequest(next.link, authTokenManager, clientCredentials);

    }

    /* TRASH BLOCKING */

    @Override
    public Request<List<Document>> getTrashedDocuments(){
        return getTrashedDocuments((DocumentRequestParameters) null);
    }

    @Override
    public Request<List<Document>> getTrashedDocuments(DocumentRequestParameters parameters) {
        Uri url = DocumentNetworkProvider.getTrashDocumentsUrl(parameters, null);
        return new DocumentNetworkProvider.GetDocumentsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<Document>> getTrashedDocuments(Page next) {
        return new DocumentNetworkProvider.GetDocumentsRequest(next.link, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Void> restoreDocument(String documentId) {
        return new PostNetworkRequest<Void>(TrashNetworkProvider.getRecoverUrl(documentId), authTokenManager, clientCredentials) {
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
        Uri url = CatalogDocumentNetworkProvider.getGetCatalogDocumentsUrl(parameters);
        return new CatalogDocumentNetworkProvider.GetCatalogDocumentsRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<Document> getCatalogDocument(String catalogId, View view) {
        Uri url = CatalogDocumentNetworkProvider.getGetCatalogDocumentUrl(catalogId, view);
        return new CatalogDocumentNetworkProvider.GetCatalogDocumentRequest(url, authTokenManager, clientCredentials);
    }


    /* RECENTLY READ POSITIONS */

    @Override
    public Request<List<ReadPosition>> getRecentlyRead(String groupId, String fileId, int limit) {
        Uri url = RecentlyReadNetworkProvider.getGetRecentlyReadUrl(groupId, fileId, limit);
        return new RecentlyReadNetworkProvider.GetRecentlyReadRequest(url, authTokenManager, clientCredentials);
    }

    @Override
    public Request<ReadPosition> postRecentlyRead(ReadPosition readPosition) {
        return new RecentlyReadNetworkProvider.PostRecentlyReadRequest(readPosition, authTokenManager, clientCredentials);
    }

    @Override
    public Request<List<String>> getApplicationFeatures() {
        return new ApplicationFeaturesNetworkProvider.GetApplicationFeaturesProcedure(authTokenManager, clientCredentials);
    }


}
