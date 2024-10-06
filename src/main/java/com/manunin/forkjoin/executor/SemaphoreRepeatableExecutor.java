package com.manunin.forkjoin.executor;

import com.manunin.forkjoin.business.MaintainedTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class SemaphoreRepeatableExecutor implements RepeatableExecutor{
    private final Semaphore availableTasks = new Semaphore(0);
    private final Queue<Boolean> queue;
    private final AtomicInteger counter;
    private final MaintainedTask task;
    private final List<Future<Void>> workers = new ArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public SemaphoreRepeatableExecutor(MaintainedTask task, int iterations) {
        this.task = task;
        this.queue = new ConcurrentLinkedQueue<>();
        this.counter = new AtomicInteger(iterations);
        runAndCollectQueueReadingWorkers();
    }

    private void runAndCollectQueueReadingWorkers() {
        IntStream.range(0, 8).forEach((i) -> workers.add(executorService.submit(createWorker())));
    }

    private Callable<Void> createWorker() {
        return () -> {
            try {
                do {
                    if (availableTasks.tryAcquire(1, TimeUnit.MILLISECONDS)) {
                        queue.poll();
                        task.maintain();
                        counter.decrementAndGet();
                    }
                } while (counter.get() > 0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        };
    }

    @Override
    public void run() {
        queue.add(true);
        availableTasks.release();
    }

    @Override
    public void waitForFinish() {
        workers.forEach((worker) -> {
            try {
                worker.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }
        });
        executorService.shutdown();
    }
}
