package com.manunin.forkjoin.executor;

import com.manunin.forkjoin.business.MaintainedTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class BlockingQueueRepeatableExecutor implements RepeatableExecutor {

    private final MaintainedTask task;
    private final BlockingQueue<Boolean> queue;
    private final AtomicInteger counter;
    private List<Future<Void>> workers = new ArrayList<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public BlockingQueueRepeatableExecutor(MaintainedTask task, int iterations) {
        this.task = task;
        this.queue = new LinkedBlockingQueue<>(iterations);
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
                    Boolean take = queue.take();
                    if (isPoisoned(take)) break;
                    task.maintain();
                } while (counter.decrementAndGet() > 0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            addPoisonPill();
            return null;
        };
    }

    private boolean isPoisoned(Boolean take) {
        return !take;
    }

    private void addPoisonPill() {
        queue.add(false);
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
        workers.forEach((worker) -> {
            try {
                worker.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.shutdown();
    }
}
