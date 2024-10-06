package com.manunin;

import com.manunin.forkjoin.executor.BlockingQueueRepeatableExecutor;
import com.manunin.forkjoin.business.BusinessTask;
import com.manunin.forkjoin.business.MaintainedTask;
import com.manunin.forkjoin.executor.ForkJoinRepeatableExecutor;
import com.manunin.forkjoin.executor.RepeatableExecutor;
import com.manunin.forkjoin.executor.SemaphoreRepeatableExecutor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

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
        int p2_iterations;

        @Param({"FORK_JOIN", "BLOCKING_QUEUE", "SEMAPHORE"})
        String p3_executor;

        @Param({"1", "10"})
        int p1_businessTaskDurationInMillis;
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
        MaintainedTask businessTask = new BusinessTask(plan.p1_businessTaskDurationInMillis);
        RepeatableExecutor executor = RepeatableExecutors.valueOf(plan.p3_executor).getExecutor(plan.p2_iterations, businessTask);
        IntStream.range(0, plan.p2_iterations).forEach(i -> executor.run());
        executor.waitForFinish();
    }
}
