package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.FileDownloadException;
import com.mendeley.api.request.GetNetworkRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.mendeley.api.request.NetworkUtils.API_URL;

/**
 * {@link Request} to download the binary of a file (usually the pdf file)
 */
public class GetFileNetworkRequest extends GetNetworkRequest<Long> {

    private static String filesUrl = API_URL + "files";

    private static final String PARTIALLY_DOWNLOADED_EXTENSION = ".part";
    private final String fileId;
    private final File targetFile;

    public GetFileNetworkRequest(String fileId, File targetFile, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(filesUrl + "/" + fileId, null, authTokenManager, clientCredentials);
        this.fileId = fileId;
        this.targetFile = targetFile;
    }

    @Override
    protected Long manageResponse(InputStream is) throws IOException, FileDownloadException {
        final File tempFile = new File(targetFile.getParent(), targetFile.getName() + PARTIALLY_DOWNLOADED_EXTENSION);

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
