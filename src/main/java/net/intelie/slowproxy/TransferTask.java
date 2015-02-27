package net.intelie.slowproxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;

class TransferTask implements Runnable {
    private final InputStream input;
    private final OutputStream output;
    private final Socket outputSocket;
    private final Throttler throttler;
    private final AtomicLong bytes;

    public TransferTask(InputStream input, OutputStream output, Socket outputSocket, Throttler throttler, AtomicLong bytes) {
        this.input = input;
        this.output = output;
        this.outputSocket = outputSocket;
        this.throttler = throttler;
        this.bytes = bytes;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = throttler.newBuffer();
            int len;
            while ((len = input.read(buffer)) >= 0) {
                long wait = throttler.offer(System.currentTimeMillis());
                while (wait > 0) {
                    Thread.sleep(wait);
                    wait = throttler.offer(System.currentTimeMillis());
                }
                output.write(buffer, 0, len);
                bytes.addAndGet(len);
            }
            output.flush();
            outputSocket.shutdownOutput();
        } catch (SocketException e) {
            if (!"Socket closed".equals(e.getMessage()) && !"Socket output is already shutdown".equals(e.getMessage()))
                e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
