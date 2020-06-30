package com.extremelyd1.world.generation;

import org.bukkit.World;

public class PendingChunk {

    private final World world;

    private final int x;
    private final int z;

    public PendingChunk(World world, int x, int z) {
        this.world = world;

        this.x = x;
        this.z = z;
    }

    public void generate() {
        if (!world.isChunkGenerated(x, z)) {
            world.getChunkAt(x, z);
        }
    }

}
