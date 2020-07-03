package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PendingGeneration extends PendingWorld {

    /**
     * The index of this world
     */
    private final int index;
    /**
     * The type of environment of this world
     */
    private final World.Environment environment;
    /**
     * A queue containing chunks that are pending generation
     * A chunk will be polled from this queue if it is being generated
     */
    private final Queue<PendingChunk> pendingChunks;
    /**
     * An arraylist of pending chunks
     * This arraylist will not be changed
     */
    private final ArrayList<PendingChunk> chunks;

    /**
     * The world instance
     */
    private World world;

    public PendingGeneration(int index, World.Environment environment) {
        this.index = index;
        this.environment = environment;

        this.pendingChunks = new ConcurrentLinkedQueue<>();
        this.chunks = new ArrayList<>();
    }

    public int getIndex() {
        return index;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    /**
     * Adds a pending chunk to this world
     * @param pendingChunk The chunk to be added
     */
    public void add(PendingChunk pendingChunk) {
        this.pendingChunks.add(pendingChunk);
        this.chunks.add(pendingChunk);
    }

    public Queue<PendingChunk> getPendingChunks() {
        return pendingChunks;
    }

    /**
     * Returns whether all chunks in this pending world have been generated
     * @return True if all chunks have been generation
     */
    public boolean isGenerated() {
        int notGenerated = 0;
        for (PendingChunk pendingChunk : this.chunks) {
            if (!pendingChunk.isGenerated()) {
                notGenerated++;
            }
        }

        if (notGenerated > 0) {
            Game.getLogger().info(notGenerated + " chunks are not generated yet");
            Game.getLogger().info("If you see this message, the server cannot keep up with the chunk generation");
            Game.getLogger().info("So you might want to tweak the chunk generation config values");
            return false;
        }

        return true;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
        this.worldFolder = world.getWorldFolder();
    }

    public String toString() {
        return environment.toString() + index;
    }
}
