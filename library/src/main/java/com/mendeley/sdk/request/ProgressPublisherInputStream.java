package com.mendeley.sdk.request;


import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper over {@link InputStream} to publish report the progress of reading through it
 */
public abstract class ProgressPublisherInputStream extends InputStream {

    private final InputStream delegate;
    private final long length;

    private long total;


    public ProgressPublisherInputStream(InputStream delegate, long length) {
        this.delegate = delegate;
        this.length = length;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        final int r = delegate.read(buffer, byteOffset, byteCount);

        if (length > 0) {
            total += r;
            long progress = (total * 100 / length);
            onProgress(progress);
        }

        return r;
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return delegate.skip(byteCount);
    }

    protected abstract void onProgress(long progress);
}
