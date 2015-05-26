package com.mendeley.api.request.provider;

import android.os.AsyncTask;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.File;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.procedure.GetNetworkRequest;
import com.mendeley.api.request.params.FileRequestParameters;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Files API calls
 */
public class FileNetworkProvider {
	private Map<String, AsyncTask> fileTaskMap = new HashMap<String, AsyncTask>();

	private static String filesUrl = API_URL + "files";
	private static final String TAG = FileNetworkProvider.class.getSimpleName();

    public FileNetworkProvider() {
    }


    /**
     * Building the url for get files
     *
     * @param params the file request parameters
     * @return the url string
     * @throws UnsupportedEncodingException
     */
    public static String getGetFilesUrl(FileRequestParameters params) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        url.append(filesUrl);

        if (params != null) {
            boolean firstParam = true;
            if (params.documentId != null) {
                url.append(firstParam?"?":"&").append("document_id="+params.documentId);
                firstParam = false;
            }
            if (params.groupId != null) {
                url.append(firstParam?"?":"&").append("group_id="+params.groupId);
                firstParam = false;
            }
            if (params.addedSince != null) {
                url.append(firstParam?"?":"&").append("added_since="+URLEncoder.encode(params.addedSince, "ISO-8859-1"));
                firstParam = false;
            }
            if (params.deletedSince != null) {
                url.append(firstParam?"?":"&").append("deleted_since="+URLEncoder.encode(params.deletedSince, "ISO-8859-1"));
                firstParam = false;
            }
            if (params.limit != null) {
                url.append(firstParam?"?":"&").append("limit="+params.limit);
                firstParam = false;
            }
            if (params.catalogId != null) {
                url.append(firstParam?"?":"&").append("catalog_id="+params.catalogId);
                firstParam = false;
            }
        }

        return url.toString();
    }

    /**
     * Building the url for get files
     *
     * @param fileId the id of the file to get
     * @return the url string
     */
    String getGetFileUrl(String fileId) {
        return filesUrl+"/"+fileId;
    }

    /**
     * Building the url for delete files
     *
     * @param fileId the id of the file to delete
     * @return the url string
     */
    public static String getDeleteFileUrl(String fileId) {
        return filesUrl + "/" + fileId;
    }

    /* PROCEDURES */

    public static class GetFilesRequest extends GetNetworkRequest<List<File>> {
        public GetFilesRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-file.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected List<File> parseJsonString(String jsonString) throws JSONException {
            return JsonParser.parseFileList(jsonString);
        }
    }

}
