package com.manunin.forkjoin.executor;

import com.manunin.forkjoin.business.MaintainedTask;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;

public class ForkJoinRepeatableTask extends ForkJoinTask<Void> implements Runnable {

    private final MaintainedTask maintained;
    private final long iterations;
    private final AtomicLong counter = new AtomicLong(0);

    public ForkJoinRepeatableTask(MaintainedTask task, long iterations) {
        this.maintained = task;
        this.iterations = iterations;
    }

    @Override
    protected boolean exec() {
        maintained.maintain();
        return counter.incrementAndGet() == iterations;
    }

    @Override
    public Void getRawResult() {
        return null;
    }

    @Override
    protected void setRawResult(Void value) {}

    @Override
    public void run() {}
}
