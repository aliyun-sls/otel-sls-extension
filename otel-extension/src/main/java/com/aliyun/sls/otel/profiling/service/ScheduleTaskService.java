package com.aliyun.sls.otel.profiling.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ScheduleTaskService is the main class for schedule task, it provides the schedule task.
 */
public enum ScheduleTaskService {
    INSTANCE;


    /**
     * executor for clean timeout profiling trace
     */
    private final ScheduledExecutorService executors;

    ScheduleTaskService() {
        this.executors = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("profiling-service");
            thread.setDaemon(true);
            return thread;
        });
    }


    public ScheduledFuture<?> submitJob(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.executors.scheduleAtFixedRate(runnable, delay, period, timeUnit);
    }

}
