package com.mendeley.api.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.SignInCallback;
import com.mendeley.api.activity.SignInActivity;
import com.mendeley.api.activity.SignInOrSignUpActivity;
import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.Folder;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.Profile;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.request.params.AnnotationRequestParameters;
import com.mendeley.api.request.params.CatalogDocumentRequestParameters;
import com.mendeley.api.request.params.DocumentRequestParameters;
import com.mendeley.api.request.params.FileRequestParameters;
import com.mendeley.api.request.params.FolderRequestParameters;
import com.mendeley.api.request.params.GroupRequestParameters;
import com.mendeley.api.request.params.Page;
import com.mendeley.api.request.params.View;
import com.mendeley.api.request.procedure.GetFileNetworkRequest;
import com.mendeley.api.request.procedure.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Mendeley implements RequestsFactory {

    private static final String TAG = Mendeley.class.getSimpleName();
    private static Mendeley instance;

    private final ClientCredentials clientCredentials;
    private final AuthTokenManager authTokenManager;
    private final RequestsFactory requestsFactoryDelegate;

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

    private Mendeley(ClientCredentials clientCredentials, AuthTokenManager authTokenManager, RequestsFactory requestsFactoryDelegate) {
        this.clientCredentials = clientCredentials;
        this.authTokenManager = authTokenManager;
        this.requestsFactoryDelegate = requestsFactoryDelegate;
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

    public ClientCredentials getClientCredentials() {
        return clientCredentials;
    }

    public AuthTokenManager getAuthTokenManager() {
        return authTokenManager;
    }

    public boolean isSignedIn() {
        return !TextUtils.isEmpty(authTokenManager.getAccessToken());
    }

    //------------------

    @Override
    public Request<List<Document>> getDocuments() {
        return requestsFactoryDelegate.getDocuments();
    }

    @Override
    public Request<List<Document>> getDocuments(DocumentRequestParameters parameters) {
        return requestsFactoryDelegate.getDocuments(parameters);
    }

    @Override
    public Request<List<Document>> getDocuments(Page next) {
        return requestsFactoryDelegate.getDocuments(next);
    }

    @Override
    public Request<Document> getDocument(String documentId, View view) {
        return requestsFactoryDelegate.getDocument(documentId, view);
    }

    @Override
    public Request<List<String>> getDeletedDocuments(String deletedSince, DocumentRequestParameters parameters) {
        return requestsFactoryDelegate.getDeletedDocuments(deletedSince, parameters);
    }

    @Override
    public Request<List<String>> getDeletedDocuments(Page next) {
        return requestsFactoryDelegate.getDeletedDocuments(next);
    }

    @Override
    public Request<Document> postDocument(Document document) {
        return requestsFactoryDelegate.postDocument(document);
    }

    @Override
    public Request<Document> patchDocument(String documentId, Date date, Document document) {
        return requestsFactoryDelegate.patchDocument(documentId, date, document);
    }

    @Override
    public Request<Void> trashDocument(String documentId) {
        return requestsFactoryDelegate.trashDocument(documentId);
    }

    @Override
    public Request<Void> deleteDocument(String documentId) {
        return requestsFactoryDelegate.deleteDocument(documentId);
    }

    @Override
    public Request<Void> deleteTrashedDocument(String documentId) {
        return requestsFactoryDelegate.deleteTrashedDocument(documentId);
    }

    @Override
    public Request<Map<String, String>> getDocumentTypes() {
        return requestsFactoryDelegate.getDocumentTypes();
    }

    @Override
    public Request<Map<String, String>> getIdentifierTypes() {
        return requestsFactoryDelegate.getIdentifierTypes();
    }

    @Override
    public Request<List<File>> getFiles(FileRequestParameters parameters) {
        return requestsFactoryDelegate.getFiles(parameters);
    }

    @Override
    public Request<List<File>> getFiles() {
        return requestsFactoryDelegate.getFiles();
    }

    @Override
    public Request<List<File>> getFiles(Page next) {
        return requestsFactoryDelegate.getFiles(next);
    }

    @Override
    public GetFileNetworkRequest getFileBinary(String fileId, java.io.File targetFile) {
        return requestsFactoryDelegate.getFileBinary(fileId, targetFile);
    }

    @Override
    public Request<File> postFileBinary(String contentType, String documentId, InputStream inputStream, String fileName) {
        return requestsFactoryDelegate.postFileBinary(contentType, documentId, inputStream, fileName);
    }

    @Override
    public Request<Void> deleteFile(String fileId) {
        return requestsFactoryDelegate.deleteFile(fileId);
    }

    @Override
    public Request<List<Folder>> getFolders(FolderRequestParameters parameters) {
        return requestsFactoryDelegate.getFolders(parameters);
    }

    @Override
    public Request<List<Folder>> getFolders() {
        return requestsFactoryDelegate.getFolders();
    }

    @Override
    public Request<List<Folder>> getFolders(Page next) {
        return requestsFactoryDelegate.getFolders(next);
    }

    @Override
    public Request<Folder> getFolder(String folderId) {
        return requestsFactoryDelegate.getFolder(folderId);
    }

    @Override
    public Request<Folder> postFolder(Folder folder) {
        return requestsFactoryDelegate.postFolder(folder);
    }

    @Override
    public Request<Folder> patchFolder(String folderId, Folder folder) {
        return requestsFactoryDelegate.patchFolder(folderId, folder);
    }

    @Override
    public Request<List<String>> getFolderDocumentIds(FolderRequestParameters parameters, String folderId) {
        return requestsFactoryDelegate.getFolderDocumentIds(parameters, folderId);
    }

    @Override
    public Request<List<String>> getFolderDocumentIds(Page next) {
        return requestsFactoryDelegate.getFolderDocumentIds(next);
    }

    @Override
    public Request<Void> postDocumentToFolder(String folderId, String documentId) {
        return requestsFactoryDelegate.postDocumentToFolder(folderId, documentId);
    }

    @Override
    public Request<Void> deleteFolder(String folderId) {
        return requestsFactoryDelegate.deleteFolder(folderId);
    }

    @Override
    public Request<Void> deleteDocumentFromFolder(String folderId, String documentId) {
        return requestsFactoryDelegate.deleteDocumentFromFolder(folderId, documentId);
    }

    @Override
    public Request<List<Group>> getGroups(GroupRequestParameters parameters) {
        return requestsFactoryDelegate.getGroups(parameters);
    }

    @Override
    public Request<List<Group>> getGroups(Page next) {
        return requestsFactoryDelegate.getGroups(next);
    }

    @Override
    public Request<Group> getGroup(String groupId) {
        return requestsFactoryDelegate.getGroup(groupId);
    }

    @Override
    public Request<List<UserRole>> getGroupMembers(GroupRequestParameters parameters, String groupId) {
        return requestsFactoryDelegate.getGroupMembers(parameters, groupId);
    }

    @Override
    public Request<List<UserRole>> getGroupMembers(Page next) {
        return requestsFactoryDelegate.getGroupMembers(next);
    }

    @Override
    public Request<List<Document>> getTrashedDocuments(DocumentRequestParameters parameters) {
        return requestsFactoryDelegate.getTrashedDocuments(parameters);
    }

    @Override
    public Request<List<Document>> getTrashedDocuments() {
        return requestsFactoryDelegate.getTrashedDocuments();
    }

    @Override
    public Request<List<Document>> getTrashedDocuments(Page next) {
        return requestsFactoryDelegate.getTrashedDocuments(next);
    }

    @Override
    public Request<Void> restoreDocument(String documentId) {
        return requestsFactoryDelegate.restoreDocument(documentId);
    }

    @Override
    public Request<Profile> getMyProfile() {
        return requestsFactoryDelegate.getMyProfile();
    }

    @Override
    public Request<Profile> getProfile(String profileId) {
        return requestsFactoryDelegate.getProfile(profileId);
    }

    @Override
    public Request<List<Document>> getCatalogDocuments(CatalogDocumentRequestParameters parameters) {
        return requestsFactoryDelegate.getCatalogDocuments(parameters);
    }

    @Override
    public Request<Document> getCatalogDocument(String catalogId, View view) {
        return requestsFactoryDelegate.getCatalogDocument(catalogId, view);
    }

    @Override
    public Request<List<Annotation>> getAnnotations() {
        return requestsFactoryDelegate.getAnnotations();
    }

    @Override
    public Request<List<Annotation>> getAnnotations(AnnotationRequestParameters parameters) {
        return requestsFactoryDelegate.getAnnotations(parameters);
    }

    @Override
    public Request<List<Annotation>> getAnnotations(Page next) {
        return requestsFactoryDelegate.getAnnotations(next);
    }

    @Override
    public Request<Annotation> getAnnotation(String annotationId) {
        return requestsFactoryDelegate.getAnnotation(annotationId);
    }

    @Override
    public Request<Annotation> postAnnotation(Annotation annotation) {
        return requestsFactoryDelegate.postAnnotation(annotation);
    }

    @Override
    public Request<Annotation> patchAnnotation(String annotationId, Annotation annotation) {
        return requestsFactoryDelegate.patchAnnotation(annotationId, annotation);
    }

    @Override
    public Request<Void> deleteAnnotation(String annotationId) {
        return requestsFactoryDelegate.deleteAnnotation(annotationId);
    }

    @Override
    public Request<List<ReadPosition>> getRecentlyRead(String groupId, String fileId, int limit) {
        return requestsFactoryDelegate.getRecentlyRead(groupId, fileId, limit);
    }

    @Override
    public Request<ReadPosition> postRecentlyRead(ReadPosition readPosition) {
        return requestsFactoryDelegate.postRecentlyRead(readPosition);
    }

    @Override
    public Request<List<String>> getApplicationFeatures() {
        return requestsFactoryDelegate.getApplicationFeatures();
    }


}
