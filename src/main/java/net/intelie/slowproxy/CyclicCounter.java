package net.intelie.slowproxy;

import java.util.concurrent.atomic.AtomicInteger;

public class CyclicCounter {
    private final int maxVal;
    private final AtomicInteger ai = new AtomicInteger(0);

    public CyclicCounter(int maxVal) {
        this.maxVal = maxVal;
    }

    public void set(int newVal) {
        ai.set(newVal);
    }

    public int get() {
        return ai.get();
    }

    public int getAndIncrement() {
        int curVal, newVal;
        do {
            curVal = this.ai.get();
            newVal = (curVal + 1) % this.maxVal;
        } while (!this.ai.compareAndSet(curVal, newVal));
        return curVal;
    }
}
