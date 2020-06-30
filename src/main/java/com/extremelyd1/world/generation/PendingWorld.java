package com.extremelyd1.world.generation;

import org.bukkit.World;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PendingWorld {

    private final int index;
    private final World.Environment environment;
    private final Queue<PendingChunk> pendingChunks;

    private World world;
    private File worldFolder;

    public PendingWorld(int index, World.Environment environment) {
        this.index = index;
        this.environment = environment;

        this.pendingChunks = new ConcurrentLinkedQueue<>();
    }

    public int getIndex() {
        return index;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    public Queue<PendingChunk> getPendingChunks() {
        return pendingChunks;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public File getWorldFolder() {
        return worldFolder;
    }

    public void setWorldFolder(File worldFolder) {
        this.worldFolder = worldFolder;
    }

    public String toString() {
        return "PendingWorld[" + index + ", " + environment + "]";
    }
}
