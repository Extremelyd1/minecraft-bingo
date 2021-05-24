package com.extremelyd1.world.generation;

import org.bukkit.World;

public class PendingChunk {

    /**
     * The world this chunk is in
     */
    private final World world;

    /**
     * The x coordinate of the chunk
     */
    private final int x;
    /**
     * The z coordinate of the chunk
     */
    private final int z;

    /**
     * Whether this chunk has been generated
     */
    private boolean isGenerated;

    public PendingChunk(World world, int x, int z) {
        this.world = world;

        this.x = x;
        this.z = z;

        this.isGenerated = false;
    }

    /**
     * Generates this chunk
     */
    public void generate() {
        if (!world.isChunkGenerated(x, z)) {
            world.getChunkAt(x, z);
        }

        isGenerated = true;
    }

    public boolean isGenerated() {
        return isGenerated;
    }
}
