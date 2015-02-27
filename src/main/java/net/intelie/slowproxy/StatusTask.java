package net.intelie.slowproxy;

import java.util.concurrent.atomic.AtomicLong;

import static net.intelie.slowproxy.SpeedDefinition.formatBytes;

public class StatusTask implements Runnable {
    private final AtomicLong totalUpload;
    private final AtomicLong totalDownload;
    private final AtomicLong connected;
    private volatile long lastUpload = 0, lastDownload = 0;

    public StatusTask(AtomicLong totalUpload, AtomicLong totalDownload, AtomicLong connected) {
        this.totalUpload = totalUpload;
        this.totalDownload = totalDownload;
        this.connected = connected;
    }

    @Override
    public void run() {
        try {
            long currentUpload = totalUpload.get(), currentDownload = totalDownload.get(), currentConnected = connected.get();

            if (currentDownload != lastDownload || currentUpload != lastUpload || currentConnected > 0)
                System.out.println(String.format("%s/s up / %s/s down. Total: %s up / %s down / %d connected",
                        formatBytes(currentUpload - lastUpload), formatBytes(currentDownload - lastDownload),
                        formatBytes(currentUpload), formatBytes(currentDownload),
                        currentConnected));
            lastUpload = currentUpload;
            lastDownload = currentDownload;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
