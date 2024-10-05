package org.manunin.forkjoin.executor;

import org.manunin.forkjoin.business.MaintainedTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingQueueRepeatableExecutor implements RepeatableExecutor {

    private final MaintainedTask task;
    private final BlockingQueue<Boolean> queue;
    private final AtomicInteger counter;
    private Thread queueReader;

    public BlockingQueueRepeatableExecutor(MaintainedTask task, int iterations) {
        this.task = task;
        this.queue = new LinkedBlockingQueue<>(iterations);
        this.counter = new AtomicInteger(iterations);
        runQueueReader();
    }

    private void runQueueReader() {
        queueReader = new Thread(() -> {
            while (counter.getAndAdd(-1) > 0) {
                try {
                    queue.take();
                    task.maintain();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        queueReader.start();
    }

    @Override
    public void run() {
        try {
            queue.put(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void waitForFinish() {
        try {
            queueReader.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
