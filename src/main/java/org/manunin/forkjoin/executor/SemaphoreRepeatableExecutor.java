package org.manunin.forkjoin.executor;

import org.manunin.forkjoin.business.MaintainedTask;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SemaphoreRepeatableExecutor implements RepeatableExecutor{
    private final Semaphore availableTasks = new Semaphore(0);
    private final Queue<Boolean> queue;
    private Thread queueReader;
    private final AtomicInteger counter;
    private final MaintainedTask task;

    public SemaphoreRepeatableExecutor(MaintainedTask task, int iterations) {
        this.task = task;
        this.queue = new ConcurrentLinkedQueue<>();
        this.counter = new AtomicInteger(iterations);
        runQueueReader();
    }

    private void runQueueReader() {
        queueReader = new Thread(() -> {
            while (counter.getAndAdd(-1) > 0) {
                try {
                    availableTasks.acquire();
                    queue.poll();
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
        queue.add(true);
        availableTasks.release();
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
