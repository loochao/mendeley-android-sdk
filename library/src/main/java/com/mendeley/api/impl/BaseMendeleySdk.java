package com.mendeley.api.impl;

import com.mendeley.api.BlockingSdk;
import com.mendeley.api.auth.AuthenticationInterface;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.callbacks.MendeleySignInInterface;
import com.mendeley.api.callbacks.RequestHandle;
import com.mendeley.api.callbacks.annotations.AnnotationList;
import com.mendeley.api.callbacks.document.DocumentIdList;
import com.mendeley.api.callbacks.document.DocumentList;
import com.mendeley.api.callbacks.file.FileList;
import com.mendeley.api.callbacks.folder.FolderList;
import com.mendeley.api.callbacks.group.GroupList;
import com.mendeley.api.callbacks.group.GroupMembersList;
import com.mendeley.api.callbacks.read_position.ReadPositionList;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.exceptions.NoMorePagesException;
import com.mendeley.api.exceptions.NotSignedInException;
import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.Folder;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.Profile;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.network.Environment;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.procedure.DeleteNetworkProcedure;
import com.mendeley.api.network.procedure.GetFileNetworkProcedure;
import com.mendeley.api.network.procedure.PostFileNetworkProcedure;
import com.mendeley.api.network.procedure.PostNoBodyNetworkProcedure;
import com.mendeley.api.network.procedure.Procedure;
import com.mendeley.api.network.provider.AnnotationsNetworkProvider;
import com.mendeley.api.network.provider.AnnotationsNetworkProvider.GetAnnotationProcedure;
import com.mendeley.api.network.provider.AnnotationsNetworkProvider.GetAnnotationsProcedure;
import com.mendeley.api.network.provider.AnnotationsNetworkProvider.PatchAnnotationProcedure;
import com.mendeley.api.network.provider.CatalogDocumentNetworkProvider;
import com.mendeley.api.network.provider.DocumentNetworkProvider;
import com.mendeley.api.network.provider.DocumentNetworkProvider.GetDeletedDocumentsProcedure;
import com.mendeley.api.network.provider.DocumentNetworkProvider.GetDocumentProcedure;
import com.mendeley.api.network.provider.DocumentNetworkProvider.GetDocumentTypesProcedure;
import com.mendeley.api.network.provider.DocumentNetworkProvider.GetDocumentsProcedure;
import com.mendeley.api.network.provider.DocumentNetworkProvider.PatchDocumentProcedure;
import com.mendeley.api.network.provider.FileNetworkProvider;
import com.mendeley.api.network.provider.FileNetworkProvider.GetFilesProcedure;
import com.mendeley.api.network.provider.FolderNetworkProvider;
import com.mendeley.api.network.provider.FolderNetworkProvider.GetFolderDocumentIdsProcedure;
import com.mendeley.api.network.provider.FolderNetworkProvider.GetFolderProcedure;
import com.mendeley.api.network.provider.FolderNetworkProvider.GetFoldersProcedure;
import com.mendeley.api.network.provider.FolderNetworkProvider.PatchFolderProcedure;
import com.mendeley.api.network.provider.FolderNetworkProvider.PostDocumentToFolderProcedure;
import com.mendeley.api.network.provider.GroupNetworkProvider;
import com.mendeley.api.network.provider.GroupNetworkProvider.GetGroupMembersProcedure;
import com.mendeley.api.network.provider.GroupNetworkProvider.GetGroupsProcedure;
import com.mendeley.api.network.provider.ProfileNetworkProvider;
import com.mendeley.api.network.provider.ProfileNetworkProvider.GetProfileProcedure;
import com.mendeley.api.network.provider.RecentlyReadNetworkProvider;
import com.mendeley.api.network.provider.TrashNetworkProvider;
import com.mendeley.api.network.provider.UtilsNetworkProvider;
import com.mendeley.api.params.AnnotationRequestParameters;
import com.mendeley.api.params.CatalogDocumentRequestParameters;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.FileRequestParameters;
import com.mendeley.api.params.FolderRequestParameters;
import com.mendeley.api.params.GroupRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.api.params.View;

import org.json.JSONException;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import static com.mendeley.api.network.provider.AnnotationsNetworkProvider.deleteAnnotationUrl;
import static com.mendeley.api.network.provider.AnnotationsNetworkProvider.getAnnotationUrl;
import static com.mendeley.api.network.provider.AnnotationsNetworkProvider.getAnnotationsUrl;
import static com.mendeley.api.network.provider.DocumentNetworkProvider.DOCUMENT_TYPES_BASE_URL;
import static com.mendeley.api.network.provider.DocumentNetworkProvider.IDENTIFIER_TYPES_BASE_URL;
import static com.mendeley.api.network.provider.DocumentNetworkProvider.getGetDocumentUrl;
import static com.mendeley.api.network.provider.DocumentNetworkProvider.getGetDocumentsUrl;
import static com.mendeley.api.network.provider.DocumentNetworkProvider.getTrashDocumentUrl;
import static com.mendeley.api.network.provider.FolderNetworkProvider.getDeleteFolderUrl;
import static com.mendeley.api.network.provider.FolderNetworkProvider.getGetFolderDocumentIdsUrl;
import static com.mendeley.api.network.provider.FolderNetworkProvider.getGetFolderUrl;
import static com.mendeley.api.network.provider.FolderNetworkProvider.getGetFoldersUrl;
import static com.mendeley.api.network.provider.FolderNetworkProvider.getPostDocumentToFolderUrl;
import static com.mendeley.api.network.provider.GroupNetworkProvider.getGetGroupMembersUrl;
import static com.mendeley.api.network.provider.GroupNetworkProvider.getGetGroupsUrl;
import static com.mendeley.api.network.provider.ProfileNetworkProvider.PROFILES_URL;

/**
 * Implementation of the blocking API calls.
 */
public abstract class BaseMendeleySdk implements BlockingSdk, Environment {

    public static final String TAG = BaseMendeleySdk.class.getSimpleName();

    // Number of times to retry failed HTTP requests due to IOExceptions.
    public static final int MAX_HTTP_RETRIES = 3;

    protected AuthenticationManager authenticationManager;
    protected MendeleySignInInterface mendeleySignInInterface;

    protected DocumentNetworkProvider documentNetworkProvider;
    protected FileNetworkProvider fileNetworkProvider;
    protected ProfileNetworkProvider profileNetworkProvider;
    protected FolderNetworkProvider folderNetworkProvider;
    protected GroupNetworkProvider groupNetworkProvider;
    protected UtilsNetworkProvider utilsNetworkProvider;
    protected TrashNetworkProvider trashNetworkProvider;
    protected AnnotationsNetworkProvider annotationsNetworkProvider;

    protected void initProviders() {
        documentNetworkProvider = new DocumentNetworkProvider(this, authenticationManager);
        fileNetworkProvider = new FileNetworkProvider(this, authenticationManager);
        profileNetworkProvider = new ProfileNetworkProvider(this, authenticationManager);
        folderNetworkProvider = new FolderNetworkProvider(this, authenticationManager);
        groupNetworkProvider = new GroupNetworkProvider(this, authenticationManager);
        utilsNetworkProvider = new UtilsNetworkProvider(this, authenticationManager);
        trashNetworkProvider = new TrashNetworkProvider(this, authenticationManager);
        annotationsNetworkProvider = new AnnotationsNetworkProvider(this, authenticationManager);
    }

    protected AuthenticationInterface createAuthenticationInterface() {
        return new AuthenticationInterface() {
            @Override
            public void onAuthenticated(boolean manualSignIn) {
                if (mendeleySignInInterface != null && manualSignIn) {
                    mendeleySignInInterface.onSignedIn();
                }
            }

            @Override
            public void onAuthenticationFail() {
                if (mendeleySignInInterface != null) {
                    mendeleySignInInterface.onSignInFailure();
                }
            }
        };
    }

    public interface Command {
        RequestHandle exec();
    }



    /* DOCUMENTS BLOCKING */

    @Override
    public DocumentList getDocuments(DocumentRequestParameters parameters) throws MendeleyException {
        try {
            String url = getGetDocumentsUrl(parameters, null);
            Procedure<DocumentList> proc = new GetDocumentsProcedure(url, authenticationManager);
            return proc.checkedRun();
        } catch (UnsupportedEncodingException e) {
            throw new MendeleyException(e.getMessage(), e);
        }
    }

    @Override
    public DocumentList getDocuments() throws MendeleyException {
        return getDocuments((DocumentRequestParameters) null);
    }

    @Override
    public DocumentList getDocuments(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<DocumentList> proc = new GetDocumentsProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Document getDocument(String documentId, View view) throws MendeleyException {
        String url = getGetDocumentUrl(documentId, view);
        Procedure<Document> proc = new GetDocumentProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public DocumentIdList getDeletedDocuments(String deletedSince, DocumentRequestParameters parameters) throws MendeleyException {
        try {
            String url = getGetDocumentsUrl(parameters, deletedSince);
            Procedure<DocumentIdList> proc = new GetDeletedDocumentsProcedure(url, authenticationManager);
            return proc.checkedRun();
        } catch (UnsupportedEncodingException e) {
            throw new MendeleyException(e.getMessage(), e);
        }
    }

    @Override
    public DocumentIdList getDeletedDocuments(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<DocumentIdList> proc =
                new GetDeletedDocumentsProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Document postDocument(Document document) throws MendeleyException {
        try {
            Procedure<Document> proc
                    = new DocumentNetworkProvider.PostDocumentProcedure(document, authenticationManager);
            return proc.checkedRun();
        } catch (JSONException e) {
            throw new JsonParsingException(e.getMessage());
        }
    }

    @Override
    public Document patchDocument(String documentId, Date date, Document document) throws MendeleyException {
        try {
            String json = JsonParser.jsonFromDocument(document);
            Procedure<Document> proc = new PatchDocumentProcedure(documentId, json, date, authenticationManager);
            return proc.checkedRun();
        } catch (JSONException e) {
            throw new JsonParsingException(e.getMessage());
        }
    }

    @Override
    public void trashDocument(String documentId) throws MendeleyException {
        Procedure<Void> proc = new PostNoBodyNetworkProcedure(getTrashDocumentUrl(documentId), authenticationManager);
        proc.checkedRun();
    }

    @Override
    public void deleteDocument(String documentId) throws MendeleyException {
        Procedure<Void> proc = new DeleteNetworkProcedure(DocumentNetworkProvider.getDeleteDocumentUrl(documentId), authenticationManager);
        proc.checkedRun();
    }

    @Override
    public void deleteTrashedDocument(String documentId) throws MendeleyException {
        Procedure<Void> proc = new DeleteNetworkProcedure(TrashNetworkProvider.getDeleteUrl(documentId), authenticationManager);
        proc.checkedRun();
    }

    @Override
    public Map<String, String> getDocumentTypes() throws MendeleyException {
        Procedure<Map<String, String>> proc =
                new GetDocumentTypesProcedure(DOCUMENT_TYPES_BASE_URL, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Map<String, String> getIdentifierTypes() throws MendeleyException {
        Procedure<Map<String, String>> proc =
                new GetDocumentTypesProcedure(IDENTIFIER_TYPES_BASE_URL, authenticationManager);
        return proc.checkedRun();
    }

    /* ANNOTATIONS BLOCKING */

    @Override
    public AnnotationList getAnnotations(AnnotationRequestParameters parameters) throws MendeleyException {
        try {
            String url = getAnnotationsUrl(parameters);
            Procedure<AnnotationList> proc = new GetAnnotationsProcedure(url, authenticationManager);
            return proc.checkedRun();
        } catch (UnsupportedEncodingException e) {
            throw new MendeleyException("Could not encode the URL for getting annotations", e);
        }
    }

    @Override
    public AnnotationList getAnnotations() throws MendeleyException {
        return getAnnotations((AnnotationRequestParameters) null);
    }

    @Override
    public AnnotationList getAnnotations(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<AnnotationList> proc = new GetAnnotationsProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Annotation getAnnotation(String annotationId) throws MendeleyException {
        String url = getAnnotationUrl(annotationId);
        Procedure<Annotation> proc = new GetAnnotationProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Annotation postAnnotation(Annotation annotation) throws MendeleyException {
        try {
            Procedure<Annotation> proc
                    = new AnnotationsNetworkProvider.PostAnnotationProcedure(annotation, authenticationManager);
            return proc.checkedRun();
        } catch (JSONException e) {
            throw new JsonParsingException("Error parsing annotation", e);
        }
    }

    @Override
    public Annotation patchAnnotation(String annotationId, Annotation annotation) throws MendeleyException {
        try {
            String json = JsonParser.jsonFromAnnotation(annotation);
            Procedure<Annotation> proc = new PatchAnnotationProcedure(annotationId, json, authenticationManager);
            return proc.checkedRun();
        } catch (JSONException e) {
            throw new JsonParsingException("Error parsing annotation", e);
        }
    }

    @Override
    public void deleteAnnotation(String annotationId) throws MendeleyException {
        Procedure proc = new DeleteNetworkProcedure(deleteAnnotationUrl(annotationId), authenticationManager);
        proc.checkedRun();
    }

    /* FILES BLOCKING */

    @Override
    public FileList getFiles(FileRequestParameters parameters) throws MendeleyException {
        try {
            String url = FileNetworkProvider.getGetFilesUrl(parameters);
            Procedure<FileList> proc = new GetFilesProcedure(url, authenticationManager);
            return proc.checkedRun();
        }
        catch (UnsupportedEncodingException e) {
            throw new MendeleyException(e.getMessage(), e);
        }
    }

    @Override
    public FileList getFiles() throws MendeleyException {
        return getFiles((FileRequestParameters) null);
    }

    @Override
    public FileList getFiles(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<FileList> proc = new GetFilesProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public long getFile(String fileId, java.io.File targetFile) throws MendeleyException {
        Procedure<Long> proc = new GetFileNetworkProcedure(fileId, targetFile, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public File postFile(String contentType, String documentId, InputStream inputStream, String fileName) throws MendeleyException {
        Procedure<File> proc = new PostFileNetworkProcedure(contentType, documentId, fileName, inputStream, authenticationManager);
        return proc.checkedRun();
    }


    @Override
    public void deleteFile(String fileId) throws MendeleyException {
        Procedure<Void> proc = new DeleteNetworkProcedure(FileNetworkProvider.getDeleteFileUrl(fileId), authenticationManager);
        proc.checkedRun();
    }



    /* FOLDERS BLOCKING */

    @Override
    public FolderList getFolders(FolderRequestParameters parameters) throws MendeleyException {
        String url = getGetFoldersUrl(parameters);
        Procedure<FolderList> proc = new GetFoldersProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public FolderList getFolders() throws MendeleyException {
        return getFolders((FolderRequestParameters) null);
    }

    @Override
    public FolderList getFolders(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<FolderList> proc = new GetFoldersProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Folder getFolder(String folderId) throws MendeleyException {
        String url = getGetFolderUrl(folderId);
        Procedure<Folder> proc = new GetFolderProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Folder postFolder(Folder folder) throws MendeleyException {
        try {
            String url = FolderNetworkProvider.FOLDERS_URL;
            String json = JsonParser.jsonFromFolder(folder);
            Procedure<Folder> proc = new FolderNetworkProvider.PostFolderProcedure(url, json, authenticationManager);
            return proc.checkedRun();
        } catch (JSONException e) {
            throw new JsonParsingException(e.getMessage());
        }
    }

    @Override
    public Folder patchFolder(String folderId, Folder folder) throws MendeleyException {
        String folderString = null;
        try {
            String url = FolderNetworkProvider.getPatchFolderUrl(folderId);
            folderString  = JsonParser.jsonFromFolder(folder);
            Procedure<Folder> proc = new PatchFolderProcedure(url, folderString, authenticationManager);
            return proc.checkedRun();
        } catch (JSONException e) {
            throw new JsonParsingException(e.getMessage());
        }
    }

    @Override
    public DocumentIdList getFolderDocumentIds(FolderRequestParameters parameters, String folderId) throws MendeleyException {
        String url = getGetFoldersUrl(parameters, getGetFolderDocumentIdsUrl(folderId));
        Procedure<DocumentIdList> proc = new GetFolderDocumentIdsProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public DocumentIdList getFolderDocumentIds(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<DocumentIdList> proc = new GetFolderDocumentIdsProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public void postDocumentToFolder(String folderId, String documentId) throws MendeleyException {
        try {
            String documentString = JsonParser.jsonFromDocumentId(documentId);
            String url = getPostDocumentToFolderUrl(folderId);
            Procedure proc = new PostDocumentToFolderProcedure(url, documentString, authenticationManager);
            proc.checkedRun();
        } catch (JSONException e) {
            throw new JsonParsingException(e.getMessage());
        }
    }

    @Override
    public void deleteFolder(String folderId) throws MendeleyException {
        String url = getDeleteFolderUrl(folderId);
        Procedure proc = new DeleteNetworkProcedure(url, authenticationManager);
        proc.checkedRun();
    }

    @Override
    public void deleteDocumentFromFolder(String folderId, String documentId) throws MendeleyException {
        String url = FolderNetworkProvider.getDeleteDocumentFromFolderUrl(folderId, documentId);
        Procedure proc = new DeleteNetworkProcedure(url, authenticationManager);
        proc.checkedRun();
    }

    /* PROFILES BLOCKING */

    @Override
    public Profile getMyProfile() throws MendeleyException {
        Procedure<Profile> proc = new GetProfileProcedure(PROFILES_URL + "me", authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Profile getProfile(final String profileId) throws MendeleyException {
        Procedure<Profile> proc = new GetProfileProcedure(PROFILES_URL + profileId, authenticationManager);
        return proc.checkedRun();
    }

    /* GROUPS BLOCKING */

    @Override
    public GroupList getGroups(GroupRequestParameters parameters) throws MendeleyException {
        String url = getGetGroupsUrl(parameters);
        Procedure<GroupList> proc = new GetGroupsProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public GroupList getGroups(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<GroupList> proc = new GetGroupsProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public Group getGroup(String groupId) throws MendeleyException {
        String url = GroupNetworkProvider.getGetGroupUrl(groupId);
        Procedure<Group> proc = new GroupNetworkProvider.GetGroupProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public GroupMembersList getGroupMembers(GroupRequestParameters parameters, String groupId) throws MendeleyException {
        String url = getGetGroupsUrl(parameters, getGetGroupMembersUrl(groupId));
        Procedure<GroupMembersList> proc = new GetGroupMembersProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public GroupMembersList getGroupMembers(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<GroupMembersList> proc = new GetGroupMembersProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public byte[] getImage(String url) throws MendeleyException {
        return utilsNetworkProvider.getImage(url);
    }

    /* TRASH BLOCKING */

    @Override
    public DocumentList getTrashedDocuments(DocumentRequestParameters parameters) throws MendeleyException {
        try {
            String url = DocumentNetworkProvider.getTrashDocumentsUrl(parameters, null);
            Procedure<DocumentList> proc = new GetDocumentsProcedure(url, authenticationManager);
            return proc.checkedRun();
        } catch (UnsupportedEncodingException e) {
            throw new MendeleyException(e.getMessage(), e);
        }
    }

    @Override
    public DocumentList getTrashedDocuments() throws MendeleyException {
        return getTrashedDocuments((DocumentRequestParameters) null);
    }

    @Override
    public DocumentList getTrashedDocuments(Page next) throws MendeleyException {
        if (!Page.isValidPage(next)) {
            throw new NoMorePagesException();
        }
        Procedure<DocumentList> proc = new GetDocumentsProcedure(next.link, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public void restoreDocument(String documentId) throws MendeleyException {
        String url = TrashNetworkProvider.getRecoverUrl(documentId);
        Procedure<Void> proc = new PostNoBodyNetworkProcedure(url, authenticationManager);
        proc.checkedRun();
    }

    /* CATALOG BLOCKING */

    @Override
    public DocumentList getCatalogDocuments(CatalogDocumentRequestParameters parameters) throws MendeleyException {
        try {
            String url = CatalogDocumentNetworkProvider.getGetCatalogDocumentsUrl(parameters);
            Procedure<DocumentList> proc = new CatalogDocumentNetworkProvider.GetCatalogDocumentsProcedure(url, authenticationManager);
            return proc.checkedRun();
        } catch (UnsupportedEncodingException e) {
            throw new MendeleyException(e.getMessage(), e);
        }
    }

    @Override
    public Document getCatalogDocument(String catalogId, View view) throws MendeleyException {
        String url = CatalogDocumentNetworkProvider.getGetCatalogDocumentUrl(catalogId, view);
        Procedure<Document> proc = new CatalogDocumentNetworkProvider.GetCatalogDocumentProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    /* RECENTLY READ POSITIONS */

    @Override
    public ReadPositionList getRecentlyRead(String groupId, String fileId, int limit) throws MendeleyException {
        String url = RecentlyReadNetworkProvider.getGetRecentlyReadUrl(groupId, fileId, limit);
        Procedure<ReadPositionList> proc = new RecentlyReadNetworkProvider.GetRecentlyReadProcedure(url, authenticationManager);
        return proc.checkedRun();
    }

    @Override
    public ReadPosition postRecentlyRead(ReadPosition readPosition) throws MendeleyException {
        try {
            Procedure<ReadPosition> proc = new RecentlyReadNetworkProvider.PostRecentlyReadProcedure(readPosition, authenticationManager);
            return proc.checkedRun();
        } catch (JSONException e) {
            throw new JsonParsingException(e.getMessage(), e);
        }
    }

    /**
     * Internal use only.
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    protected RequestHandle run(Command command) {
        if (authenticationManager == null || !authenticationManager.isSignedIn()) {
            // Must call startSignInProcess first - caller error!
            throw new NotSignedInException();
        }
        if (authenticationManager.willExpireSoon()) {
            return authenticationManager.refreshToken(command);
        } else {
            /*
                If the expiry time check above hasn't worked for some reason, and the server
                believes the credentials have expired anyway, we will get 401 Unauthorized,
                message "Could not access resource because: Token has expired".
                Currently we do not catch this.
                In future we intend to do so, refresh the token and run the command.
                The 401 error code and message is under review by the Platform team.
            */
            return command.exec();
        }
    }
}
