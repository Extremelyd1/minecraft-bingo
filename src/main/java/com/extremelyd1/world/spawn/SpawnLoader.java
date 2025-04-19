package com.extremelyd1.world.spawn;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;

/**
 * Provides a means of finding valid spawns, loading the chunks of these spawns, and executing a method once those
 * spawns are found and chunks finish loading.
 */
public class SpawnLoader implements Listener {

    /**
     * The maximum width of a search spiral before cancelling the search.
     * Only used when the world border is disabled.
     */
    private static final int MAX_SEARCH_WIDTH = 200;

    /**
     * The game instance.
     */
    private final Game game;
    /**
     * The world where these locations need to be loaded.
     */
    private final World world;
    /**
     * The locations for which to load valid spawns.
     */
    private final List<Location> locations;
    /**
     * The callback for when the spawn chunks are loaded.
     */
    private final Consumer<List<Location>> onLoad;

    /**
     * A map containing all threads that are finding spawns.
     */
    private Set<SpawnFindThread> findThreads;
    /**
     * The locations that were found to be valid spawns.
     */
    private List<Location> foundLocations;
    /**
     * The number of chunks that need to be loaded.
     */
    private int toBeLoadedChunks;

    /**
     * The bukkit task that checks the asynchronous tasks synchronously.
     */
    private BukkitTask threadCheckTask;

    public SpawnLoader(
            Game game,
            WorldManager worldManager,
            List<Location> locations,
            Consumer<List<Location>> onLoad
    ) {
        this.game = game;
        this.world = worldManager.getWorld();
        this.locations = locations;
        this.onLoad = onLoad;
    }

    /**
     * Start finding spawns, then loading the chunks, and if finished execute the on-load method.
     */
    public void start() {
        if (game.getConfig().isPreventWaterSpawns()) {
            Game.getLogger().info("Starting threads to find team spawns");

            this.findThreads = new HashSet<>();
            this.foundLocations = new ArrayList<>();

            // Create all the threads for finding spawns
            // and their associated tasks
            for (Location location : this.locations) {
                // Decide search width based on whether a border is enabled
                // In case of a border, use half of the border size as search radius
                // since the spawns start between border center and border wall
                int searchWidth = this.game.getConfig().isBorderEnabled()
                        ? this.game.getConfig().getOverworldBorderSize() / 2 / 16
                        : MAX_SEARCH_WIDTH;

                SpawnFindThread findThread = new SpawnFindThread(
                        location,
                        searchWidth
                );

                findThread.start();

                this.findThreads.add(findThread);
            }

            // Start a synchronous timer to check for thread completion
            this.threadCheckTask = Bukkit.getScheduler().runTaskTimer(
                    this.game.getPlugin(),
                    this::checkThreads,
                    0L,
                    1L
            );
        } else {
            this.foundLocations = new ArrayList<>(this.locations);

            startChunkLoading();
        }
    }

    /**
     * Synchronously checks the asynchronous threads if they are done.
     * And if they are all done, starts the chunk loading.
     */
    private void checkThreads() {
        Iterator<SpawnFindThread> iterator = findThreads.iterator();

        while (iterator.hasNext()) {
            SpawnFindThread thread = iterator.next();

            if (thread.isDone()) {
                // Add found location to list
                this.foundLocations.add(thread.getFoundLocation());

                // Join the thread
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Game.getLogger().warning("Could not join spawn find thread, exception:\n%s".formatted(e));
                }

                // Remove this thread from the set of threads that we iterate over each time
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
     * Starts the chunk loading process with the list of locations.
     */
    private void startChunkLoading() {
        Game.getLogger().info("Starting chunk loading of found spawn locations");

        if (this.threadCheckTask != null) {
            this.threadCheckTask.cancel();
        }

        this.toBeLoadedChunks = this.foundLocations.size();

        for (Location location : this.foundLocations) {
            // Get chunk coordinates of the location
            int chunkX = (int) Math.floor(location.getX() / 16);
            int chunkZ = (int) Math.floor(location.getZ() / 16);

            // Request the chunk to be loaded with a callback
            world.getChunkAtAsync(chunkX, chunkZ, false).thenAccept(this::onChunkLoad);
        }
    }

    /**
     * Callback method for when a chunk is loaded.
     * @param chunk The chunk that was loaded.
     */
    private void onChunkLoad(Chunk chunk) {
        // Decrease the number of chunks to be loaded
        toBeLoadedChunks--;

        // If we have loaded all chunks that needed to be loaded
        if (toBeLoadedChunks == 0) {
            Game.getLogger().info("All chunks are loaded, executing callback");

            // Run on-load method
            onLoad.accept(this.foundLocations);
        }
    }
}
