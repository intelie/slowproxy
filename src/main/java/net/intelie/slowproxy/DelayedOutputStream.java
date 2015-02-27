package net.intelie.slowproxy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DelayedOutputStream extends OutputStream {
    private final OutputStream inner;
    private final long delay;
    private final Semaphore full, empty;
    private final byte[] buffer;
    private final ScheduledExecutorService executor;

    private volatile int start = 0, end = 0;
    private volatile IOException exception;

    public DelayedOutputStream(OutputStream inner, long delay, int bufferSize) {
        this(inner, delay, bufferSize, Executors.newSingleThreadScheduledExecutor());
    }

    private DelayedOutputStream(OutputStream inner, long delay, int bufferSize, ScheduledExecutorService executor) {
        this.inner = inner;
        this.executor = executor;
        this.buffer = new byte[bufferSize];
        this.delay = delay;
        this.full = new Semaphore(0);
        this.empty = new Semaphore(bufferSize);
    }

    @Override
    public void write(int i) throws IOException {
        write(new byte[]{(byte) i});
    }

    @Override
    public void write(byte[] b, int off, final int len) throws IOException {
        if (exception != null)
            throw exception;

        empty.acquireUninterruptibly(len);
        if (end + len < buffer.length) {
            System.arraycopy(b, off, buffer, end, len);

        } else {
            int first = buffer.length - end;
            System.arraycopy(b, off, buffer, end, first);
            System.arraycopy(b, off + first, buffer, 0, len - first);
        }
        end = (end + len) % buffer.length;

        executor.schedule(new Runnable() {
            @Override
            public void run() {
                if (exception != null)
                    return;
                full.acquireUninterruptibly(len);
                try {
                    if (start + len < buffer.length) {
                        inner.write(buffer, start, len);
                    } else {
                        int first = buffer.length - start;
                        inner.write(buffer, start, first);
                        inner.write(buffer, 0, len - first);
                    }

                    start = (start + len) % buffer.length;
                } catch (IOException e) {
                    exception = e;
                } finally {
                    empty.release(len);
                }
            }
        }, delay, TimeUnit.MILLISECONDS);

        full.release(len);
    }

    @Override
    public void flush() throws IOException {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        inner.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        inner.close();
        if (exception != null)
            throw exception;
    }
}
