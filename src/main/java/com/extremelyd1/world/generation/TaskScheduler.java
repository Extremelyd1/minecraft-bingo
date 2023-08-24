package com.extremelyd1.world.generation;

import java.util.Set;
import java.util.concurrent.*;

public class TaskScheduler {
    private final ExecutorService executor;
    private final Set<Future<?>> futures = ConcurrentHashMap.newKeySet();

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

    public void runTask(final Runnable runnable) {
        futures.add(executor.submit(runnable));
        futures.removeIf(Future::isDone);
    }

    public void cancelTasks() {
        for (Future<?> future : futures) {
            future.cancel(true);
        }
        futures.clear();
    }
}
