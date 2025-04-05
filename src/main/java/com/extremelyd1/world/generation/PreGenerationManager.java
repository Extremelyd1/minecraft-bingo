package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * Manager class for pre-generation of worlds.
 */
public class PreGenerationManager {
    /**
     * The game instance.
     */
    private final Game game;

    /**
     * The task scheduler instance to schedule generation tasks.
     */
    private final TaskScheduler taskScheduler;

    /**
     * Queue containing record instances for pending generations.
     */
    private final Queue<PendingGeneration> pendingGenerations;

    /**
     * Semaphore for thread safety in generation.
     */
    private final Semaphore generationSemaphore;

    /**
     * Random instance for assigning world seeds.
     */
    private final Random random;

    /**
     * Whether the task for chunk generation is currently running.
     */
    private boolean isRunning;

    /**
     * Construct the pre-generation manager with the game instance.
     *
     * @param game The game instance.
     */
    public PreGenerationManager(Game game) {
        this.game = game;
        this.taskScheduler = new TaskScheduler();

        this.pendingGenerations = new ConcurrentLinkedQueue<>();
        this.generationSemaphore = new Semaphore(1);
        this.random = new Random();
    }

    /**
     * Pre-generates and zips a number of worlds determined by start index and number of worlds to generate.
     *
     * @param start  The number/index to start generating at.
     * @param number The total number of worlds to generate.
     */
    public void createWorlds(int start, int number) {
        for (int i = start; i < number + start; i++) {
            // Add overworld with index
            this.pendingGenerations.add(new PendingGeneration(
                    i,
                    World.Environment.NORMAL
            ));

            // If nether is enabled, add nether with index
            if (Bukkit.getAllowNether()) {
                this.pendingGenerations.add(new PendingGeneration(
                        i,
                        World.Environment.NETHER
                ));
            }

            // If end is enabled, add end with index
            if (Bukkit.getAllowEnd()) {
                this.pendingGenerations.add(new PendingGeneration(
                        i,
                        World.Environment.THE_END
                ));
            }
        }

        if (!isRunning) {
            taskScheduler.runTask(this::run);
        }
    }

    /**
     * Stops all current world generation and other tasks.
     */
    public void stop() {
        Game.getLogger().info("Stopping pre-generation...");

        Game.getLogger().info("Cancelling tasks...");

        this.isRunning = false;
        this.taskScheduler.cancelTasks();

        Game.getLogger().info("Unloading all active worlds...");

        for (World world : Bukkit.getWorlds()) {
            if (world.equals(game.getWorldManager().getWorld())
                    || world.equals(game.getWorldManager().getNether())
                    || world.equals(game.getWorldManager().getEnd())
            ) {
                continue;
            }

            // Unload the world without saving, since we are not zipping it anymore, and we are deleting it afterward
            Bukkit.unloadWorld(world, false);

            try {
                FileUtils.deleteDirectory(world.getWorldFolder());
            } catch (IOException e) {
                Game.getLogger().warning(String.format(
                        "Could not delete world folder for %s, exception:\n%s",
                        world.getName(),
                        e
                ));
            }
        }

        Game.getLogger().info("Pre-generation stopped successfully!");
    }

    /**
     * Runs the asynchronous task of getting pending generations and eventually generating them.
     */
    private void run() {
        isRunning = true;

        while (isRunning) {
            try {
                generationSemaphore.acquire();
            } catch (InterruptedException e) {
                isRunning = false;
                break;
            }

            if (pendingGenerations.isEmpty()) {
                Game.getLogger().info("No more pending generations left, stopping pre-generation");
                isRunning = false;
                return;
            }

            PendingGeneration pendingGeneration = pendingGenerations.poll();

            Game.getLogger().info(String.format(
                    "Pending generation found: %s, creating world",
                    pendingGeneration
            ));

            WorldCreator worldCreator;
            switch (pendingGeneration.environment()) {
                case NORMAL -> worldCreator = new WorldCreator(
                        "world" + pendingGeneration.index()
                ).copy(this.game.getWorldManager().getWorld());

                case NETHER -> worldCreator = new WorldCreator(
                        "world" + pendingGeneration.index() + "_nether"
                ).copy(this.game.getWorldManager().getNether());

                case THE_END -> worldCreator = new WorldCreator(
                        "world" + pendingGeneration.index() + "_the_end"
                ).copy(this.game.getWorldManager().getEnd());

                default -> {
                    continue;
                }
            }

            worldCreator.seed(random.nextLong());

            Game.getLogger().info("Starting world creation on main thread...");

            Future<World> future = Bukkit.getScheduler().callSyncMethod(game.getPlugin(), () -> {
                World world = Bukkit.createWorld(worldCreator);

                if (world == null) {
                    Game.getLogger().warning(String.format(
                            "Tried creating new world (%s) for pre-generation, but failed",
                            pendingGeneration
                    ));
                    return null;
                }

                if (pendingGeneration.environment().equals(World.Environment.THE_END)) {
                    Game.getLogger().info(String.format(
                            "World (%s) created, no chunk generation for end",
                            pendingGeneration
                    ));

                    return world;
                }

                Game.getLogger().info(String.format(
                        "World (%s) created, starting chunk generation",
                        pendingGeneration
                ));

                this.game.getWorldManager().setWorldBorder(world);

                return world;
            });

            World world;
            try {
                world = future.get();
            } catch (InterruptedException | ExecutionException e) {
                Game.getLogger().info(String.format(
                        "Could not create world (%s), skipping generation",
                        pendingGeneration
                ));

                generationSemaphore.release();
                continue;
            }

            if (world == null) {
                generationSemaphore.release();
                continue;
            }

            if (pendingGeneration.environment().equals(World.Environment.THE_END)) {
                processGeneratedWorld(pendingGeneration, world);
                continue;
            }

            GenerationTask currentGenerationTask = new GenerationTask(
                    world,
                    () -> processGeneratedWorld(pendingGeneration, world)
            );
            taskScheduler.runTask(currentGenerationTask);
        }
    }

    /**
     * Processes a given pending generation and its corresponding world for zipping, unloading and deletion.
     *
     * @param pendingGeneration The pending generation to process.
     * @param world             The world instance for the pending generation.
     */
    private void processGeneratedWorld(PendingGeneration pendingGeneration, World world) {
        Game.getLogger().info(String.format(
                "World (%s) finished pre-generating, unloading world",
                pendingGeneration
        ));

        boolean unloadSuccess;
        try {
            unloadSuccess = Bukkit.getScheduler().callSyncMethod(
                    game.getPlugin(),
                    () -> Bukkit.unloadWorld(world, true)
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            Game.getLogger().warning(String.format(
                    "Error occurred while unloading world (%s), cannot zip world",
                    pendingGeneration
            ));

            generationSemaphore.release();
            return;
        }

        if (!unloadSuccess) {
            Game.getLogger().warning(String.format(
                    "Could not unload world (%s), cannot zip world",
                    pendingGeneration
            ));

            generationSemaphore.release();
            return;
        }

        String dirName = "world";
        switch (world.getEnvironment()) {
            case NETHER -> dirName += "_nether";
            case THE_END -> dirName += "_the_end";
        }

        File worldZip = new File(String.format(
                "%s/world%s.zip",
                getWorldsFolder().getPath(),
                pendingGeneration.index()
        ));

        FileUtil.packZip(
                worldZip,
                world.getWorldFolder(),
                dirName
        );

        try {
            FileUtils.deleteDirectory(world.getWorldFolder());
        } catch (IOException e) {
            Game.getLogger().warning(String.format(
                    "Could not delete world folder for %s, exception:\n%s",
                    world.getName(),
                    e
            ));
        }

        generationSemaphore.release();
    }

    /**
     * Gets the folder that pre-generated worlds should be stored in.
     *
     * @return A file instance for the 'worlds' folder.
     */
    private File getWorldsFolder() {
        File dataFolder = game.getDataFolder();
        File worldsFolder = new File(dataFolder.getPath() + "/worlds/");

        if (!worldsFolder.exists()) {
            if (!worldsFolder.mkdirs()) {
                Game.getLogger().warning("Could not create worlds directory");
            }
        }

        return worldsFolder;
    }
}
