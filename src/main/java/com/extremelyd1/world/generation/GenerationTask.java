package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.Pair;
import org.bukkit.World;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that represents the chunk generation of a specific world.
 */
public class GenerationTask implements Runnable {
    /**
     * The maximum number of permits for the semaphore that controls chunk generation.
     */
    private static final int MAX_PERMITS = 50;

    /**
     * The start time of the generation.
     */
    private final AtomicLong startTime = new AtomicLong();
    /**
     * The time to keep track of updates to log progress.
     */
    private final AtomicLong updateTime = new AtomicLong();

    /**
     * The number of chunks that have finished chunks.
     */
    private final AtomicLong finishedChunks = new AtomicLong();
    /**
     * Deque that stores samples of updates to calculate the chunk generation rate.
     */
    private final Deque<Pair<Long, AtomicLong>> updateSamples = new ConcurrentLinkedDeque<>();

    /**
     * The world to generate chunks for.
     */
    private final World world;
    /**
     * The runnable to run when the world is fully generated.
     */
    private final Runnable completedAction;
    /**
     * The chunk iterator for all the chunks to generate.
     */
    private final ChunkIterator chunkIterator;
    /**
     * The progress instance that keeps track of the progress of chunk generation.
     */
    private final Progress progress;
    /**
     * Whether this generation task has stopped.
     */
    private boolean stopped;
    /**
     * Whether this generation task has completed.
     */
    private boolean completed;

    /**
     * Constructs the generation task with the given world and the runnable.
     *
     * @param world           The world to generate chunks in.
     * @param completedAction The runnable to run when this task completes.
     */
    public GenerationTask(World world, Runnable completedAction) {
        this.world = world;
        this.completedAction = completedAction;
        this.chunkIterator = new ChunkIterator(world.getWorldBorder());
        this.progress = new Progress(world.getName(), chunkIterator.total());
    }

    /**
     * Update the progress of the task given that a new chunk has generated.
     */
    private synchronized void update() {
        if (stopped) {
            return;
        }

        progress.chunkCount = finishedChunks.addAndGet(1);
        progress.percentComplete = 100f * progress.chunkCount / chunkIterator.total();

        final long currentTime = System.currentTimeMillis();
        final Pair<Long, AtomicLong> bin = updateSamples.peekLast();
        if (bin != null && currentTime - bin.left() < 2e3) {
            bin.right().addAndGet(1);
        } else if (updateSamples.add(Pair.of(currentTime, new AtomicLong(1)))) {
            while (!updateSamples.isEmpty() && currentTime - updateSamples.peek().left() > 1e4) {
                updateSamples.poll();
            }
        }

        final Pair<Long, AtomicLong> oldest = updateSamples.peek();
        final long oldestTime = oldest == null ? currentTime : oldest.left();
        final long chunksLeft = chunkIterator.total() - finishedChunks.get();
        final double timeDiff = (currentTime - oldestTime) / 1e3;
        if (chunksLeft > 0 && timeDiff < 1e-1) {
            return;
        }

        long sampleCount = 0;
        for (Pair<Long, AtomicLong> b : updateSamples) {
            sampleCount += b.right().get();
        }
        progress.rate = timeDiff > 0 ? sampleCount / timeDiff : 0;
        final long time;
        if (chunksLeft == 0) {
            time = (currentTime - startTime.get()) / 1000;
            progress.complete = true;
        } else {
            time = (long) (chunksLeft / progress.rate);
        }
        progress.hours = time / 3600;
        progress.minutes = (time - progress.hours * 3600) / 60;
        progress.seconds = time - progress.hours * 3600 - progress.minutes * 60;

        if (progress.complete) {
            progress.sendUpdate();
            return;
        }
        final boolean updateIntervalElapsed = ((currentTime - updateTime.get()) / 1e3) > 2;
        if (updateIntervalElapsed) {
            progress.sendUpdate();
            updateTime.set(currentTime);
        }
    }

    @Override
    public void run() {
        final String poolThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(String.format("BingoGen-%s Thread", world.getName()));
        final Semaphore working = new Semaphore(MAX_PERMITS);
        startTime.set(System.currentTimeMillis());
        while (!stopped && chunkIterator.hasNext()) {
            final ChunkCoordinate chunk = chunkIterator.next();

            try {
                working.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stop(completed);
                break;
            }
            final CompletableFuture<Boolean> isChunkGenerated = CompletableFuture.supplyAsync(() -> {
                try {
                    return world.isChunkGenerated(chunk.x(), chunk.z());
                } catch (CompletionException e) {
                    return false;
                }
            });
            isChunkGenerated
                    .thenCompose(generated -> {
                        if (Boolean.TRUE.equals(generated)) {
                            return CompletableFuture.completedFuture(null);
                        } else {
                            return world.getChunkAtAsync(chunk.x(), chunk.z(), true);
                        }
                    }).whenComplete((ignored, throwable) -> {
                        update();
                        working.release();
                    });
        }
        if (stopped) {
            Game.getLogger().info(String.format("Task stopped for %s.", world.getName()));
        } else {
            completed = true;
        }
        Thread.currentThread().setName(poolThreadName);

        try {
            working.acquire(MAX_PERMITS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            completedAction.run();
        }
    }

    /**
     * Stops this generation task.
     *
     * @param completed Whether the task was completed.
     */
    public void stop(final boolean completed) {
        this.stopped = true;
        this.completed = completed;
    }

    /**
     * Class that tracks progress of generation.
     */
    public static final class Progress {
        /**
         * The name of the world for the progress message.
         */
        private final String world;
        /**
         * The total number of chunks that need to be generated.
         */
        private final long totalChunks;
        /**
         * The current number of chunks that have been generated.
         */
        private long chunkCount;
        /**
         * Whether the generation is complete.
         */
        private boolean complete;
        /**
         * The percentage of completion for the generation.
         */
        private float percentComplete;

        /**
         * The number of hours that the generation has been in progress.
         */
        private long hours;
        /**
         * The number of minutes (modulo hours) that the generation has been in progress.
         */
        private long minutes;
        /**
         * The number of seconds (modulo minutes) that the generation has been in progress.
         */
        private long seconds;

        /**
         * The current rate in chunks of generation.
         */
        private double rate;

        /**
         * Constructs the progress instance with the given world and total number of chunks.
         *
         * @param world       The name of the world.
         * @param totalChunks The total number of chunks.
         */
        private Progress(final String world, final long totalChunks) {
            this.world = world;
            this.totalChunks = totalChunks;
        }

        public String getWorld() {
            return world;
        }

        /**
         * Sends an update to the logger with the progress of the generation.
         */
        public void sendUpdate() {
            if (complete) {
                Game.getLogger().info(String.format(
                        "Generation finished for %s: processed %s chunks [%.2f], Total time: %01d:%02d:%02d",
                        world,
                        chunkCount,
                        percentComplete,
                        hours,
                        minutes,
                        seconds
                ));
            } else {
                Game.getLogger().info(String.format(
                        "Generation progress for %s: %s/%s [%.2f%%, %.2f chunks/s], ETA: %01d:%02d:%02d",
                        world,
                        chunkCount,
                        totalChunks,
                        percentComplete,
                        rate,
                        hours,
                        minutes,
                        seconds
                ));
            }
        }
    }
}
