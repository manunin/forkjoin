package org.manunin.forkjoin.executor;

import org.manunin.forkjoin.business.MaintainedTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class ForkJoinRepeatableExecutor implements RepeatableExecutor{

    private final ForkJoinSchedulerTask task;
    private final ExecutorService executorService = ForkJoinPool.commonPool();

    public ForkJoinRepeatableExecutor(MaintainedTask task, long iterations) {
        this.task = new ForkJoinSchedulerTask(task, iterations);
    }

    @Override
    public void run() {
        executorService.execute(task);
    }

    @Override
    public void waitForFinish() {
        task.join();
    }
}
