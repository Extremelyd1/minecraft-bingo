package com.extremelyd1.world.platform;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

/**
 * Represents the environment in which the server runs (currently either Spigot or Paper).
 */
public abstract class Environment {
    /**
     * The class name of the paper config class for older versions of paper.
     */
    private static final String PAPER_CONFIG_CLASS_NAME = "com.destroystokyo.paper.PaperConfig";
    /**
     * The class name of the paper config class for newer versions of paper.
     */
    private static final String PAPER_CONFIG_CLASS_NAME2 = "io.papermc.paper.configuration.Configuration";

    /**
     * The static instance of the environment for easy access to methods.
     */
    private static final Environment INSTANCE = initialize();

    /**
     * Non-public constructor.
     */
    protected Environment() {
    }

    /**
     * Initializes the environment instance to be either a SpigotEnvironment or PaperEnvironment based on what we are
     * running on.
     * @return The environment instance corresponding to what we are running.
     */
    private static Environment initialize() {
        if (hasClass(PAPER_CONFIG_CLASS_NAME) || hasClass(PAPER_CONFIG_CLASS_NAME2)) {
            return new PaperEnvironment();
        } else {
            return new SpigotEnvironment();
        }
    }

    /**
     * Whether a class with the given name exists in our runtime.
     * @param className The name of the class to look for.
     * @return True if the class was found, false otherwise.
     */
    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Gets the chunk at the target location, loading it asynchronously if needed.
     * @param world World to load chunk for.
     * @param x X coordinate of the chunk to load.
     * @param z Z coordinate of the chunk to load.
     * @param gen Should the chunk generate or not.
     * @return Future that completes with the chunk, or null if the chunk did not exist and generation was not
     * requested.
     */
    public static CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z, boolean gen) {
        return INSTANCE.getChunkAtAsyncInternal(world, x, z, gen);
    }

    /**
     * Internal version of {@link #getChunkAtAsync(World, int, int, boolean)}.
     */
    public abstract CompletableFuture<Chunk> getChunkAtAsyncInternal(World world, int x, int z, boolean gen);
}
