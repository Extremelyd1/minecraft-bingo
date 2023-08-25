package com.extremelyd1.world.generation;

import java.util.Set;
import java.util.concurrent.*;

/**
 * Task scheduler that manages a thread pool to schedule tasks.
 */
public class TaskScheduler {
    /**
     * The executor service for submitting runnables.
     */
    private final ExecutorService executor;

    /**
     * Set of futures of tasks that have been submitted.
     */
    private final Set<Future<?>> futures = ConcurrentHashMap.newKeySet();

    /**
     * Construct the task scheduler.
     */
    public TaskScheduler() {
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, Integer.MAX_VALUE, 5, TimeUnit.MINUTES, new SynchronousQueue<>());
        threadPoolExecutor.setThreadFactory(runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        threadPoolExecutor.prestartAllCoreThreads();
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        this.executor = threadPoolExecutor;
    }

    /**
     * Schedules a runnable to be executed on a thread.
     *
     * @param runnable The runnable to run on a thread.
     */
    public void runTask(final Runnable runnable) {
        futures.add(executor.submit(runnable));
        futures.removeIf(Future::isDone);
    }

    /**
     * Cancels all scheduled tasks.
     */
    public void cancelTasks() {
        for (Future<?> future : futures) {
            future.cancel(true);
        }
        futures.clear();
    }
}
