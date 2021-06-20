package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.generation.zip.PendingZip;
import com.extremelyd1.world.generation.zip.WorldZippingThread;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PregenerationManager {

    /**
     * The game instance
     */
    private final Game game;

    /**
     * The chunk generation thread
     */
    private final ChunkGenerationThread chunkGenerationThread;
    /**
     * The zipping thread
     */
    private final WorldZippingThread worldZippingThread;

    /**
     * The bukkit task checking for updates
     */
    private BukkitTask updateTask;

    /**
     * A queue containing worlds that are pending generation
     */
    private final Queue<PendingGeneration> pendingGenerationQueue;
    /**
     * A queue containing pending zips that are pending zipping
     */
    private final Queue<PendingGeneration> pendingZipQueue;

    /**
     * A queue containing worlds that are done generating
     */
    private final Queue<PendingGeneration> generatedWorldQueue;

    public PregenerationManager(Game game) {
        this.game = game;

        this.pendingGenerationQueue = new ConcurrentLinkedQueue<>();
        this.pendingZipQueue = new ConcurrentLinkedQueue<>();

        this.generatedWorldQueue = new ConcurrentLinkedQueue<>();

        this.chunkGenerationThread = new ChunkGenerationThread(
                this,
                game.getConfig().getPreGenerationTicksPerCycle(),
                game.getConfig().getPreGenerationChunksPerCycle()
        );

        this.worldZippingThread = new WorldZippingThread();

        Game.getLogger().info("Starting chunk generation thread");
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                this.game.getPlugin(),
                this.chunkGenerationThread,
                0L,
                1L
        );

        this.updateTask = Bukkit.getScheduler().runTaskTimer(
                this.game.getPlugin(),
                this::checkThreadUpdates,
                0L,
                1L
        );
    }

    /**
     * Pre-generates and zips a number of worlds determined by start and number
     * @param start The number to start at
     * @param number The total number of worlds te create
     */
    public void createWorlds(int start, int number) {
        // Checked whether the update task is stopped
        if (this.updateTask.isCancelled()) {
            // Restart the update task
            this.updateTask = Bukkit.getScheduler().runTaskTimer(
                    this.game.getPlugin(),
                    this::checkThreadUpdates,
                    0L,
                    1L
            );
        }

        for (int i = start; i < number + start; i++) {
            // Add overworld with index
            this.pendingGenerationQueue.add(new PendingGeneration(
                    i,
                    World.Environment.NORMAL
            ));

            // If nether is enabled, add nether with index
            if (Bukkit.getAllowNether()) {
                this.pendingGenerationQueue.add(new PendingGeneration(
                        i,
                        World.Environment.NETHER
                ));
            }

            // If end is enabled, add end with index
            if (Bukkit.getAllowEnd()) {
                this.pendingGenerationQueue.add(new PendingGeneration(
                        i,
                        World.Environment.THE_END
                ));
            }
        }
    }

    /**
     * Stops all current world generation and zipping
     * Shuts down world generation immediately and finishes the
     * last zipping task before shutting down
     */
    public void stop() {
        Game.getLogger().info("Stopping pregeneration...");

        // Stop the update task
        this.updateTask.cancel();

        // Stop the chunk generation thread
        // This will add all scheduled worlds in the thread
        // to the generated queue
        this.chunkGenerationThread.stop();

        Game.getLogger().info("Unloading all active worlds");
        while (!this.generatedWorldQueue.isEmpty()) {
            PendingGeneration pendingGeneration = this.generatedWorldQueue.poll();

            // Unload the world without saving
            Bukkit.unloadWorld(pendingGeneration.getWorld(), false);

            // Delete the world folder
            pendingGeneration.deleteWorldFolder();
        }

        // Clear the rest of the queues
        this.pendingGenerationQueue.clear();
        this.pendingZipQueue.clear();

        if (this.worldZippingThread.isIdle()) {
            Game.getLogger().info("Pregeneration stopped successfully!");
        } else {
            Game.getLogger().info("Please wait for the current zip to finish before shutting down");
        }
    }

    /**
     * Checks the threads for updates and checks the generated worlds queue for worlds that are done
     */
    private void checkThreadUpdates() {
        // Check whether there are worlds generated that are ready for zipping
        if (!this.generatedWorldQueue.isEmpty()) {
            PendingGeneration pendingGeneration = this.generatedWorldQueue.poll();

            // Save world folder and unload world
            Game.getLogger().info("Unloading generated " + pendingGeneration + ", saving world folder");
            pendingGeneration.setWorldFolder(pendingGeneration.getWorld().getWorldFolder());

            // Unload world
            Bukkit.unloadWorld(pendingGeneration.getWorld(), true);

            // Add current generated world to zipping queue
            this.pendingZipQueue.add(pendingGeneration);

            Game.getLogger().info("World " + pendingGeneration + " has generated, now zipping...");
        }

        checkGenerationThread();

        checkZippingThread();
    }

    /**
     * Checks the generation thread for updates
     */
    private void checkGenerationThread() {
        if (this.chunkGenerationThread.isIdle() && !this.pendingGenerationQueue.isEmpty()) {
            Game.getLogger().info("Creating new pending world for chunk generation");
            PendingGeneration pendingGeneration = this.pendingGenerationQueue.poll();

            // Decide world creator based on world environment
            WorldCreator worldCreator = null;
            switch (pendingGeneration.getEnvironment()) {
                case NORMAL:
                    worldCreator = new WorldCreator(
                            "world" + pendingGeneration.getIndex()
                    ).copy(this.game.getWorldManager().getWorld());
                    break;
                case NETHER:
                    worldCreator = new WorldCreator(
                            "world" + pendingGeneration.getIndex() + "_nether"
                    ).copy(this.game.getWorldManager().getNether());
                    break;
                case THE_END:
                    worldCreator = new WorldCreator(
                            "world" + pendingGeneration.getIndex() + "_the_end"
                    ).copy(this.game.getWorldManager().getEnd());
                    break;
            }

            // Randomize seed of worldcreator
            worldCreator.seed(new Random().nextLong());

            // Create the new world
            World newWorld = Bukkit.createWorld(worldCreator);

            if (newWorld != null) {
                // Set the world
                pendingGeneration.setWorld(newWorld);

                Game.getLogger().info("World created successfully, scheduling chunk generation for " + pendingGeneration);
                World.Environment environment = pendingGeneration.getEnvironment();

                // Check whether it is the end environment
                if (!environment.equals(World.Environment.THE_END)) {
                    // Set border of world
                    this.game.getWorldManager().setWorldBorder(newWorld);

                    // Schedule chunk generation of the world
                    scheduleChunksInBorder(pendingGeneration, this.chunkGenerationThread);
                } else {
                    Game.getLogger().info(
                            "End world, no chunk generation needed for "
                                    + pendingGeneration
                                    + ", adding to generated queue"
                    );

                    // Pass pending world directly to generated queue
                    this.generatedWorldQueue.add(pendingGeneration);
                }
            }
        }
    }

    /**
     * Checks the zipping thread for updates
     */
    private void checkZippingThread() {
        // Only start new zip if thread is idle and the queue is non-empty
        if (this.worldZippingThread.isIdle() && !this.pendingZipQueue.isEmpty()) {
            PendingGeneration pendingGeneration = this.pendingZipQueue.poll();
            Game.getLogger().info("Creating new pending zip for zipping for " + pendingGeneration);

            String dirName = "world";
            switch (pendingGeneration.getEnvironment()) {
                case NORMAL:
                    break;
                case NETHER:
                    dirName += "_nether";
                    break;
                case THE_END:
                    dirName += "_the_end";
                    break;
            }

            PendingZip pendingZip = new PendingZip(
                    getWorldsZip(),
                    "world" + pendingGeneration.getIndex(),
                    pendingGeneration.getWorldFolder(),
                    dirName,
                    pendingGeneration.getEnvironment(),
                    pendingGeneration.getIndex()
            );

            Game.getLogger().info("Scheduling pending zip " + pendingZip);
            worldZippingThread.scheduleZip(game.getPlugin(), pendingZip);
        }
    }

    /**
     * Schedules all chunks within the world border to be generated by the given generation thread
     * @param pendingGeneration The pending world in which the chunks need to be generated
     * @param generationThread The thread to generate them in
     */
    private void scheduleChunksInBorder(PendingGeneration pendingGeneration, ChunkGenerationThread generationThread) {
        World world = pendingGeneration.getWorld();
        WorldBorder worldBorder = world.getWorldBorder();

        double size = worldBorder.getSize();

        Location corner1 = worldBorder.getCenter().clone().add(size / 2.0D, size / 2.0D, size / 2.0D);
        Location corner2 = worldBorder.getCenter().clone().subtract(size / 2.0D, size / 2.0D, size / 2.0D);

        final int CHUNK_SIZE = 16;
        final int BUFFER = 1;

        int x1 = Integer.min(corner1.getBlockX() / CHUNK_SIZE, corner2.getBlockX() / CHUNK_SIZE) - BUFFER;
        int z1 = Integer.min(corner1.getBlockZ() / CHUNK_SIZE, corner2.getBlockZ() / CHUNK_SIZE) - BUFFER;
        int x2 = Integer.max(corner1.getBlockX() / CHUNK_SIZE, corner2.getBlockX() / CHUNK_SIZE) + BUFFER;
        int z2 = Integer.max(corner1.getBlockZ() / CHUNK_SIZE, corner2.getBlockZ() / CHUNK_SIZE) + BUFFER;

        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                pendingGeneration.add(new PendingChunk(world, x, z));
            }
        }

        generationThread.scheduleWorld(pendingGeneration);
    }

    /**
     * Retrieve the worlds zip file, creates it if it does not exist yet
     * @return A file
     */
    private File getWorldsZip() {
        File worldsZipFile = new File("worlds.zip");
        if (!worldsZipFile.exists()) {
            try {
                Game.getLogger().info("Creating new worlds.zip file");
                if (!worldsZipFile.createNewFile()) {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return worldsZipFile;
    }

    /**
     * Add a world to the generated queue to indicate it is done generating chunks
     * @param pendingGeneration The pending world that needs to be added
     */
    public void addGeneratedWorld(PendingGeneration pendingGeneration) {
        this.generatedWorldQueue.add(pendingGeneration);
    }

}
