package net.intelie.throttler;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws Exception {

        int maxKBps, localPort, remotePort;
        String remoteHost;

        try {
            maxKBps = Integer.parseInt(args[0]);
            localPort = Integer.parseInt(args[1]);
            remoteHost = args[2];
            remotePort = Integer.parseInt(args[3]);
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
            System.out.println("usage: throttler <kbytes/second> <local port> <remote host> <remote port>");
            System.out.println("example: throttler 56 1234 somehost 80");
            System.exit(1);
            return;
        }


        ExecutorService service = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

        ServerSocket server = new ServerSocket(localPort);
        System.out.println("Listening at " + localPort);

        while (true) {
            final Socket localSocket = server.accept();
            try {
                final Socket remoteSocket = new Socket(remoteHost, remotePort);

                System.out.println("Accepted from: " + localSocket.getRemoteSocketAddress());
                final TransferTask upload = new TransferTask(localSocket.getInputStream(), remoteSocket.getOutputStream(), maxKBps);
                final TransferTask download = new TransferTask(remoteSocket.getInputStream(), localSocket.getOutputStream(), maxKBps);
                service.submit(upload);
                service.submit(download);
                service.submit(new ProgressTask(upload, download, localSocket));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static class TransferTask implements Runnable {
        private final InputStream input;
        private final OutputStream output;
        private final int maxKBps;
        private long bytes = 0;
        private boolean running = true;

        public TransferTask(InputStream input, OutputStream output, int maxKBps) {
            this.input = input;
            this.output = output;
            this.maxKBps = maxKBps;
        }

        public long getBytes() {
            return bytes;
        }

        public boolean isRunning() {
            return running;
        }

        public void close() {
            try {
                if (running) {
                    output.close();
                    running = false;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Throttler throttler = new Throttler(maxKBps, 1000);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = input.read(buffer)) >= 0) {
                    long wait = throttler.offer(System.currentTimeMillis());
                    if (wait > 0)
                        Thread.sleep(wait);
                    output.write(buffer, 0, len);
                    bytes += len;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }
    }

    private static class ProgressTask implements Runnable {
        private final TransferTask upload;
        private final TransferTask download;
        private final Socket localSocket;

        public ProgressTask(TransferTask upload, TransferTask download, Socket localSocket) {
            this.upload = upload;
            this.download = download;
            this.localSocket = localSocket;
        }

        @Override
        public void run() {
            long lastUpload = 0, lastDownload = 0;
            SocketAddress localAddress = localSocket.getRemoteSocketAddress();

            while (upload.isRunning() && download.isRunning()) {
                try {
                    long currentUpload = upload.getBytes(), currentDownload = download.getBytes();


                    System.out.println(String.format("%s: %.2fKB/s up / %.2fKB/s down. Total: %.2fKB up / %.2fKB down",
                            localAddress,
                            (currentUpload - lastUpload) / 1024.0, (currentDownload - lastDownload) / 1024.0,
                            currentUpload / 1024.0, currentDownload / 1024.0));
                    lastUpload = currentUpload;
                    lastDownload = currentDownload;
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            upload.close();
            download.close();
            System.out.println("Closed socket from " + localAddress);
        }
    }
}
