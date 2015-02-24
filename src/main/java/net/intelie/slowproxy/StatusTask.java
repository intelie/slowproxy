package net.intelie.slowproxy;

import java.util.concurrent.atomic.AtomicLong;

public class StatusTask implements Runnable {
    private final AtomicLong totalUpload;
    private final AtomicLong totalDownload;
    private final AtomicLong connected;
    private volatile long lastUpload = 0, lastDownload = 0, lastConnected = -1;
    public static final String[] LEVEL_STRINGS = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

    public StatusTask(AtomicLong totalUpload, AtomicLong totalDownload, AtomicLong connected) {
        this.totalUpload = totalUpload;
        this.totalDownload = totalDownload;
        this.connected = connected;
    }

    @Override
    public void run() {
        try {
            long currentUpload = totalUpload.get(), currentDownload = totalDownload.get(), currentConnected = connected.get();

            if (currentDownload != lastDownload || currentUpload != lastUpload || currentConnected != lastConnected)
                System.out.println(String.format("%s/s up / %s/s down. Total: %s up / %s down / %d connected",
                        formatBytes(currentUpload - lastUpload), formatBytes(currentDownload - lastDownload),
                        formatBytes(currentUpload), formatBytes(currentDownload),
                        currentConnected));
            lastUpload = currentUpload;
            lastDownload = currentDownload;
            lastConnected = currentConnected;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatBytes(double bytes) {
        int level = 0;
        while (bytes > 1000) {
            bytes /= 1024;
            level++;
        }
        return level > 0 ?
                String.format("%.2f%s", bytes, LEVEL_STRINGS[level]) :
                String.format("%.0fB", bytes);
    }
}
