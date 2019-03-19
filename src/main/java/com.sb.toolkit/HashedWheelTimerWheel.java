package com.sb.toolkit;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

/**
 * A hashed timer wheel can work with a larger maxInterval
 * compared to the simple timer wheel
 *
 * A timer can hash into j % slots . Maintain an order list of timers
 * per slot
 *
 */
public class HashedWheelTimerWheel {

    private TimerWheel timerWheel;
    private ThreadFactory threadFactory;
    private Worker worker = new Worker();

    public HashedWheelTimerWheel(ThreadFactory tf) {
        this.timerWheel = new TimerWheel();
        this.threadFactory.newThread(worker);
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                timerWheel.tick();
            } catch (InterruptedException ex) {
                //todo
            }
        }
    }

    /**
     * A timer wheel containing 1024 slots, as powers of 2 allows for
     * better hash collisions
     */
    public class TimerWheel {

        private int MAX_CAPACITY = 1024;
        private SortedMap<Integer, Consumer<Boolean>>[] timers;
        private int currentTick;

        public TimerWheel() {
            currentTick = -1;
            timers = new SortedMap[MAX_CAPACITY];
        }

        public void addTimer(int expiry, Consumer<Boolean> consumer) {
            int slot = 0, offset = 0;
            if (expiry > MAX_CAPACITY) {
                slot = expiry / MAX_CAPACITY;
                offset = expiry % MAX_CAPACITY;
            } else {
                slot = expiry;
            }
            if (timers[slot] == null) {
                timers[slot] = new TreeMap<>((a, b) -> (a <= b ? a : b));
            }
            timers[slot].put(offset, consumer);
        }

        private int wrapTick(int currentTick, int delta) {
            int tick = currentTick + delta;
            if (tick < MAX_CAPACITY) {
                return tick;
            } else {
                return (MAX_CAPACITY - tick - 1);
            }
        }

        public void tick() {
            currentTick = wrapTick(currentTick, 1);
            if (this.timers[currentTick] != null) {
                SortedMap<Integer, Consumer<Boolean>> newMap = new TreeMap<>();
                this.timers[currentTick].forEach((i, c) -> {
                    if (i == 0) {
                        c.accept(true);
                    } else {
                        newMap.put(i-1, c);
                    }
                });
                this.timers[currentTick] = newMap;
            }
        }
    }
}