package com.extremelyd1.world.spawn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class WorldChunkCoordinate {

    /**
     * The worlds in which this chunk is
     */
    private final World world;
    /**
     * The x coordinate
     */
    private int x;
    /**
     * The z coordinate
     */
    private int z;

    public WorldChunkCoordinate(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public WorldChunkCoordinate(Location location) {
        this.world = location.getWorld();
        this.x = (int) Math.floor(location.getX() / 16);
        this.z = (int) Math.floor(location.getZ() / 16);
    }

    /**
     * Gets the biome at the center of this chunk coords with the highest block y
     * @return The biome
     */
    public Biome getBiome() {
        return getBiome(
                this.world.getHighestBlockYAt(this.x, this.z)
        );
    }

    /**
     * Gets the biome at the center of this chunk coords with the given y
     * @param y The y value at which to get the biome
     * @return The biome
     */
    public Biome getBiome(int y) {
        Location center = this.getCenter();
        return world.getBiome(
                center.getBlockX(),
                y,
                center.getBlockZ()
        );
    }

    /**
     * Adds the given coordinates to this chunk's coords
     * @param x The x to add
     * @param z The z to add
     */
    public void add(int x, int z) {
        this.x += x;
        this.z += z;
    }

    /**
     * Returns a new instance of chunks coords with the same values
     * @return A new instance of chunks coords
     */
    public WorldChunkCoordinate copy() {
        return new WorldChunkCoordinate(
                this.world,
                this.x,
                this.z
        );
    }

    /**
     * Gets the center locations of this chunk
     * @return The center location of the chunk
     */
    public Location getCenter() {
        int newX = this.x * 16 + 8;
        int newZ = this.z * 16 + 8;
        int newY = this.world.getHighestBlockYAt(newX, newZ);

        return new Location(
                this.world,
                newX,
                newY,
                newZ
        );
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
