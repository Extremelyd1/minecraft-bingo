package com.extremelyd1.world.platform;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

/**
 * Environment class for when we are running the Spigot server.
 */
public class SpigotEnvironment extends Environment {
    /**
     * Non-public constructor.
     */
    protected SpigotEnvironment() {
        super();
    }

    /**
     * Implementation for Spigot of {@link Environment#getChunkAtAsync(World, int, int, boolean)}.
     */
    @Override
    public CompletableFuture<Chunk> getChunkAtAsyncInternal(World world, int x, int z, boolean gen) {
        return CompletableFuture.completedFuture(world.getChunkAt(x, z, gen));
    }
}
