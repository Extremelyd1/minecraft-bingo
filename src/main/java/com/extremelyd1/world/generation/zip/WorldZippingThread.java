package com.extremelyd1.world.generation.zip;

import com.extremelyd1.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class WorldZippingThread implements Runnable {

    /**
     * The pending zip to zip
     */
    private PendingZip currentPendingZip;

    /**
     * Whether this thread is idle
     */
    private boolean idle;

    public WorldZippingThread() {
        this.idle = true;
    }

    /**
     * Schedules this pending zip to be zipped
     * @param pendingZip The pending zip to be zipped
     */
    public void scheduleZip(Plugin plugin, PendingZip pendingZip) {
        if (idle) {
            currentPendingZip = pendingZip;
            idle = false;

            // Schedule it with a task
            Bukkit.getScheduler().runTaskAsynchronously(
                    plugin,
                    this
            );

            Game.getLogger().info("Scheduled zip in zipping thread");
        } else {
            Game.getLogger().info("Zipping thread not idle, cannot schedule zip");
        }
    }

    public void run() {
        if (this.idle) {
            return;
        }

        // Zip the next pending zip
        currentPendingZip.zip();

        Game.getLogger().info("World " + currentPendingZip + " has been zipped, deleting...");

        // Delete the world folder of this pending zip
        currentPendingZip.deleteWorldFolder();

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
