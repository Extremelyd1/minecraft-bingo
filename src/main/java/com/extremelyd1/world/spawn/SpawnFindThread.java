package com.extremelyd1.world.spawn;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.World;

public class SpawnFindThread extends Thread {

    /**
     * The location used to start this search with
     */
    private final Location startLocation;

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
        this.startLocation = location;
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
        while (!this.done) {
            // Stop searching when we reach max search width
            if (this.spiral.getCurrentWidth() >= this.maxSearchWidth) {
                Game.getLogger().info(
                        "Stopping search for spawn after spiral width of "
                                + this.maxSearchWidth
                );

                // Default to returning start location if nothing can be found
                this.foundLocation = this.startLocation;
                this.done = true;
            }

            // Advance the spiral a step and check new location
            WorldChunkCoordinate newChunkCoords = this.spiral.step();

            Location centerLocation = newChunkCoords.getCenter();
            if (!LocationUtil.isInsideWorldBorder(centerLocation)) {
                Game.getLogger().info(
                        "Stopping search for spawn after passing world border at "
                                + this.spiral.getNumIterations()
                                + " spiral iterations"
                );

                // Default to returning start location if nothing can be found
                this.foundLocation = this.startLocation;
                this.done = true;
            }

            if (LocationUtil.isValidSpawnBiome(newChunkCoords.getBiome())) {
                Game.getLogger().info(
                        "Spawn location found after "
                                + this.spiral.getNumIterations()
                                + " spiral iterations"
                );

                // Now find a suitable block location inside the found chunk
                int startX = newChunkCoords.getX() * 16;
                int startZ = newChunkCoords.getZ() * 16;
                World world = newChunkCoords.getWorld();

                // Loop through coordinates within chunk
                for (int x = startX; x < startX + 16; x++) {
                    for (int z = startZ; z < startZ + 16; z++) {
                        // Get location
                        Location location = new Location(
                                world,
                                x,
                                world.getHighestBlockYAt(x, z),
                                z
                        );
                        // Check whether this location is a valid spawn
                        if (LocationUtil.isValidSpawnLocation(location)) {
                            Game.getLogger().info("Valid spawn location found within chunk");

                            // Set found location and return
                            this.foundLocation = location;
                            this.done = true;

                            return;
                        }
                    }
                }

                Game.getLogger().info("No valid spawn location found within chunk, using center");

                // In case no valid spawn could be found inside the chunk,
                // simply return the center location of the chunk
                this.foundLocation = centerLocation;
                this.done = true;
            }
        }
    }

    public boolean isDone() {
        return done;
    }

    public Location getFoundLocation() {
        return foundLocation;
    }
}
