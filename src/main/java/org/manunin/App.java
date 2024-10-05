package org.manunin;

import org.manunin.forkjoin.business.BusinessTask;
import org.manunin.forkjoin.business.MaintainedTask;
import org.manunin.forkjoin.executor.BlockingQueueRepeatableExecutor;
import org.manunin.forkjoin.executor.ForkJoinRepeatableExecutor;
import org.manunin.forkjoin.executor.RepeatableExecutor;
import org.manunin.forkjoin.executor.SemaphoreRepeatableExecutor;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.stream.IntStream;

public class App {

    private enum RepeatableExecutors {
        FORK_JOIN{
            @Override
            public RepeatableExecutor getExecutor(int interactions, MaintainedTask businessService) {
                return new ForkJoinRepeatableExecutor(businessService, interactions);
            }
        },
        BLOCKING_QUEUE{
            @Override
            public RepeatableExecutor getExecutor(int interactions, MaintainedTask businessService) {
                return new BlockingQueueRepeatableExecutor(businessService, interactions);
            }
        },
        SEMAPHORE{
            @Override
            public RepeatableExecutor getExecutor(int interactions, MaintainedTask businessService) {
                return new SemaphoreRepeatableExecutor(businessService, interactions);
            }
        };
        public abstract RepeatableExecutor getExecutor(int interactions, MaintainedTask businessService);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkExecutionPlan {
        @Param({"1", "10", "100", "1000"})
        int iterations;

        @Param({"FORK_JOIN", "BLOCKING_QUEUE", "SEMAPHORE"})
        String executor;

        @Param({"1", "10"})
        int businessTaskDurationInMillis;
    }

    public static void main( String[] args ) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 5, time = 5)
    @BenchmarkMode(Mode.AverageTime)
    public static void runApp(BenchmarkExecutionPlan plan) {
        MaintainedTask businessTask = new BusinessTask(plan.businessTaskDurationInMillis);
        RepeatableExecutor executor = RepeatableExecutors.valueOf(plan.executor).getExecutor(plan.iterations, businessTask);
        IntStream.range(0, plan.iterations).forEach(i -> executor.run());
        executor.waitForFinish();
    }
}
