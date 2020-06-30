package com.extremelyd1.world.zip;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.PregenerationManager;
import com.google.common.collect.Queues;

import java.util.Queue;

public class WorldZippingThread implements Runnable {

    private static final int TICKS_PER_CYCLE = 20;

    private final PregenerationManager pregenerationManager;

    private final Queue<PendingZip> pendingZips;

    private long ticked;

    private boolean idle;

    public WorldZippingThread(PregenerationManager pregenerationManager) {
        this.pregenerationManager = pregenerationManager;

        this.pendingZips = Queues.newConcurrentLinkedQueue();

        this.ticked = 0;

        this.idle = true;
    }

    /**
     * Schedules this pending zip to be zipped
     * @param pendingZip The pending zip to be zipped
     */
    public void scheduleZip(PendingZip pendingZip) {
        this.pendingZips.add(pendingZip);

        this.idle = false;
    }

    public void run() {
        if (this.idle) {
            return;
        }

        // Increase tick count
        this.ticked++;

        // Check whether we are zipping a new file
        if (this.ticked % TICKS_PER_CYCLE != 0) {
            return;
        }

        PendingZip pendingZip = this.pendingZips.poll();
        if (pendingZip == null) {
            // No more pending zips to zip
            this.idle = true;
            return;
        }

        Game.getLogger().info("Zipping " + pendingZip);

        // Zip the next pending zip
        pendingZip.zip();

        // Delete the world folder of this pending zip
        pendingZip.deleteWorldFolder();
    }

    /**
     * Whether nothing is schedules and the thread is idle
     * @return Whether the thread is idle
     */
    public boolean isIdle() {
        return idle;
    }
}
