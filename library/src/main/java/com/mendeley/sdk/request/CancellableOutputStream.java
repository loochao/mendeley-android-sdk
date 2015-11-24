package com.mendeley.sdk.request;


import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CancellationException;

/**
 * Wrapper over {@link java.io.OutputStream} that will stop writing to it if it's been cancelled
 */
public abstract class CancellableOutputStream extends OutputStream {

    private final OutputStream delegate;

    protected CancellableOutputStream(OutputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        delegate.write(buffer);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (isCancelled()) {
            throw new CancellationException("Writing to output stream interrupted due to cancellation");
        }
        delegate.write(buffer, offset, count);
    }

    @Override
    public void write(int oneByte) throws IOException {
        if (isCancelled()) {
            throw new CancellationException("Writing to output stream interrupted due to cancellation");
        }
        delegate.write(oneByte);
    }

    protected abstract boolean isCancelled();

}
