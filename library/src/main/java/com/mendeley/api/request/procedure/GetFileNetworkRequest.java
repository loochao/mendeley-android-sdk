package com.mendeley.api.request.procedure;

import android.text.TextUtils;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.FileDownloadException;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.request.NetworkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.HttpsURLConnection;

import static com.mendeley.api.request.NetworkUtils.API_URL;
import static com.mendeley.api.request.NetworkUtils.getConnection;

public class GetFileNetworkRequest extends NetworkRequest<Long> {

    private static String filesUrl = API_URL + "files";

    private static final String PARTIALLY_DOWNLOADED_EXTENSION = ".part";
    private final String fileId;
    private final File targetFile;

    public GetFileNetworkRequest(String fileId, File targetFile, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.fileId = fileId;
        this.targetFile = targetFile;
    }

    @Override
    protected int getExpectedResponse() {
        return 201;
    }

    @Override
    protected RequestResponse<Long> doRun() throws MendeleyException {

        final String url = filesUrl + "/" + fileId;
        long total = 0;

        FileOutputStream fileOutputStream = null;
        HttpsURLConnection con = null;
        InputStream is = null;

        try {
            con = getConnection(url, "GET", authTokenManager);

            // TODO: try to do this code simpler by redirecting delegating into HttpsURLConnection
            con.setInstanceFollowRedirects(false);
            con.connect();


            if (con.getResponseCode() != 303) {
                throw HttpResponseException.create(con);
            } else {
                con.disconnect();

                final String location = con.getHeaderField("location");

                if (TextUtils.isEmpty(location)) {
                    throw new FileDownloadException("Server did not provide redirect url", fileId);
                }

                con = NetworkUtils.getDownloadConnection(location, "GET");
                con.connect();


                if (con.getResponseCode() != 200) {
                    throw new FileDownloadException("HTTP status error downloading file.", HttpResponseException.create(con), fileId);
                } else {

                    final File tempFile = new File(targetFile.getParent(), targetFile.getName() + PARTIALLY_DOWNLOADED_EXTENSION);

                    int fileLength = con.getContentLength();
                    is = con.getInputStream();
                    fileOutputStream = new FileOutputStream(tempFile);

                    byte data[] = new byte[1024];
                    int count;

                    while (!isCancelled() && (count = is.read(data)) != -1) {
                        total += count;
                        // TODO: publish progress
//                        if (fileLength > 0) {
//                            int progress = (int) (total * 100 / fileLength);
//                        }
                        fileOutputStream.write(data, 0, count);
                    }

                    if (!tempFile.renameTo(targetFile)) {
                        throw new FileDownloadException("Cannot rename downloaded file", fileId);
                    }
                }
            }
            return new RequestResponse(total, serverDate);
        } catch (Exception e) {
            throw new MendeleyException("Could not download file " + fileId, e);
        } finally {

            if (con != null) {
                con.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public String getFileId() {
        return fileId;
    }
}
