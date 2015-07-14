package com.mendeley.api.callbacks.file;

import com.mendeley.api.exceptions.MendeleyException;

public interface GetFileCallback {
    void onFileDownloadProgress(String fileId, int progress);
    void onFileReceived(String fileId, java.io.File downloadedFile);
    void onFileNotReceived(String fileId, MendeleyException mendeleyException);
}
