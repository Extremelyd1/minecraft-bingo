package com.extremelyd1.world;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a means of loading chunks and executing a method
 * once those chunks finish loading
 */
public class ChunkLoader implements Listener {

    private final JavaPlugin plugin;
    private final World world;
    private final List<Location> locations;
    private final Runnable onLoad;

    private List<Chunk> toBeLoadedChunks;

    public ChunkLoader(JavaPlugin plugin, WorldManager worldManager, List<Location> locations, Runnable onLoad) {
        this.plugin = plugin;
        this.world = worldManager.getWorld();
        this.locations = locations;
        this.onLoad = onLoad;
    }

    /**
     * Start loading the chunks, and if finished execute the on-load method
     */
    public void start() {
        toBeLoadedChunks = new ArrayList<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        for (Location location : locations) {
            Chunk chunk = world.getChunkAt(location);
            if (!chunk.isLoaded()) {
                // Load the chunk and wait add to list for waiting on its load
                toBeLoadedChunks.add(chunk);
                chunk.load();
            }
            // Keep chunk loaded until starting is finished
            chunk.setForceLoaded(true);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        Chunk chunk = e.getChunk();

        toBeLoadedChunks.remove(chunk);

        // If we have loaded all chunks that needed to be loaded
        if (toBeLoadedChunks.isEmpty()) {
            // Run on-load method
            onLoad.run();

            // Unregister this event listener
            HandlerList.unregisterAll(this);

            // Release all force loaded chunks
            for (Location location : locations) {
                world.getChunkAt(location).setForceLoaded(false);
            }
        }
    }
}
