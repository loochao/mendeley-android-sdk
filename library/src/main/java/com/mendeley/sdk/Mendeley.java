package com.mendeley.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.mendeley.sdk.activity.SignInActivity;
import com.mendeley.sdk.activity.SignInOrSignUpActivity;
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
import com.mendeley.sdk.request.endpoint.DocumentIdentifiersEndpoint;
import com.mendeley.sdk.request.endpoint.DocumentTypesEndpoint;
import com.mendeley.sdk.request.endpoint.FilesEndpoint;
import com.mendeley.sdk.request.endpoint.FoldersEndpoint;
import com.mendeley.sdk.request.endpoint.GroupsEndpoint;
import com.mendeley.sdk.request.endpoint.ProfilesEndpoint;
import com.mendeley.sdk.request.endpoint.RecentlyReadEndpoint;
import com.mendeley.sdk.request.endpoint.TrashEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Mendeley {

    private static final String TAG = Mendeley.class.getSimpleName();
    private static Mendeley instance;

    private final ClientCredentials clientCredentials;
    private final AuthTokenManager authTokenManager;
    private final RequestsFactory requestsFactory;

    public static void sdkInitialise(Context context, ClientCredentials clientCredentials) {
        final AuthTokenManager authTokenManager = new SharedPreferencesAuthTokenManager(context.getSharedPreferences("auth", Context.MODE_PRIVATE));
        final RequestFactoryImpl requestFactory = new RequestFactoryImpl(authTokenManager, clientCredentials);
        instance = new Mendeley(clientCredentials, authTokenManager, requestFactory);
    }


    /**
     * Return the MendeleySdk singleton.
     */
    public static Mendeley getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Sdk is not initialised. You must call #sdkInitialise() first.");
        }
        return instance;
    }

    private Mendeley(ClientCredentials clientCredentials, AuthTokenManager authTokenManager, RequestsFactory requestsFactory) {
        this.clientCredentials = clientCredentials;
        this.authTokenManager = authTokenManager;
        this.requestsFactory = requestsFactory;
    }

    /**
     * Sign the user in.
     * @param activity used for creating the sign-in activity.
     * @param showSignUpScreen whether to show the screen with UI to create a new account before the sign-in dialog
     */
    public void signIn(Activity activity, boolean showSignUpScreen) {
        final Intent intent;
        if (showSignUpScreen) {
            intent = new Intent(activity, SignInOrSignUpActivity.class);
        } else {
            intent = new Intent(activity, SignInActivity.class);
        }
        activity.startActivityForResult(intent, SignInActivity.AUTH_REQUEST_CODE);
    }

    public void signOut() {
        authTokenManager.clearTokens();
    }


    public final boolean onActivityResult(int requestCode, int resultCode, Intent data, SignInCallback signInCallback) {
        switch (requestCode) {
            case SignInActivity.AUTH_REQUEST_CODE:
                onLoginActivityResult(resultCode, data, signInCallback);
                return true;
            default:
                return false;
        }
    }

    private void onLoginActivityResult(int resultCode, Intent data, SignInCallback signInCallback) {
        if (resultCode == Activity.RESULT_OK && onJsonStringResult(data.getStringExtra(SignInActivity.EXTRA_JSON_TOKENS))) {
            signInCallback.onSignedIn();
        } else {
            signInCallback.onSignInFailure();
        }
    }

    private boolean onJsonStringResult(String jsonTokenString) {
        try {
            JSONObject tokenObject = new JSONObject(jsonTokenString);

            String accessToken = tokenObject.getString("access_token");
            String refreshToken = tokenObject.getString("refresh_token");
            String tokenType = tokenObject.getString("token_type");
            int expiresIn = tokenObject.getInt("expires_in");

            authTokenManager.saveTokens(accessToken, refreshToken, tokenType, expiresIn);
            return true;
        } catch (JSONException e) {
            // If the client credentials are incorrect, the tokenString contains an error message
            Log.e(TAG, "Could not parse the json response with the auth tokens: " + jsonTokenString, e);
            return false;
        }
    }

    public boolean isSignedIn() {
        return !TextUtils.isEmpty(authTokenManager.getAccessToken());
    }

    public ClientCredentials getClientCredentials() {
        return clientCredentials;
    }

    public AuthTokenManager getAuthTokenManager() {
        return authTokenManager;
    }

    public RequestsFactory getRequestFactory() {return requestsFactory; }


    /**
     * Interface that should be implemented by the application for receiving callbacks for sign
     * in events.
     */
    public interface SignInCallback {
        void onSignedIn();
        void onSignInFailure();
    }

    /**
     * Implementation of the blocking API calls.
     */
    public static class RequestFactoryImpl implements RequestsFactory {

        public static final String TAG = RequestFactoryImpl.class.getSimpleName();

        private final ClientCredentials clientCredentials;
        private final AuthTokenManager authTokenManager;

        public RequestFactoryImpl(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this.authTokenManager = authTokenManager;
            this.clientCredentials = clientCredentials;
        }

        /* DOCUMENTS */

        @Override
        public Request<List<Document>> newGetDocumentsRequest() {
            return newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null);
        }

        @Override
        public Request<List<Document>> newGetDocumentsRequest(DocumentEndpoint.DocumentRequestParameters parameters) {
            return new DocumentEndpoint.GetDocumentsRequest(parameters, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<Document>> newGetDocumentsRequest(Uri url) {
            return new DocumentEndpoint.GetDocumentsRequest(url, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Document> newGetDocumentRequest(String documentId, DocumentEndpoint.DocumentRequestParameters.View view) {
            return new DocumentEndpoint.GetDocumentRequest(documentId, view, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Document> newPostDocumentRequest(Document document) {
            return new DocumentEndpoint.PostDocumentRequest(document, authTokenManager, clientCredentials);
        }


        @Override
        public Request<Document> newPatchDocumentRequest(String documentId, Date date, Document document) {
            return new DocumentEndpoint.PatchDocumentAuthorizedRequest(documentId, document, date, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Void> newTrashDocumentRequest(String documentId) {
            return new DocumentEndpoint.TrashDocumentRequest(documentId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Void> newDeleteDocumentRequest(String documentId) {
            return new DocumentEndpoint.DeleteDocumentRequest(documentId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Void> newDeleteTrashedDocumentRequest(String documentId) {
            return new TrashEndpoint.DeleteTrashedDocumentRequest(documentId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Map<String, String>> newGetDocumentTypesRequest()  {
            return new DocumentTypesEndpoint.GetDocumentTypesRequest(authTokenManager, clientCredentials);
        }

        @Override
        public Request<Map<String, String>> newGetDocumentIdentifierTypesRequest() {
            return new DocumentIdentifiersEndpoint.GetDocumentIdentifiersRequest(authTokenManager, clientCredentials);
        }

        /* ANNOTATIONS */

        @Override
        public Request<List<Annotation>> newGetAnnotationsRequest() {
            return newGetAnnotationsRequest((AnnotationsEndpoint.AnnotationRequestParameters) null);
        }

        @Override
        public Request<List<Annotation>> newGetAnnotationsRequest(AnnotationsEndpoint.AnnotationRequestParameters parameters) {
            return new AnnotationsEndpoint.GetAnnotationsRequest(parameters, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<Annotation>> newGetAnnotationsRequest(Uri url) {
            return new AnnotationsEndpoint.GetAnnotationsRequest(url, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Annotation> newGetAnnotationRequest(String annotationId) {
            return new AnnotationsEndpoint.GetAnnotationRequest(annotationId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Annotation> newPostAnnotationRequest(Annotation annotation) {
            return new AnnotationsEndpoint.PostAnnotationRequest(annotation, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Annotation> newPatchAnnotationRequest(String annotationId, Annotation annotation) {
           return new AnnotationsEndpoint.PatchAnnotationRequest(annotationId, annotation, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Void> newDeleteAnnotationRequest(String annotationId) {
            return new AnnotationsEndpoint.DeleteAnnotationRequest(annotationId, authTokenManager, clientCredentials);
        }

        /* FILES BLOCKING */

        @Override
        public Request<List<File>> newGetFilesRequest() {
            return newGetFilesRequest((FilesEndpoint.FileRequestParameters) null);
        }

        @Override
        public Request<List<File>> newGetFilesRequest(FilesEndpoint.FileRequestParameters parameters) {
            return new FilesEndpoint.GetFilesRequest(parameters, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<File>> newGetFilesRequest(Uri uri) {
            return new FilesEndpoint.GetFilesRequest(uri, authTokenManager, clientCredentials);
        }

        @Override
        public FilesEndpoint.GetFileBinaryRequest newGetFileBinaryRequest(String fileId, java.io.File targetFile) {
            return new FilesEndpoint.GetFileBinaryRequest(fileId, targetFile, authTokenManager, clientCredentials);
        }

        @Override
        public Request<File> newPostFileWithBinaryRequest(String contentType, String documentId, InputStream inputStream, String fileName) {
            return new FilesEndpoint.PostFileWithBinaryRequest(contentType, documentId, fileName, inputStream, authTokenManager, clientCredentials);
        }


        @Override
        public Request<Void> newDeleteFileRequest(String fileId) {
            return new FilesEndpoint.DeleteFileRequest(fileId, authTokenManager, clientCredentials);
        }

        /* FOLDERS BLOCKING */

        @Override
        public Request<List<Folder>> newGetFoldersRequest() {
            return newGetFoldersRequest((FoldersEndpoint.FolderRequestParameters) null);
        }

        @Override
        public Request<List<Folder>> newGetFoldersRequest(FoldersEndpoint.FolderRequestParameters parameters) {
            return new FoldersEndpoint.GetFoldersRequest(parameters, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<Folder>> newGetFoldersRequest(Uri uri) {
            return new FoldersEndpoint.GetFoldersRequest(uri, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Folder> newGetFolderRequest(String folderId) {
            return new FoldersEndpoint.GetFolderRequest(folderId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Folder> newPostFolderRequest(Folder folder) {
            return new FoldersEndpoint.PostFolderRequest(folder, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Folder> newPatchFolderRequest(String folderId, Folder folder) {
            return new FoldersEndpoint.PatchFolderAuthorizedRequest(folderId, folder, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<String>> newGetFolderDocumentsRequest(FoldersEndpoint.FolderRequestParameters parameters, String folderId) {
            return new FoldersEndpoint.GetFolderDocumentIdsRequest(parameters, folderId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<String>> newGetFolderDocumentsRequest(Uri uri) {
            return new FoldersEndpoint.GetFolderDocumentIdsRequest(uri, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Void> newPostDocumentToFolderRequest(String folderId, String documentId) {
            return new FoldersEndpoint.PostDocumentToFolderRequest(folderId, documentId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Void> newDeleteFolderRequest(String folderId) {
            return new FoldersEndpoint.DeleteFolderRequest(folderId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Void> newDeleteDocumentFromFolderRequest(String folderId, String documentId) {
            return new FoldersEndpoint.DeleteDocumentFromFolder(folderId, documentId, authTokenManager, clientCredentials);
        }


        /* PROFILES */

        @Override
        public Request<Profile> newGetMyProfileRequest() {
            return new ProfilesEndpoint.GetProfileRequest("me", authTokenManager, clientCredentials);
        }

        @Override
        public Request<Profile> newGetProfileRequest(final String profileId) {
            return new ProfilesEndpoint.GetProfileRequest(profileId, authTokenManager, clientCredentials);
        }

        /* GROUPS */

        @Override
        public Request<List<Group>> newGetGroupsRequest(GroupsEndpoint.GroupRequestParameters parameters) {
            return new GroupsEndpoint.GetGroupsRequest(parameters, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<Group>> newGetGroupsRequest(Uri uri) {
            return new GroupsEndpoint.GetGroupsRequest(uri, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Group> newGetGroupRequest(String groupId) {
            return new GroupsEndpoint.GetGroupRequest(groupId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<UserRole>> newGetGroupMembersRequest(GroupsEndpoint.GroupRequestParameters parameters, String groupId) {
            return new GroupsEndpoint.GetGroupMembersRequest(parameters, groupId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<UserRole>> newGetGroupMembersRequest(Uri url){
            return new GroupsEndpoint.GetGroupMembersRequest(url, authTokenManager, clientCredentials);

        }

        /* TRASH */

        @Override
        public Request<List<Document>> newGetTrashedDocumentsRequest() {
            return newGetTrashedDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null);
        }

        @Override
        public Request<List<Document>> newGetTrashedDocumentsRequest(DocumentEndpoint.DocumentRequestParameters parameters) {
            return new TrashEndpoint.GetTrashedDocumentsRequest(parameters, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<Document>> newGetTrashedDocumentsRequest(Uri uri) {
            return new TrashEndpoint.GetTrashedDocumentsRequest(uri, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Void> newRestoreTrashedDocumentRequest(String documentId) {
            return new TrashEndpoint.RestoreTrashedDocumentRequest(documentId, authTokenManager, clientCredentials);
        }

        /* CATALOG  */

        @Override
        public Request<List<Document>> newGetCatalogDocumentsRequest(CatalogEndpoint.CatalogDocumentRequestParameters parameters) {
            return new CatalogEndpoint.GetCatalogDocumentsRequest(parameters, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Document> newGetCatalogDocumentRequest(String catalogId, DocumentEndpoint.DocumentRequestParameters.View view) {
            return new CatalogEndpoint.GetCatalogDocumentRequest(catalogId, view, authTokenManager, clientCredentials);
        }

        /* RECENTLY READ */

        @Override
        public Request<List<ReadPosition>> newGetRecentlyReadRequest(String groupId, String fileId, int limit) {
            return new RecentlyReadEndpoint.GetRecentlyReadRequest(groupId, fileId, limit, authTokenManager, clientCredentials);
        }

        @Override
        public Request<ReadPosition> newPostRecentlyReadRequest(ReadPosition readPosition) {
            return new RecentlyReadEndpoint.PostRecentlyReadRequest(readPosition, authTokenManager, clientCredentials);
        }

    }

    /**
     * Adds async calls to BaseMendeleySdk.
     */
    static class SharedPreferencesAuthTokenManager implements AuthTokenManager {

        // Shared preferences keys:
        private static final String ACCESS_TOKEN_KEY = "accessToken";
        private static final String REFRESH_TOKEN_KEY = "refreshToken";
        private static final String EXPIRES_AT_KEY = "expiresAtDate";
        private static final String TOKEN_TYPE_KEY = "tokenType";

        private final SharedPreferences preferences;

        SharedPreferencesAuthTokenManager(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public void saveTokens(String accessToken, String refreshToken, String tokenType, int expiresIn)  {
            Date expiresAt = generateExpiresAtFromExpiresIn(expiresIn);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(ACCESS_TOKEN_KEY, accessToken);
            editor.putString(REFRESH_TOKEN_KEY, refreshToken);
            editor.putString(TOKEN_TYPE_KEY, tokenType);
            editor.putLong(EXPIRES_AT_KEY, expiresAt.getTime());
            editor.commit();
        }

        @Override
        public void clearTokens() {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(ACCESS_TOKEN_KEY);
            editor.remove(REFRESH_TOKEN_KEY);
            editor.remove(EXPIRES_AT_KEY);
            editor.remove(TOKEN_TYPE_KEY);
            editor.commit();
        }

        @Override
        public Date getAuthTenExpiresAt() {
            return new Date(preferences.getLong(EXPIRES_AT_KEY, 0));
        }

        @Override
        public String getRefreshToken() {
            return preferences.getString(REFRESH_TOKEN_KEY, null);
        }

        /**
         * @return the access token string, or null if it does not exist.
         */
        @Override
        public String getAccessToken() {
            return preferences.getString(ACCESS_TOKEN_KEY, null);
        }

        @Override
        public String getTokenType() {
            return preferences.getString(TOKEN_TYPE_KEY, null);
        }

        private Date generateExpiresAtFromExpiresIn(int expiresIn) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.SECOND, expiresIn);
            return c.getTime();
        }
    }

}
