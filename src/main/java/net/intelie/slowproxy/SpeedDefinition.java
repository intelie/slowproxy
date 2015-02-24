package net.intelie.slowproxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeedDefinition {
    private final int maxDownloadBytes;
    private final int maxUploadBytes;

    public SpeedDefinition(int maxDownloadBytes, int maxUploadBytes) {
        this.maxDownloadBytes = maxDownloadBytes;
        this.maxUploadBytes = maxUploadBytes;
    }

    public static SpeedDefinition parse(String input) {
        Pattern pattern = Pattern.compile("@(\\d+)([a-z,A-Z]*)(?:/(\\d+)([a-z,A-Z]*))?");
        Matcher matcher = pattern.matcher(input);

        if (!matcher.matches())
            throw new IllegalArgumentException("Invalid speed definition: " + input);

        int downloadBytes = makeSpeed(matcher.group(1), matcher.group(2));
        int uploadBytes = matcher.group(3) != null ? makeSpeed(matcher.group(3), matcher.group(4)) : downloadBytes;

        return new SpeedDefinition(downloadBytes, uploadBytes);
    }

    private static int makeSpeed(String number, String unit) {
        int value = Integer.parseInt(number);
        if ("bps".equalsIgnoreCase(unit) || "".equals(unit))
            return value / 8;
        switch (Character.toLowerCase(unit.charAt(0))) {
            case 'g':
                value *= 1024;
            case 'm':
                value *= 1024;
            case 'k':
                value *= 1024;
        }
        return value / 8;
    }

    public int maxDownloadBytes() {
        return maxDownloadBytes;
    }

    public int maxUploadBytes() {
        return maxUploadBytes;
    }
}
