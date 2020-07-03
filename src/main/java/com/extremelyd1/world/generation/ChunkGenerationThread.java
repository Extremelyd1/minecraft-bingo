package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import com.google.common.collect.Queues;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class ChunkGenerationThread implements Runnable {

    /**
     * The number of ticks between notifications
     */
    private static final int TICKS_PER_NOTIFICATION = 5 * 20;

    /**
     * The pregeneration manager instance
     */
    private final PregenerationManager pregenerationManager;

    /**
     * A queue containing worlds that are pending chunk generation
     */
    private final Queue<PendingGeneration> pendingGenerations;

    /**
     * The number of ticks in between generation cycles
     */
    private final int ticksPerCycle;
    /**
     * The number of chunks to generate per cycle
     */
    private final int chunksPerCycle;

    /**
     * Number of ticks passed
     */
    private long ticked;

    /**
     * The number of chunks left since last check
     */
    private int lastChunksLeft;

    /**
     * The number of nanoseconds since the last call
     */
    private long lastCallNanos;

    /**
     * Total number of chunks for this pending world
     */
    private int numberOfChunks;

    /**
     * Whether the thread is idle and not generating anything
     */
    private boolean idle;

    public ChunkGenerationThread(
            PregenerationManager pregenerationManager,
            int ticksPerCycle,
            int chunksPerCycle
    ) {
        this.pregenerationManager = pregenerationManager;

        this.ticksPerCycle = ticksPerCycle;
        this.chunksPerCycle = chunksPerCycle;

        this.pendingGenerations = Queues.newConcurrentLinkedQueue();

        this.ticked = 0;
        this.lastChunksLeft = 0;
        this.lastCallNanos = 0;

        this.idle = true;
    }

    /**
     * Check whether to send a notification and if so, create and send it
     */
    private void checkNotification() {
        PendingGeneration pendingGeneration = this.pendingGenerations.peek();
        if (pendingGeneration == null) {
            return;
        }

        int chunksLeft = pendingGeneration.getPendingChunks().size();
        // There are no chunks left to generate, no need to send a notification
        if (chunksLeft == 0 && this.lastChunksLeft == 0) {
            return;
        }

        // Calculate chunk progress percentage
        NumberFormat formatter = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.UK));
        double percentage = 100.0 / this.numberOfChunks * (this.numberOfChunks - chunksLeft);

        // Calculate chunks per second
        long currentTime = System.nanoTime();
        double delta = (currentTime - this.lastCallNanos);
        this.lastCallNanos = currentTime;

        // Divide by 1 * 10^9 (nanos)
        delta /= 1000000000;

        double chunksPerSecond = (this.lastChunksLeft - chunksLeft) / delta;
        if (chunksPerSecond < 0) {
            chunksPerSecond = 0;
        }

        String progressMessage = "Chunk gen progress for %s: %d/%d [%s%%, %s chunks/s]";
        progressMessage = String.format(
                progressMessage,
                pendingGeneration.toString(),
                chunksLeft,
                this.numberOfChunks,
                formatter.format(percentage),
                formatter.format(chunksPerSecond)
        );

        Game.getLogger().info(progressMessage);

        this.lastChunksLeft = chunksLeft;
    }

    /**
     * Schedules the given pending world to be generated
     * @param pendingGeneration The pending world to be generated
     */
    public void scheduleWorld(PendingGeneration pendingGeneration) {
        this.pendingGenerations.add(pendingGeneration);

        // If the generation thread was idle, update new number of chunks
        if (this.idle) {
            this.numberOfChunks = pendingGeneration.getPendingChunks().size();

            this.idle = false;
        }
    }

    public void run() {
        if (this.idle) {
            return;
        }

        // Increase tick count
        this.ticked++;

        // Check whether to send a notification
        if (this.ticked % TICKS_PER_NOTIFICATION == 0) {
            checkNotification();
        }

        // Check whether we are generation a new chunk batch
        if (this.ticked % ticksPerCycle != 0) {
            return;
        }

        PendingGeneration pendingGeneration = this.pendingGenerations.peek();
        if (pendingGeneration == null) {
            // No more worlds to generate
            this.idle = true;
            return;
        }

        if (generateAsync(pendingGeneration)) {
            // Check if all chunks have been generated
            if (!pendingGeneration.isGenerated()) {
                return;
            }

            // Remove world from pending generations, because all
            // chunks have been generated
            this.pendingGenerations.poll();

            // Add it to the generated queue for unloading
            this.pregenerationManager.addGeneratedWorld(pendingGeneration);

            // Update number of chunks for new pending world
            pendingGeneration = this.pendingGenerations.peek();
            if (pendingGeneration != null) {
                this.numberOfChunks = pendingGeneration.getPendingChunks().size();
            }
        }
    }

    /**
     * Generate a given number of chunks asynchronously
     * @param pendingGeneration A non-empty pending world
     * @return Whether a pending world is completed
     */
    private boolean generateAsync(PendingGeneration pendingGeneration) {
        Queue<PendingChunk> pendingChunks = pendingGeneration.getPendingChunks();
        if (pendingChunks.peek() == null) {
            return true;
        }

        for (int i = 0; i < chunksPerCycle; i++) {
            CompletableFuture.supplyAsync(pendingChunks::poll)
                    .thenAccept(pending -> {
                        if (pending != null) {
                            pending.generate();
                        }
                    });
        }

        return false;
    }

    /**
     * Stops world generation immediately
     */
    public void stop() {
        // Add all pending worlds to generated queue for unloading
        while (!this.pendingGenerations.isEmpty()) {
            this.pregenerationManager.addGeneratedWorld(
                    this.pendingGenerations.poll()
            );
        }

        // Set this thread to idle
        this.idle = true;
    }

    /**
     * Whether nothing is schedules and the thread is idle
     * @return Whether the thread is idle
     */
    public boolean isIdle() {
        return idle;
    }
}
