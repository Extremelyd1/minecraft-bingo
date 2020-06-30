package com.extremelyd1.world;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.generation.ChunkGenerationThread;
import com.extremelyd1.world.generation.PendingChunk;
import com.extremelyd1.world.generation.PendingWorld;
import com.extremelyd1.world.zip.PendingZip;
import com.extremelyd1.world.zip.WorldZippingThread;
import org.bukkit.*;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PregenerationManager {

    private final Game game;

    private final ChunkGenerationThread chunkGenerationThread;
    private final WorldZippingThread worldZippingThread;

    private final Queue<PendingWorld> pendingWorldQueue;
    private final Queue<PendingWorld> pendingZipQueue;

    private final Queue<PendingWorld> generatedWorldQueue;

    public PregenerationManager(Game game) {
        this.game = game;

        this.pendingWorldQueue = new ConcurrentLinkedQueue<>();
        this.pendingZipQueue = new ConcurrentLinkedQueue<>();

        this.generatedWorldQueue = new ConcurrentLinkedQueue<>();

        this.chunkGenerationThread = new ChunkGenerationThread(this);

        this.worldZippingThread = new WorldZippingThread(this);

        Game.getLogger().info("Starting chunk generation thread");
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                this.game.getPlugin(),
                this.chunkGenerationThread,
                0L,
                1L
        );

        Game.getLogger().info("Starting world zipping thread");
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                this.game.getPlugin(),
                this.worldZippingThread,
                0L,
                1L
        );

        Bukkit.getScheduler().runTaskTimer(
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
        for (int i = start; i < number + start; i++) {
            // Add overworld with index
            this.pendingWorldQueue.add(new PendingWorld(
                    i,
                    World.Environment.NORMAL
            ));

            // If nether is enabled, add nether with index
            if (Bukkit.getAllowNether()) {
                this.pendingWorldQueue.add(new PendingWorld(
                        i,
                        World.Environment.NETHER
                ));
            }

            // If end is enabled, add end with index
            if (Bukkit.getAllowEnd()) {
                this.pendingWorldQueue.add(new PendingWorld(
                        i,
                        World.Environment.THE_END
                ));
            }
        }
    }

    /**
     * Checks the threads for updates and checks the generated worlds queue for worlds that are done
     */
    private void checkThreadUpdates() {
        // Check whether there are worlds generated that are ready for zipping
        if (!this.generatedWorldQueue.isEmpty()) {
            PendingWorld pendingWorld = this.generatedWorldQueue.poll();

            // Save world folder and unload world
            Game.getLogger().info("Unloading generated " + pendingWorld + ", saving world folder");
            pendingWorld.setWorldFolder(pendingWorld.getWorld().getWorldFolder());

            // Unload world
            Bukkit.unloadWorld(pendingWorld.getWorld(), true);

            // Add current generated world to zipping queue
            Game.getLogger().info("Added generated " + pendingWorld + " to zipping queue");
            this.pendingZipQueue.add(pendingWorld);
        }

        checkGenerationThread();

        checkZippingThread();
    }

    /**
     * Checks the generation thread for updates
     */
    private void checkGenerationThread() {
        if (this.chunkGenerationThread.isIdle() && !this.pendingWorldQueue.isEmpty()) {
            Game.getLogger().info("Creating new pending world for chunk generation");
            PendingWorld pendingWorld = this.pendingWorldQueue.poll();

            // Decide world creator based on world environment
            WorldCreator worldCreator = null;
            switch (pendingWorld.getEnvironment()) {
                case NORMAL:
                    worldCreator = new WorldCreator(
                            "world" + pendingWorld.getIndex()
                    ).copy(this.game.getWorldManager().getWorld());
                    break;
                case NETHER:
                    worldCreator = new WorldCreator(
                            "world" + pendingWorld.getIndex() + "_nether"
                    ).copy(this.game.getWorldManager().getNether());
                    break;
                case THE_END:
                    worldCreator = new WorldCreator(
                            "world" + pendingWorld.getIndex() + "_the_end"
                    ).copy(this.game.getWorldManager().getEnd());
                    break;
            }

            // Create the new world
            World newWorld = Bukkit.createWorld(worldCreator);

            // Set the world
            pendingWorld.setWorld(newWorld);
            if (newWorld != null) {
                Game.getLogger().info("World created successfully, scheduling chunk generation for " + pendingWorld);
                World.Environment environment = pendingWorld.getEnvironment();

                // Check whether it is the end environment
                if (!environment.equals(World.Environment.THE_END)) {
                    // Set border of world
                    this.game.getWorldManager().setWorldBorder(newWorld);

                    // Schedule chunk generation of the world
                    scheduleChunksInBorder(pendingWorld, this.chunkGenerationThread);
                } else {
                    Game.getLogger().info("End world, no chunk generation needed for " + pendingWorld + ", adding to zip queue");

                    // Pass pending world directly to generated queue
                    this.generatedWorldQueue.add(pendingWorld);
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
            PendingWorld pendingWorld = this.pendingZipQueue.poll();
            Game.getLogger().info("Creating new pending zip for zipping for " + pendingWorld);

            String dirName = "world";
            switch (pendingWorld.getEnvironment()) {
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
                    "world" + pendingWorld.getIndex(),
                    pendingWorld.getWorldFolder(),
                    dirName
            );

            Game.getLogger().info("Scheduling pending zip " + pendingZip);
            worldZippingThread.scheduleZip(pendingZip);
        }
    }

    /**
     * Schedules all chunks within the world border to be generated by the given generation thread
     * @param pendingWorld The pending world in which the chunks need to be generated
     * @param generationThread The thread to generate them in
     */
    private void scheduleChunksInBorder(PendingWorld pendingWorld, ChunkGenerationThread generationThread) {
        World world = pendingWorld.getWorld();
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
                pendingWorld.getPendingChunks().add(new PendingChunk(world, x, z));
            }
        }

        generationThread.scheduleWorld(pendingWorld);
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

    public void addGeneratedWorld(PendingWorld pendingWorld) {
        this.generatedWorldQueue.add(pendingWorld);
    }

}
