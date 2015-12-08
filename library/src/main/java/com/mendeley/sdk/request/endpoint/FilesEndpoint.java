package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.exceptions.FileDownloadException;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.request.DeleteAuthorizedRequest;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.request.PostAuthorizedRequest;
import com.mendeley.sdk.request.Request;
import com.mendeley.sdk.util.DateUtils;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

        public GetFilesRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(url, authTokenManager, appCredentials);
        }

        public GetFilesRequest(FileRequestParameters parameters, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            this(getGetFilesUrl(parameters), authTokenManager, appCredentials);
        }

        @Override
        protected List<File> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.filesFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", FILES_CONTENT_TYPE);
        }
    }

    /**
     * {@link Request} to download the binary of a file (usually the pdf file)
     */
    public static class GetFileBinaryRequest extends GetAuthorizedRequest<Long> {

        private static String filesUrl = MENDELEY_API_BASE_URL + "files";

        private static final String PARTIALLY_DOWNLOADED_EXTENSION = ".part";
        private final String fileId;
        private final java.io.File targetFile;

        public GetFileBinaryRequest(String fileId, java.io.File targetFile, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(filesUrl + "/" + fileId), authTokenManager, appCredentials);
            this.fileId = fileId;
            this.targetFile = targetFile;
        }

        @Override
        protected Long manageResponse(InputStream is) throws IOException, FileDownloadException {
            final java.io.File tempFile = new java.io.File(targetFile.getParent(), targetFile.getName() + PARTIALLY_DOWNLOADED_EXTENSION);

            long total = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

            byte data[] = new byte[1024];
            int count;

            while (!isCancelled() && (count = is.read(data)) != -1) {
                total += count;
                fileOutputStream.write(data, 0, count);
            }

            if (!tempFile.renameTo(targetFile)) {
                throw new FileDownloadException("Cannot rename downloaded file", fileId);
            } else {
                return total;
            }
        }

        public String getFileId() {
            return fileId;
        }
    }

    public static class PostFileWithBinaryRequest extends PostAuthorizedRequest<File> {
        private final String contentType;
        private final String documentId;
        private final String fileName;
        private final InputStream inputStream;

        private static String filesUrl = MENDELEY_API_BASE_URL + "files";

        public PostFileWithBinaryRequest(String contentType, String documentId, String fileName, InputStream inputStream, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(filesUrl), authTokenManager, appCredentials);
            this.contentType = contentType;
            this.documentId = documentId;
            this.fileName = fileName;
            this.inputStream = inputStream;
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-Disposition", "attachment; filename*=UTF-8\'\'" + fileName);
            headers.put("Content-type", contentType);
            headers.put("Link", "<" + MENDELEY_API_BASE_URL + "documents/" + documentId + ">; rel=\"document\"");
        }

        @Override
        protected void writePostBody(OutputStream os) throws Exception {
            final int bufferSize = 65536;
            final byte[] buffer = new byte[bufferSize];
            int r;
            while ((r =  inputStream.read(buffer, 0, bufferSize)) > 0) {
                os.write(buffer, 0, r);
            }
        }

        @Override
        protected File manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.fileFromJson(reader);
        }

    }

    public static class DeleteFileRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteFileRequest(String fileId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(FILES_BASE_URL + "/" + fileId), authTokenManager, appCredentials);
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
