package com.mendeley.sdk.exceptions;

/**
 * Exception that is thrown when a file fails to download.
 */
public class FileDownloadException extends MendeleyException {
	private final String fileId;

	public FileDownloadException(String message, String fileId) {
		super(message);
		this.fileId = fileId;
	}

	public String getFileId() {
		return this.fileId;
	}

    public FileDownloadException(String detailMessage, Throwable throwable, String fileId) {
        super(detailMessage, throwable);
        this.fileId = fileId;
    }
}
