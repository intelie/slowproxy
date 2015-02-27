package net.intelie.slowproxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeedDefinition {
    public static final String[] LEVEL_STRINGS = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
    public static final SpeedDefinition NO_LIMIT = new SpeedDefinition(-1, -1, 0, 0, false);
    private final int maxDownloadBytes;
    private final int maxUploadBytes;
    private final int uploadDelay;
    private final int downloadDelay;
    private final boolean splitUpDown;

    public SpeedDefinition(int maxDownloadBytes, int maxUploadBytes, int downloadDelay, int uploadDelay, boolean splitUpDown) {
        this.maxDownloadBytes = maxDownloadBytes;
        this.maxUploadBytes = maxUploadBytes;
        this.uploadDelay = uploadDelay;
        this.downloadDelay = downloadDelay;
        this.splitUpDown = splitUpDown;

        checkBufferSize(makeBufferSize(this.uploadDelay, this.maxUploadBytes), "upload");
        checkBufferSize(makeBufferSize(this.downloadDelay, this.maxDownloadBytes), "download");
    }

    private void checkBufferSize(long size, String type) {
        if (size < 0 || size > 128 * 1024 * 1024)
            throw new IllegalArgumentException("The specified " + type + " speed and delay require too much memory to be simulated: " + formatBytes(size));
    }

    public static SpeedDefinition parse(String input) {
        Pattern pattern = Pattern.compile("(\\d+)([a-z,A-Z]*)(?:\\:(\\d+)([a-z,A-Z]*))?(?:/(\\d+)([a-z,A-Z]*)(?:\\:(\\d+)([a-z,A-Z]*))?)?");
        Matcher matcher = pattern.matcher(input);

        if (!matcher.matches())
            throw new IllegalArgumentException("Invalid speed definition: " + input);

        int downloadBytes = makeSpeed(matcher.group(1), matcher.group(2));
        int downloadDelay = makeDelay(matcher.group(3), matcher.group(4));
        boolean splitUpDown = matcher.group(5) != null;
        int uploadBytes = splitUpDown ? makeSpeed(matcher.group(5), matcher.group(6)) : downloadBytes;
        int uploadDelay = splitUpDown ? makeDelay(matcher.group(7), matcher.group(8)) : downloadDelay;


        return new SpeedDefinition(downloadBytes, uploadBytes, downloadDelay, uploadDelay, splitUpDown);
    }

    private static int makeSpeed(String number, String unit) {
        long value = Integer.parseInt(number);
        if ("bps".equalsIgnoreCase(unit) || "".equals(unit))
            return (int) (value / 8);
        switch (Character.toLowerCase(unit.charAt(0))) {
            case 'g':
                value *= 1024;
            case 'm':
                value *= 1024;
            case 'k':
                value *= 1024;
        }
        if ((value / 8) > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid speed: " + formatBytes(value / 8) + "/s");
        return (int) (value / 8);
    }

    private static int makeDelay(String number, String unit) {
        if (number == null) return 0;
        int value = Integer.parseInt(number);
        if ("ms".equalsIgnoreCase(unit)) return value;
        if ("s".equalsIgnoreCase(unit)) return value * 1000;
        throw new IllegalArgumentException("Invalid delay unit: " + unit);
    }

    public int maxDownloadBytes() {
        return maxDownloadBytes;
    }

    public int maxUploadBytes() {
        return maxUploadBytes;
    }

    public int uploadDelay() {
        return uploadDelay;
    }

    public int downloadDelay() {
        return downloadDelay;
    }

    public boolean splitUpDown() {
        return splitUpDown;
    }

    public static long makeBufferSize(int delay, int bytes) {
        return (delay + 999L) / 1000 * bytes;
    }

    public int uploadBufferSize() {
        return (int) makeBufferSize(uploadDelay, maxUploadBytes);
    }

    public int downloadBufferSize() {
        return (int) makeBufferSize(downloadDelay, maxDownloadBytes);
    }

    public static String formatBytes(double bytes) {
        if (bytes < 0) return "[virtually infinite]";
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
