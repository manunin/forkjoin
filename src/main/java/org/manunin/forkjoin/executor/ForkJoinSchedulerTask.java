package org.manunin.forkjoin.executor;

import org.manunin.forkjoin.business.MaintainedTask;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;

public class ForkJoinSchedulerTask extends ForkJoinTask<Void> implements Runnable, RepeatableExecutor {

    private final MaintainedTask maintained;
    private final long iterations;

    private final AtomicLong counter = new AtomicLong(0);

    public ForkJoinSchedulerTask(MaintainedTask task, long iterations) {
        this.maintained = task;
        this.iterations = iterations;
    }


    public Void getRawResult() {
        return null;
    }

    protected void setRawResult(Void value) {
    }

    @Override
    protected boolean exec() {
        maintained.maintain();
        return counter.incrementAndGet() == iterations;
    }

    @Override
    public void run() {}

    @Override
    public void waitForFinish() {
        this.join();
    }
}
