package com.mendeley.api.network.provider;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.callbacks.RequestHandle;
import com.mendeley.api.callbacks.document.GetDocumentsCallback;
import com.mendeley.api.callbacks.trash.RestoreDocumentCallback;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.exceptions.NoMorePagesException;
import com.mendeley.api.network.Environment;
import com.mendeley.api.network.NullRequest;
import com.mendeley.api.network.task.PostNoBodyNetworkTask;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.Page;

import java.io.UnsupportedEncodingException;

import static com.mendeley.api.network.NetworkUtils.API_URL;
import static com.mendeley.api.network.provider.DocumentNetworkProvider.GetDocumentsTask;
import static com.mendeley.api.network.provider.DocumentNetworkProvider.getTrashDocumentsUrl;

public class TrashNetworkProvider {
    public static String BASE_URL = API_URL + "trash";

    private final Environment environment;
    private final AccessTokenProvider accessTokenProvider;

    public TrashNetworkProvider(Environment environment, AccessTokenProvider accessTokenProvider) {
        this.environment = environment;
        this.accessTokenProvider = accessTokenProvider;
    }

    public RequestHandle doGetDocuments(DocumentRequestParameters params, GetDocumentsCallback callback) {
        try {
            String[] paramsArray = new String[] { getTrashDocumentsUrl(params, null) };
            GetDocumentsTask getDocumentsTask = new GetDocumentsTask(callback, accessTokenProvider);
            getDocumentsTask.executeOnExecutor(environment.getExecutor(), paramsArray);
            return getDocumentsTask;
        }
        catch (UnsupportedEncodingException e) {
            callback.onDocumentsNotReceived(new MendeleyException(e.getMessage(), e));
            return NullRequest.get();
        }
    }

    public RequestHandle doGetDocuments(Page next, GetDocumentsCallback callback) {
        if (Page.isValidPage(next)) {
            String[] paramsArray = new String[] { next.link };
            GetDocumentsTask getDocumentsTask = new GetDocumentsTask(callback, accessTokenProvider);
            getDocumentsTask.executeOnExecutor(environment.getExecutor(), paramsArray);
            return getDocumentsTask;
        } else {
            callback.onDocumentsNotReceived(new NoMorePagesException());
            return NullRequest.get();
        }
    }

    public void doPostRecoverDocument(String documentId, RestoreDocumentCallback callback) {
        String[] paramsArray = new String[] { getRecoverUrl(documentId), documentId };
        PostRecoverDocumentTask postRecoverDocumentTask = new PostRecoverDocumentTask(callback, documentId);
        postRecoverDocumentTask.executeOnExecutor(environment.getExecutor(), paramsArray);
    }

    /* URLS */

    public static String getRecoverUrl(String documentId) {
        return BASE_URL + "/" + documentId + "/restore";
    }

    public static String getDeleteUrl(String documentId) {
        return BASE_URL + "/" + documentId;
    }

    /* TASKS */

    private class PostRecoverDocumentTask extends PostNoBodyNetworkTask {
        private final RestoreDocumentCallback callback;

        private final String documentId;

        private PostRecoverDocumentTask(RestoreDocumentCallback callback, String documentId) {
            this.callback = callback;
            this.documentId = documentId;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onDocumentRestored(documentId);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onDocumentNotRestored(exception);
        }
    }
}
