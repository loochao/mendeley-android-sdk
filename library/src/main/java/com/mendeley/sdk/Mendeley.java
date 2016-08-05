package com.mendeley.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.mendeley.sdk.model.Annotation;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.model.Folder;
import com.mendeley.sdk.model.Group;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.model.ReadPosition;
import com.mendeley.sdk.model.UserRole;
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
import com.mendeley.sdk.request.endpoint.SubjectAreasEndpoint;
import com.mendeley.sdk.request.endpoint.TrashEndpoint;
import com.mendeley.sdk.request.endpoint.UserRolesEndpoint;
import com.mendeley.sdk.ui.sign_in.SignInActivity;
import com.mendeley.sdk.util.MssoCookieManager;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Class exposing all the pubic functionality of the Mendeley SDK.
 *
 * <p/>
 *
 * This class provides default implementations of {@link AuthTokenManager}, used to get
 * a valid authorization token and {@link RequestsFactory}, used to create HTTP {@link Request}s
 * against the Mendeley API.
 *
 * <p/>
 * This class can't be instantiated by client code and should always be accessed in a
 * singleton way using {@link Mendeley#getInstance()}
 *
 * <p/>
 * First thing client code should do is initialising this class by passing a valid
 * API key and credentials calling  {@link Mendeley#init(Context, String, String)}
 *
 * <p/>
 *
 * The Mendeley API is documented in the Developer portal of Mendeley @{see http://dev.mendeley.com/}
 *
 * <p/>
 *
 * Note: developers that wants to get rid of the {@link Mendeley} singleton for better dependency
 * injection and unit testing may ignore this class and directly instantiate a {@link AuthTokenManager}
 * and {@link RequestsFactory} if they want to do so.
 */
public class Mendeley {

    private static Mendeley instance;

    private ClientCredentials clientCredentials;
    private AuthTokenManager authTokenManager;
    private MssoCookieManager mssoCookieManager;
    private RequestsFactory requestsFactory;

    /**
     * @return a reference to the @{Mendeley} SDK singleton.
     */
    public static synchronized Mendeley getInstance() {
        if (instance == null) {
            instance = new Mendeley();
        }
        return instance;
    }

    private Mendeley() {
    }

    /**
     * Initialises the SDK, providing a valid API key and credentials to obtain authorization tokens
     * from the Mendeley API
     *
     * @param context a Context, won't be kept as a reference.
     * @param appId, valid client app id
     * @param appSecret, valid client app secret
     */
    public final void init(Context context, String appId, String appSecret) {
        final SharedPreferencesAuthTokenManager sharedPreferencesAuthTokenManager = SharedPreferencesAuthTokenManager.obtain(context);

        this.authTokenManager = sharedPreferencesAuthTokenManager;
        this.mssoCookieManager = sharedPreferencesAuthTokenManager;

        this.clientCredentials = new ClientCredentials(appId, appSecret);
        this.requestsFactory = new RequestFactoryImpl(authTokenManager, clientCredentials);
    }

    /**
     * Signs the user in.
     *
     * <p/>
     *
     * Calling this method will open one {@link Activity} showing the UI that lets the user enter
     * their credentials to sign in into the Mendeley server.
     *
     * The SDK will take control of the flow, and will invoke @{Activity#onActivityResult} once the
     * sign in flow has been resolved.
     *
     * Your @{Activity} will need to pass the result back to this class invoking @{Mendeley#onActivityResult}
     * providing a {@link com.mendeley.sdk.Mendeley.SignInCallback} that can be used by your
     * code to get the final outcome of the signing in attempt.
     *
     * @param activity used for creating the sign in @{Activity}. The SDK won't hold any reference to this,
     *                 so there is no risk of leaking it.
     */
    public final void signIn(Activity activity) {
        assertInitialised();

        activity.startActivityForResult(new Intent(activity, SignInActivity.class), SignInActivity.REQUEST_CODE);
    }


    /**
     * Signs the user out.
     *
     * <p/>
     *
     * In practice, this simply means clearing the authorization tokens from the Mendeley SDK, if any.*
     */
    public void signOut() {
        assertInitialised();
        authTokenManager.clearTokens();
    }

    /**
     * To be called from the {@link Activity#onActivityResult(int, int, Intent)} of your application.
     *
     * <p/>
     *
     * Once the user has signed in to the Mendeley server, the Activity that performs that work will
     * pass the result using {@link Activity#onActivityResult(int, int, Intent)}. Hence, you need
     * to pass the result back to the SDK by calling this method in your Activity, so that the SDK
     * will analyse the result and persist the authorization token.
     *
     *
     * @param requestCode the requestCode received in the onActivityResult of your app
     * @param resultCode the resultCode received in the onActivityResult of your app
     * @param signInCallback a callback letting client code know the outcome of the sign in attempt
     *
     * @return true if the result relates to a sign in attempt and has been consumed by the Mendeley SDK
     */
    public final boolean onActivityResult(int requestCode, int resultCode, SignInCallback signInCallback) {
        assertInitialised();

        switch (requestCode) {
            case SignInActivity.REQUEST_CODE:
                onLoginActivityResult(resultCode, signInCallback);
                return true;
            default:
                return false;
        }
    }

    private void onLoginActivityResult(int resultCode, SignInCallback signInCallback) {
        if (resultCode == Activity.RESULT_OK) {
            signInCallback.onSignedIn();
        } else {
            signInCallback.onSignInFailure();
        }
    }

    /**
     * Returns whether or not the user is signed in to the Mendeley API server.
     * This is, if the SDK has the needed information (OAuth access or refresh tokens) to launch
     * HTTP {@link Request}s against the Mendeley API.
     *
     * @return if the user is signed in
     */
    public final boolean isSignedIn() {
        assertInitialised();
        return isValidToken(authTokenManager.getAccessToken()) && isValidToken(authTokenManager.getRefreshToken());
    }

    private boolean isValidToken(String token) {
        return !TextUtils.isEmpty(token) && !"null".equals(token);
    }

    /**
     * Returns one @{RequestsFactory} that you can use for creating typical {@link Request}s against
     * the Mendeley SDK.
     *
     * @return a Request factory to create typical Requests
     */
    public RequestsFactory getRequestFactory() {return requestsFactory; }

    /**
     * @return clients credentials used by this Mendeley SDK, as passed by the app
     * in the {@link Mendeley#init(Context, String, String)} method
     */
    public ClientCredentials getClientCredentials() {
        return clientCredentials;
    }

    /**
     * @return AuthTokenManager used by this Mendeley SDK
     */
    public AuthTokenManager getAuthTokenManager() {
        return authTokenManager;
    }

    public MssoCookieManager getMssoCookieManager() {
        return mssoCookieManager;
    }

    private void assertInitialised() {
        if (authTokenManager == null) {
            throw new IllegalStateException("Sdk is not initialised. You must call #sdkInitialise() first.");
        }
    }

    /**
     * Interface that should be implemented by the application for receiving callbacks for sign
     * in events.
     */
    public interface SignInCallback {
        void onSignedIn();
        void onSignInFailure();
    }

    /**
     * Default implementation of {@link RequestsFactory}
     *
     * <p/>
     *
     * Typical Android applications using the Mendeley SDK won't directly need to deal with this,
     * but this class is left public in case you don't want to use the {@link Mendeley} singleton
     * in your app and you prefer to instantiate the {@link RequestsFactory} by yourself.
     */
    public static class RequestFactoryImpl implements RequestsFactory {

        private final ClientCredentials clientCredentials;
        private final AuthTokenManager authTokenManager;

        public RequestFactoryImpl(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this.authTokenManager = authTokenManager;
            this.clientCredentials = clientCredentials;
        }

        @Override
        public Request<Profile> newGetMyProfileRequest() {
            return new ProfilesEndpoint.GetProfileRequest("me", authTokenManager, clientCredentials);
        }

        @Override
        public Request<Profile> newPatchMeProfileRequest(Profile profile) {
            return new ProfilesEndpoint.PatchMeProfileRequest(profile, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Profile> newGetProfileRequest(final String profileId) {
            return new ProfilesEndpoint.GetProfileRequest(profileId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Profile> newPostProfileRequest(Profile profile, String password) {
            return new ProfilesEndpoint.PostProfileRequest(authTokenManager, clientCredentials, profile, password);
        }

        @Override
        public Request<Void> newDeleteProfileRequest(String profileId) {
            return new ProfilesEndpoint.DeleteProfileRequest(profileId, authTokenManager, clientCredentials);
        }

        @Override
        public Request<Map<String, String>> newGetDocumentTypesRequest()  {
            return new DocumentTypesEndpoint.GetDocumentTypesRequest(authTokenManager, clientCredentials);
        }

        @Override
        public Request<Map<String, String>> newGetDocumentIdentifierTypesRequest() {
            return new DocumentIdentifiersEndpoint.GetDocumentIdentifiersRequest(authTokenManager, clientCredentials);
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
        public Request<Void> newDeleteFolderRequest(String folderId) {
            return new FoldersEndpoint.DeleteFolderRequest(folderId, authTokenManager, clientCredentials);
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
        public Request<Void> newDeleteDocumentFromFolderRequest(String folderId, String documentId) {
            return new FoldersEndpoint.DeleteDocumentFromFolder(folderId, documentId, authTokenManager, clientCredentials);
        }

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

        @Override
        public Request<List<ReadPosition>> newGetRecentlyReadRequest(String groupId, String fileId, int limit) {
            return new RecentlyReadEndpoint.GetRecentlyReadRequest(groupId, fileId, limit, authTokenManager, clientCredentials);
        }

        @Override
        public Request<ReadPosition> newPostRecentlyReadRequest(ReadPosition readPosition) {
            return new RecentlyReadEndpoint.PostRecentlyReadRequest(readPosition, authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<String>> newGetSubjectAreasRequest() {
            return new SubjectAreasEndpoint.GetSubjectAreasRequest(authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<String>> newGetUserRolesRequest() {
            return new UserRolesEndpoint.GetUserRolesRequest(authTokenManager, clientCredentials);
        }

        @Override
        public Request<List<Document>> newGetCatalogDocument(String identifier, String value) {
            return new CatalogEndpoint.GetCatalogDocumentRequest(identifier, value, authTokenManager, clientCredentials);
        }
    }


    /**
     * Implementation of {@link AuthTokenManager} that uses Shared Preferences to persist the
     * OAuth tokens.
     *
     * <p/>
     *
     * Typical Android applications using the Mendeley SDK won't directly need to deal with this,
     * but this class is left public in case you don't want to use the {@link Mendeley} singleton
     * in your app and you prefer to instantiate the {@link AuthTokenManager} by yourself.
     */
    public static class SharedPreferencesAuthTokenManager implements AuthTokenManager, MssoCookieManager {

        private static final String SHARED_PREFERENCES_NAME = "auth";

        // Shared preferences keys:
        private static final String ACCESS_TOKEN_KEY = "accessToken";
        private static final String REFRESH_TOKEN_KEY = "refreshToken";
        private static final String EXPIRES_AT_KEY = "expiresAtDate";
        private static final String TOKEN_TYPE_KEY = "tokenType";

        private static final String MSSO_COOKIE_KEY = "mssoCookie";

        public static SharedPreferencesAuthTokenManager obtain(Context context) {
            return new SharedPreferencesAuthTokenManager(context.getSharedPreferences(SharedPreferencesAuthTokenManager.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE));
        }

        private final SharedPreferences preferences;

        public SharedPreferencesAuthTokenManager(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public final void saveTokens(String accessToken, String refreshToken, String tokenType, int expiresIn)  {
            Date expiresAt = generateExpiresAtFromExpiresIn(expiresIn);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(ACCESS_TOKEN_KEY, accessToken);
            editor.putString(REFRESH_TOKEN_KEY, refreshToken);
            editor.putString(TOKEN_TYPE_KEY, tokenType);
            editor.putLong(EXPIRES_AT_KEY, expiresAt.getTime());
            editor.commit();
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public final void clearTokens() {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(ACCESS_TOKEN_KEY);
            editor.remove(REFRESH_TOKEN_KEY);
            editor.remove(EXPIRES_AT_KEY);
            editor.remove(TOKEN_TYPE_KEY);
            editor.commit();
        }

        @Override
        public final Date getAuthTokenExpirationDate() {
            return new Date(preferences.getLong(EXPIRES_AT_KEY, 0));
        }

        @Override
        public final String getRefreshToken() {
            return preferences.getString(REFRESH_TOKEN_KEY, null);
        }

        @Override
        public final  String getAccessToken() {
            return preferences.getString(ACCESS_TOKEN_KEY, null);
        }

        @Override
        public final String getTokenType() {
            return preferences.getString(TOKEN_TYPE_KEY, null);
        }

        @Override
        public void saveMSSOCookieValue(String mssoCookie) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(MSSO_COOKIE_KEY, mssoCookie);
            editor.commit();
        }

        @Override
        public String getMssoCookieValue() {
            return preferences.getString(MSSO_COOKIE_KEY, null);
        }

        private Date generateExpiresAtFromExpiresIn(int expiresIn) {
            final Calendar c = Calendar.getInstance();
            c.add(Calendar.SECOND, expiresIn);
            return c.getTime();
        }
    }
}
