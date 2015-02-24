package net.intelie.slowproxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class ServerTask implements Runnable {
    private final AtomicLong totalUpload = new AtomicLong(0);
    private final AtomicLong totalDownload = new AtomicLong(0);
    private final AtomicLong connected = new AtomicLong(0);

    private final Options options;
    private final ScheduledExecutorService scheduled;
    private final ExecutorService executor;
    private final AtomicInteger currentHost;

    public ServerTask(Options options, ScheduledExecutorService scheduled, ExecutorService executor, AtomicInteger currentHost) {
        this.options = options;
        this.scheduled = scheduled;
        this.executor = executor;
        this.currentHost = currentHost;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = options.local().newServerSocket();
            scheduled.scheduleAtFixedRate(new StatusTask(totalUpload, totalDownload, connected), 1, 1, TimeUnit.SECONDS);

            System.out.println("Listening at " + server.getLocalPort());

            while (true) {
                acceptSingle(server);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void acceptSingle(ServerSocket server) {
        final SpeedDefinition speed = options.speed();
        try {
            final Socket localSocket = server.accept();
            try {
                System.out.println("Accepting: " + localSocket.getRemoteSocketAddress());
                final Socket remoteSocket = options.remote().get(currentHost.get()).newSocket();

                connected.incrementAndGet();
                System.out.println("Accepted. From: " + localSocket.getRemoteSocketAddress() + ". To:  " + remoteSocket.getRemoteSocketAddress() + ".");

                final TransferTask upload = new TransferTask(
                        localSocket.getInputStream(),
                        remoteSocket.getOutputStream(),
                        speed.maxUploadBytes(), totalUpload);
                final TransferTask download = new TransferTask(
                        remoteSocket.getInputStream(),
                        localSocket.getOutputStream(),
                        speed.maxDownloadBytes(), totalDownload);

                final Future<?> uploadFuture = executor.submit(upload);
                final Future<?> downloadFuture = executor.submit(download);
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        Util.getSilently(uploadFuture);
                        Util.getSilently(downloadFuture);
                        Util.silentlyClose(localSocket);
                        Util.silentlyClose(remoteSocket);
                        System.out.println("Closed from: " + localSocket.getRemoteSocketAddress());
                        connected.decrementAndGet();
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
                Util.silentlyClose(localSocket);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
