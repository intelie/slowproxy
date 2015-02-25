package net.intelie.slowproxy;

import java.io.Serializable;

public class Throttler implements Serializable {
    private final long[] queue;
    private final int maxCount;
    private final long period;
    private final int bufferSize;
    private final int maxBytes;
    private int begin = 0, count = 0;

    public Throttler(int maxBytes, long period) {
        this.maxBytes = maxBytes;
        this.bufferSize = maxBytes > 0 ? Math.max(maxBytes / 1024, 1) : 1024;
        this.maxCount = Math.max(maxBytes / bufferSize, 1);
        this.period = period;
        this.queue = new long[maxCount];
    }

    public long offer(long ts) {
        if (maxBytes < 0) return 0;
        if (maxBytes == 0) return period;
        return syncOffer(ts);
    }

    public byte[] newBuffer() {
        return new byte[bufferSize];
    }

    private void update(long now) {
        while (count > 0 && queue[begin] <= now) {
            begin = (begin + 1) % maxCount;
            count--;
        }
    }

    private synchronized long syncOffer(long ts) {
        update(ts);
        if (count >= maxCount)
            return queue[begin] - ts;
        queue[(begin + count++) % maxCount] = ts + period;
        return 0;
    }
}
