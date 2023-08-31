package com.extremelyd1.world.platform;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

/**
 * Environment class for when we are running the Paper server.
 */
public class PaperEnvironment extends Environment {
    /**
     * Non-public constructor.
     */
    protected PaperEnvironment() {
        super();
    }

    /**
     * Implementation for Paper of {@link Environment#getChunkAtAsync(World, int, int, boolean)}.
     */
    @Override
    public CompletableFuture<Chunk> getChunkAtAsyncInternal(World world, int x, int z, boolean gen) {
        return world.getChunkAtAsync(x, z, gen);
    }
}
