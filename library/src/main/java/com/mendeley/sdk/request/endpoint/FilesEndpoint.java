package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.request.DeleteAuthorizedRequest;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.util.DateUtils;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mendeley.sdk.request.Request.MENDELEY_API_BASE_URL;


public class FilesEndpoint {

    public final static String FILES_BASE_URL = MENDELEY_API_BASE_URL + "files";
    public static final String FILES_CONTENT_TYPE = "application/vnd.mendeley-file.1+json";

    /* PROCEDURES */

    public static class GetFilesRequest extends GetAuthorizedRequest<List<File>> {
        private static Uri getGetFilesUrl(FileRequestParameters params) {
            final Uri.Builder bld = Uri.parse(FILES_BASE_URL).buildUpon();
            if (params == null) {
                return bld.build();
            }
            return params.appendToUi(bld.build());
        }

        public GetFilesRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetFilesRequest(FileRequestParameters parameters, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(getGetFilesUrl(parameters), authTokenManager, clientCredentials);
        }

        @Override
        protected List<File> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseFileList(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", FILES_CONTENT_TYPE);
        }
    }

    public static class DeleteFileRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteFileRequest(String fileId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(FILES_BASE_URL + "/" + fileId), authTokenManager, clientCredentials);
        }
    }

    /**
     * This class represents parameters for file SDK requests.
     * <p>
     * Uninitialised properties will be ignored.
     */
    public static class FileRequestParameters {
        /**
         * The document ID.
         */
        public String documentId;

        /**
         * The group ID.
         */
        public String groupId;

        /**
         * Returns only files added since this timestamp. Should be supplied in ISO 8601 format.
         */
        public Date addedSince;

        /**
         * Returns files deleted since this timestamp. Should be supplied in ISO 8601 format.
         */
        public Date deletedSince;

        /**
         * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
         */
        public Integer limit;

        /**
         * The catalog ID.
         */
        public String catalogId;


        Uri appendToUi(Uri uri) {
            final Uri.Builder bld = uri.buildUpon();

            if (documentId != null) {
                bld.appendQueryParameter("document_id", documentId);
            }
            if (groupId != null) {
                bld.appendQueryParameter("group_id", groupId);
            }
            if (addedSince != null) {
                bld.appendQueryParameter("added_since", DateUtils.formatMendeleyApiTimestamp(addedSince));
            }
            if (deletedSince != null) {
                bld.appendQueryParameter("deleted_since", DateUtils.formatMendeleyApiTimestamp(deletedSince));
            }
            if (limit != null) {
                bld.appendQueryParameter("limit", String.valueOf(limit));
            }
            if (catalogId != null) {
                bld.appendQueryParameter("catalog_id", catalogId);
            }
            return bld.build();
        }
    }
}
