package com.sb.toolkit;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleTimerWheel {

    private TimerRing timerRing;
    private ThreadFactory threadFactory;
    private Worker worker = new Worker();

    /**
     * maxInterval in seconds
     *
     * @param maxInterval
     */
    public SimpleTimerWheel(int maxInterval, ThreadFactory threadFactory) {
        this.timerRing = new TimerRing(maxInterval);
        this.threadFactory.newThread(worker);
    }

    public void addTimer(int expiry, Consumer<Boolean> consumer) {
        this.timerRing.addTimer(expiry, consumer);
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                timerRing.tick();
            } catch (InterruptedException ex) {
                //todo
            }
        }
    }


    public class TimerRing {

        private int tickPos = 0;
        private ArrayList<Consumer<Boolean>>[] buffer;

        public TimerRing(int capacity) {
            this.buffer = new ArrayList[capacity];
        }

        private int wrapPos(int pos) {
            if (pos > this.buffer.length-1) {
                return (this.buffer.length-1 - pos) -1;
            }
            return pos;
        }

        public void addTimer(int expiry, Consumer<Boolean> consumer) {
            int pos = wrapPos(this.tickPos+expiry);
            if (this.buffer[pos] == null) {
                this.buffer[pos] = new ArrayList<>();
            }
            this.buffer[pos].add(consumer);
        }

        public void tick() {
            this.tickPos = wrapPos(tickPos++);
            if (this.buffer[this.tickPos] != null) {
                this.buffer[this.tickPos].forEach(c -> c.accept(true));
            }
        }
    }
}