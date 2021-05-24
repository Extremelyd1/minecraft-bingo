package com.extremelyd1.world.spawn;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Provides a means of finding valid spawns and loading chunks and executing a method
 * once those spawns are found and chunks finish loading
 */
public class SpawnLoader implements Listener {

    /**
     * The maximum width of a search spiral before cancelling the search
     * Only used when the world border is disable
     */
    private static final int MAX_SEARCH_WIDTH = 200;

    /**
     * The game instance
     */
    private final Game game;
    /**
     * The world where these locations need to be loaded
     */
    private final World world;
    /**
     * The locations for which to load valid spawns
     */
    private final List<Location> locations;
    /**
     * The callback runnable
     */
    private final Runnable onLoad;

    /**
     * Whether chunks are loaded and the callback is executed
     */
    private boolean callbackExecuted;

    /**
     * A map containing all threads that are finding spawns
     */
    private Map<Integer, SpawnFindThread> findThreads;
    /**
     * A list of chunks that need to be loaded
     */
    private List<Chunk> toBeLoadedChunks;

    /**
     * The bukkit task that checks the asynchronous tasks synchronously
     */
    private BukkitTask threadCheckTask;

    public SpawnLoader(
            Game game,
            WorldManager worldManager,
            List<Location> locations,
            Runnable onLoad
    ) {
        this.game = game;
        this.world = worldManager.getWorld();
        this.locations = locations;
        this.onLoad = onLoad;

        this.callbackExecuted = false;
    }

    /**
     * Start finding spawns, then loading the chunks, and if finished execute the on-load method
     */
    public void start() {
        if (game.getConfig().isPreventWaterSpawns()) {
            Game.getLogger().info("Starting threads to find team spawns");

            this.findThreads = new HashMap<>();

            // Create all the threads for finding spawns
            // and their associated tasks
            for (int i = 0; i < this.locations.size(); i++) {
                // Decide search width based on whether a border is enabled
                // In case of a border, use half of the border size as search radius
                // since the spawns start between border center and border wall
                int searchWidth = this.game.getConfig().isBorderEnabled()
                        ? this.game.getConfig().getOverworldBorderSize() / 2 / 16
                        : MAX_SEARCH_WIDTH;

                SpawnFindThread findThread = new SpawnFindThread(
                        this.locations.get(i),
                        searchWidth
                );

                findThread.start();

                this.findThreads.put(i, findThread);
            }

            // Start a synchronous timer to check for thread completion
            this.threadCheckTask = Bukkit.getScheduler().runTaskTimer(
                    this.game.getPlugin(),
                    this::checkThreads,
                    0L,
                    1L
            );
        } else {
            startChunkLoading();
        }
    }

    /**
     * Synchronously checks the asynchronous threads if they are done
     * And if they are all done, starts the chunk loading
     */
    private void checkThreads() {
        // Get iterator for spawn find thread mapping
        Iterator<Map.Entry<Integer, SpawnFindThread>> iterator = this.findThreads.entrySet().iterator();

        while (iterator.hasNext()) {
            // Get next entry from map
            Map.Entry<Integer, SpawnFindThread> entry = iterator.next();

            SpawnFindThread spawnFindThread = entry.getValue();
            if (spawnFindThread.isDone()) {
                // Update location in list to found location
                this.locations.set(entry.getKey(), spawnFindThread.getFoundLocation());

                // Join the thread
                try {
                    spawnFindThread.join();
                } catch (InterruptedException ignored) {
                }

                // Remove from lists
                iterator.remove();
            }
        }

        // If we have completed finding all spawns
        // we can start by chunk loading the spawns
        if (this.findThreads.isEmpty()) {
            startChunkLoading();
        }
    }

    /**
     * Starts the chunk loading process with the list of locations
     */
    private void startChunkLoading() {
        Game.getLogger().info("Starting chunk loading of found spawn locations");

        if (this.threadCheckTask != null) {
            this.threadCheckTask.cancel();
        }

        // Initialize chunk list
        this.toBeLoadedChunks = new ArrayList<>();

        // Register this class as event listener
        Bukkit.getPluginManager().registerEvents(this, this.game.getPlugin());

        for (Location location : this.locations) {
            // Get chunk associated with the location
            Chunk chunk = this.world.getChunkAt(location);

            // Keep chunk loaded until starting is finished
            chunk.setForceLoaded(true);

            // Check if chunk is not already loaded
            if (!chunk.isLoaded()) {
                // Load the chunk and add to list to wait for its load
                this.toBeLoadedChunks.add(chunk);
                chunk.load();
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // Prevent multiple executions of the callback
        if (this.callbackExecuted) {
            return;
        }

        Chunk chunk = e.getChunk();

        // Whether it is or is not on the list, try to remove it
        toBeLoadedChunks.remove(chunk);

        // If we have loaded all chunks that needed to be loaded
        if (toBeLoadedChunks.isEmpty()) {
            Game.getLogger().info("All chunks are loaded, executing callback");

            // Prevent multiple executions of the callback
            this.callbackExecuted = true;

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
