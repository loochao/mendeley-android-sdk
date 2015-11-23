package com.mendeley.sdk.request;


import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

/**
 * Wrapper over {@link InputStream} to publish report the progress of reading through it
 */
public abstract class CancellableInputStream extends InputStream {

    private final InputStream delegate;
    private Future l;

    public CancellableInputStream(InputStream delegate) {
        this.delegate = delegate;
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
        if (isCancelled()) {
            throw new CancellationException("Stream has been cancelled");
        }
        return delegate.read(buffer, byteOffset, byteCount);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return delegate.skip(byteCount);
    }

    protected abstract boolean isCancelled();

}
