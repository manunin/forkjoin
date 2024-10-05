package org.manunin.forkjoin.executor;

public interface RepeatableExecutor {
    void run();
    void waitForFinish();
}
