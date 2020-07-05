package com.extremelyd1.world.spawn;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.LocationUtil;
import org.bukkit.Location;

public class SpawnFindThread extends Thread {

    /**
     * The spiral used to find a valid spawn
     */
    private final Spiral spiral;
    /**
     * The max width of the spiral after which the search ends
     */
    private final int maxSearchWidth;

    /**
     * Whether the search is done
     */
    private boolean done;
    /**
     * The location found in the search
     */
    private Location foundLocation;

    public SpawnFindThread(Location location, int maxSearchWidth) {
        // Create spiral to find valid spawn
        this.spiral = new Spiral(location);
        this.maxSearchWidth = maxSearchWidth;

        // If start location is already valid, return it
        if (LocationUtil.isValidSpawnLocation(location)) {
            this.foundLocation = location;
            this.done = true;
        } else {
            this.done = false;
        }
    }

    @Override
    public void run() {
        if (this.done) {
            return;
        }

        // Stop searching when we reach max search width
        if (this.spiral.getCurrentWidth() >= this.maxSearchWidth) {
            Game.getLogger().info(
                    "Stopping searching for spawn after spiral width of "
                            + this.maxSearchWidth
            );

            this.foundLocation = spiral.getCenter();
            this.done = true;
        }

        // Advance the spiral a step and check new location
        Location newLocation = spiral.step();
        if (LocationUtil.containsValidSpawnLocation(
                newLocation.getWorld(),
                newLocation.getBlockX(),
                newLocation.getBlockZ()
        )) {
            this.foundLocation = spiral.getCenter();
            this.done = true;
        }
    }

    public boolean isDone() {
        return done;
    }

    public Location getFoundLocation() {
        return foundLocation;
    }
}
