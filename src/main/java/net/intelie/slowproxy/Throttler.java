package net.intelie.slowproxy;

import java.io.Serializable;

public class Throttler implements Serializable {
    private final long[] queue;
    private final int maxCount;
    private final long period;
    private int begin = 0, count = 0;

    public Throttler(int maxCount, long period) {
        this.maxCount = maxCount;
        this.period = period;
        this.queue = new long[maxCount];
    }

    public void update(long now) {
        while (count > 0 && queue[begin] <= now) {
            begin = (begin + 1) % maxCount;
            count--;
        }
    }

    public long offer(long ts) {
        update(ts);
        if (count >= maxCount)
            return queue[begin] - ts;
        queue[(begin + count++) % maxCount] = ts + period;
        return 0;
    }
}
