package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.Pair;
import io.papermc.lib.PaperLib;
import org.bukkit.World;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class GenerationTask implements Runnable {
    private static final int MAX_PERMITS = 50;

    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong updateTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    private final Deque<Pair<Long, AtomicLong>> updateSamples = new ConcurrentLinkedDeque<>();
    private final World world;
    private final Runnable completedAction;
    private final ChunkIterator chunkIterator;
    private final Progress progress;
    private boolean stopped, completed;

    public GenerationTask(World world, Runnable completedAction) {
        this.world = world;
        this.completedAction = completedAction;
        this.chunkIterator = new ChunkIterator(world.getWorldBorder());
        this.progress = new Progress(world.getName(), chunkIterator.total());
    }

    private synchronized void update(final int chunkX, final int chunkZ) {
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
        progress.chunkX = chunkX;
        progress.chunkZ = chunkZ;
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
        // TODO: change to config value instead of hardcoded value
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
                            return PaperLib.getChunkAtAsync(world, chunk.x(), chunk.z(), true);
                        }
                    }).whenComplete((ignored, throwable) -> {
                        update(chunk.x(), chunk.z());
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

    public void stop(final boolean cancelled) {
        this.stopped = true;
        this.completed = cancelled;
    }

    public long getCount() {
        return finishedChunks.get();
    }

    public ChunkIterator getChunkIterator() {
        return chunkIterator;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isStoppedOrCompleted() {
        return stopped || completed;
    }

    public long getTotalTime() {
        return startTime.get() > 0 ? System.currentTimeMillis() - startTime.get() : 0;
    }

    public Progress getProgress() {
        return progress;
    }

    @SuppressWarnings("unused")
    public static final class Progress {
        private final String world;
        private final long totalChunks;
        private long chunkCount;
        private boolean complete;
        private float percentComplete;
        private long hours, minutes, seconds;
        private double rate;
        private int chunkX, chunkZ;

        private Progress(final String world, final long totalChunks) {
            this.world = world;
            this.totalChunks = totalChunks;
        }

        public String getWorld() {
            return world;
        }

        public long getChunkCount() {
            return chunkCount;
        }

        public boolean isComplete() {
            return complete;
        }

        public float getPercentComplete() {
            return percentComplete;
        }

        public long getHours() {
            return hours;
        }

        public long getMinutes() {
            return minutes;
        }

        public long getSeconds() {
            return seconds;
        }

        public double getRate() {
            return rate;
        }

        public int getChunkX() {
            return chunkX;
        }

        public int getChunkZ() {
            return chunkZ;
        }

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
