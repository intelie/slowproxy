package net.intelie.slowproxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;

class TransferTask implements Runnable {
    private final InputStream input;
    private final OutputStream output;
    private final int maxBytes;
    private final AtomicLong bytes;

    public TransferTask(InputStream input, OutputStream output, int maxBytes, AtomicLong bytes) {
        this.input = input;
        this.output = output;
        this.maxBytes = maxBytes;
        this.bytes = bytes;
    }

    @Override
    public void run() {
        try {
            int bufferSize = maxBytes > 0 ? Math.max(maxBytes / 1024, 1) : 1024;
            Throttler throttler = new Throttler(Math.max(maxBytes / bufferSize, 1), 1000);
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = input.read(buffer)) >= 0) {
                if (maxBytes > 0) {
                    long wait = throttler.offer(System.currentTimeMillis());
                    while (wait > 0) {
                        Thread.sleep(wait);
                        wait = throttler.offer(System.currentTimeMillis());
                    }
                }
                output.write(buffer, 0, len);
                bytes.addAndGet(len);
            }
            input.close();
            output.close();
        } catch (SocketException e) {
            if (!"Socket closed".equals(e.getMessage()))
                e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
