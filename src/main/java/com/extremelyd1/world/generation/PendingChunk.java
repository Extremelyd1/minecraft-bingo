package com.extremelyd1.world.generation;

import io.papermc.lib.PaperLib;
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
        if (world.isChunkGenerated(x, z)) {
            isGenerated = true;
            return;
        }

        // Get the chunk asynchronously via PaperLib to ensure that it also works with Spigot
        // And after it is generated, unload the chunk and mark it as generated
        PaperLib.getChunkAtAsync(world, x, z, true, true).thenAccept(chunk -> {
            chunk.unload(true);

            isGenerated = true;
        });
    }

    public boolean isGenerated() {
        return isGenerated;
    }
}
