package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.PregenerationManager;
import com.google.common.collect.Queues;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class ChunkGenerationThread implements Runnable {

    private static final int TICKS_PER_CYCLE = 20;
    private static final int CHUNKS_PER_CYCLE = 50;
    private static final int TICKS_PER_NOTIFICATION = 5 * 20;

    private final PregenerationManager pregenerationManager;

    private final Queue<PendingWorld> pendingGenerations;

    private long ticked;

    private int lastSchedulesLeft;

    private long lastCallNanos;

    private int numberOfChunks;

    private boolean idle;

    public ChunkGenerationThread(PregenerationManager pregenerationManager) {
        this.pregenerationManager = pregenerationManager;

        this.pendingGenerations = Queues.newConcurrentLinkedQueue();

        this.ticked = 0;
        this.lastSchedulesLeft = 0;
        this.lastCallNanos = 0;

        this.idle = true;
    }

    private double percentDone(int schedulesLeft) {
        return 100.0D / this.numberOfChunks * (this.numberOfChunks - schedulesLeft);
    }

    private void checkNotification() {
        int schedulesLeft = this.pendingGenerations.size();
        // There are no schedules left, no need to send a nofitication
        if (schedulesLeft == 0 && this.lastSchedulesLeft == 0) {
            return;
        }

        NumberFormat formatter = new DecimalFormat("#0.00");
        String percent = "   [" + formatter.format(percentDone(schedulesLeft)) + "% Done]";
        String scheduleMessage = "Pending chunk generations: " + schedulesLeft + "/" + this.numberOfChunks + percent;

        long currentTime = System.nanoTime();
        double delta = (currentTime - this.lastCallNanos);
        this.lastCallNanos = currentTime;

        // Divide by 1 * 10^9 (nanos)
        delta /= 1000000000;

        double chunksPerSecond = (this.lastSchedulesLeft - schedulesLeft) / delta;
        chunksPerSecond = (int)(chunksPerSecond * 100.0D) / 100.0D;
        String generationMessage = "Generated " + chunksPerSecond + " chunks per second";
        if (chunksPerSecond < 0.1) {
            generationMessage = "Low generation cycle";
        }

        Game.getLogger().info(scheduleMessage);
        Game.getLogger().info(generationMessage);

        this.lastSchedulesLeft = schedulesLeft;
    }

    /**
     * Schedules the given pending world to be generated
     * @param pendingWorld The pending world to be generated
     */
    public void scheduleWorld(PendingWorld pendingWorld) {
        this.pendingGenerations.add(pendingWorld);

        // If the generation thread was idle, update new number of chunks
        if (this.idle) {
            this.numberOfChunks = pendingWorld.getPendingChunks().size();

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
        if (this.ticked % TICKS_PER_CYCLE != 0) {
            return;
        }

        PendingWorld pendingWorld = this.pendingGenerations.peek();
        if (pendingWorld == null) {
            // No more worlds to generate
            this.idle = true;
            return;
        }

        if (generateAsync(pendingWorld)) {
            // Remove world from pending generations, because all
            // chunks have been generated
            this.pendingGenerations.poll();

            // Add it to the generated queue for unloading
            this.pregenerationManager.addGeneratedWorld(pendingWorld);

            // Update number of chunks for new pending world
            pendingWorld = this.pendingGenerations.peek();
            if (pendingWorld != null) {
                this.numberOfChunks = pendingWorld.getPendingChunks().size();
            }
        }
    }

    /**
     * Generate a given number of chunks asynchronously
     * @param pendingWorld A non-empty pending world
     * @return Whether a pending world is completed
     */
    private boolean generateAsync(PendingWorld pendingWorld) {
        Queue<PendingChunk> pendingChunks = pendingWorld.getPendingChunks();
        if (pendingChunks.peek() == null) {
            return true;
        }

        for (int i = 0; i < CHUNKS_PER_CYCLE; i++) {
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
     * Whether nothing is schedules and the thread is idle
     * @return Whether the thread is idle
     */
    public boolean isIdle() {
        return idle;
    }
}
