package com.manunin.forkjoin.business;

public class BusinessTask implements MaintainedTask {

    private final int workingTime;

    public BusinessTask(int workingTime) {
        this.workingTime = workingTime;
    }

    @Override
    public void maintain() {
        try {
            Thread.sleep(workingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
