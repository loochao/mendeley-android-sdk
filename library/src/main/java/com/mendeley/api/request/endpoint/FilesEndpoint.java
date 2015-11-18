package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.File;
import com.mendeley.api.request.DeleteAuthorizedRequest;
import com.mendeley.api.request.GetAuthorizedRequest;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.params.FileRequestParameters;
import com.mendeley.api.util.DateUtils;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.Request.MENDELEY_API_BASE_URL;


public class FilesEndpoint {

    public final static String FILES_BASE_URL = MENDELEY_API_BASE_URL + "files";
    public static final String FILES_CONTENT_TYPE = "application/vnd.mendeley-file.1+json";

    /**
     * Building the url for get files
     *
     * @param params the file request parameters
     * @return the url string
     * @throws UnsupportedEncodingException
     */
    public static Uri getGetFilesUrl(FileRequestParameters params) {
        try {
            StringBuilder url = new StringBuilder();
            url.append(FILES_BASE_URL);

            if (params != null) {
                boolean firstParam = true;
                if (params.documentId != null) {
                    url.append(firstParam ? "?" : "&").append("document_id=" + params.documentId);
                    firstParam = false;
                }
                if (params.groupId != null) {
                    url.append(firstParam ? "?" : "&").append("group_id=" + params.groupId);
                    firstParam = false;
                }
                if (params.addedSince != null) {
                    url.append(firstParam ? "?" : "&").append("added_since=" + URLEncoder.encode(DateUtils.formatMendeleyApiTimestamp(params.addedSince), "ISO-8859-1"));
                    firstParam = false;
                }
                if (params.deletedSince != null) {
                    url.append(firstParam ? "?" : "&").append("deleted_since=" + URLEncoder.encode(DateUtils.formatMendeleyApiTimestamp(params.deletedSince), "ISO-8859-1"));
                    firstParam = false;
                }
                if (params.limit != null) {
                    url.append(firstParam ? "?" : "&").append("limit=" + params.limit);
                    firstParam = false;
                }
                if (params.catalogId != null) {
                    url.append(firstParam ? "?" : "&").append("catalog_id=" + params.catalogId);
                    firstParam = false;
                }
            }

            return Uri.parse(url.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not parse date", e);
        }
    }

    /**
     * Building the url for get files
     *
     * @param fileId the id of the file to get
     * @return the url string
     */
    String getGetFileUrl(String fileId) {
        return FILES_BASE_URL +"/"+fileId;
    }

    /**
     * Building the url for delete files
     *
     * @param fileId the id of the file to delete
     * @return the url string
     */
    public static Uri getDeleteFileUrl(String fileId) {
        return Uri.parse(FILES_BASE_URL + "/" + fileId);
    }

    /* PROCEDURES */

    public static class GetFilesRequest extends GetAuthorizedRequest<List<File>> {
        public GetFilesRequest(Uri url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, authTokenManager, clientCredentials);
        }

        public GetFilesRequest(FileRequestParameters parameters, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            this(FilesEndpoint.getGetFilesUrl(parameters), authTokenManager, clientCredentials);
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
            super(FilesEndpoint.getDeleteFileUrl(fileId), authTokenManager, clientCredentials);
        }
    }

}
