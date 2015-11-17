package com.mendeley.api.request;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.File;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

public class PostFileAuthorizedRequest extends PostNetworkRequest<File> {
    private final String contentType;
    private final String documentId;
    private final String fileName;
    private final InputStream inputStream;

    private static String filesUrl = API_URL + "files";

    public PostFileAuthorizedRequest(String contentType, String documentId, String fileName, InputStream inputStream, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(Uri.parse(filesUrl), authTokenManager, clientCredentials);
        this.contentType = contentType;
        this.documentId = documentId;
        this.fileName = fileName;
        this.inputStream = inputStream;
    }

    @Override
    protected void appendHeaders(Map<String, String> headers) {
        headers.put("Content-Disposition", "attachment; filename*=UTF-8\'\'"+fileName);
        headers.put("Content-type", contentType);
        headers.put("Link", "<" + API_URL+"documents/"+documentId+">; rel=\"document\"");
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
        return JsonParser.parseFile(reader);
    }


}
