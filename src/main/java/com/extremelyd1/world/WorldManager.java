package com.extremelyd1.world;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.generation.PreGenerationManager;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.util.StructureSearchResult;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;

public class WorldManager implements Listener {

    /**
     * The game instance.
     */
    private final Game game;

    /**
     * The overworld world instance.
     */
    private World world;
    /**
     * The nether world instance.
     */
    private World nether;
    /**
     * The end world instance.
     */
    private World end;

    /**
     * The pre-generation manager instance.
     * Only created if config value for pre-generation is true.
     */
    private PreGenerationManager preGenerationManager;

    public WorldManager(Game game) throws IllegalArgumentException {
        this.game = game;

        if (game.getConfig().isBorderEnabled() && game.getConfig().isOverrideWorldGeneration()) {
            Server server = Bukkit.getServer();
            CraftServer craftServer = (CraftServer) server;

            try {
                Field confField = craftServer.getClass().getDeclaredField("configuration");
                confField.setAccessible(true);

                YamlConfiguration configuration = (YamlConfiguration) confField.get(craftServer);
                configuration.set("worlds.world.generator", game.getPlugin().getName());
                configuration.set("worlds.world_nether.generator", game.getPlugin().getName());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Game.getLogger().severe("Could not set generator in Bukkit configuration");
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        World world = e.getWorld();

        if (world.getEnvironment().equals(World.Environment.NORMAL) && this.world == null) {
            this.world = world;

            world.setAutoSave(false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setTime(0);

            // If the server is in pre-generation mode, create manager
            // and stop further initialization
            if (this.game.getConfig().isPreGenerateWorlds()) {
                this.preGenerationManager = new PreGenerationManager(this.game);
                return;
            }

            if (game.getConfig().isBorderEnabled()) {
                Game.getLogger().info("Setting overworld world border...");
                setWorldBorder(world);
                Game.getLogger().info("Overworld border set");

                // Set the world spawn location to the world border center
                // with the Y coordinate as the highest at that location
                Location spawnLocation = world.getWorldBorder().getCenter();
                // Increase y by 1 due to block location being at the bottom of the block
                spawnLocation.setY(world.getHighestBlockYAt(spawnLocation) + 1);
                world.setSpawnLocation(spawnLocation);
            }
        } else if (world.getEnvironment().equals(World.Environment.NETHER) && this.nether == null) {
            this.nether = world;

            world.setAutoSave(false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

            if (game.getConfig().isBorderEnabled()) {
                Game.getLogger().info("Setting nether world border...");
                setWorldBorder(nether);
                Game.getLogger().info("Nether border set");
            }
        } else if (world.getEnvironment().equals(World.Environment.THE_END) && this.end == null) {
            this.end = world;

            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }

        if (!this.game.getGameBoardManager().isInitialized()) {
            this.game.getGameBoardManager().initialize();
        }
    }

    /**
     * Sets the world border with given size in the given world ensuring that the closest structure given by
     * structureType and structureName are within this border
     * The size is determined by the config value
     * @param world The world to set the border on
     */
    public void setWorldBorder(World world) {
        if (world.getEnvironment().equals(World.Environment.NORMAL)) {
            setWorldBorder(
                    world,
                    StructureType.STRONGHOLD,
                    net.minecraft.world.level.levelgen.structure.StructureType.STRONGHOLD,
                    3000,
                    this.game.getConfig().getOverworldBorderSize()
            );
        } else if (world.getEnvironment().equals(World.Environment.NETHER)) {
            setWorldBorder(
                    world,
                    StructureType.FORTRESS,
                    net.minecraft.world.level.levelgen.structure.StructureType.FORTRESS,
                    3000,
                    this.game.getConfig().getNetherBorderSize()
            );
        }
    }

    /**
     * Sets the world border with given size in the given world ensuring that the closest structure given by
     * structureType and structureName are within this border
     * @param world The world in which to set the border
     * @param bukkitStructureType The bukkit type of structure that needs to be encompassed in this border
     * @param nmsStructureType The internal NMS type of the structure
     * @param searchRadius The radius for which to search for the structure
     * @param size The size of the border
     */
    private <S extends Structure> void setWorldBorder(
            World world,
            StructureType bukkitStructureType,
            net.minecraft.world.level.levelgen.structure.StructureType<S> nmsStructureType,
            int searchRadius,
            int size
    ) {
        Game.getLogger().info("Locating structure " + bukkitStructureType.getKey() + " to determine border center");
        // Find the closest structure
        StructureSearchResult structureSearchResult = world.locateNearestStructure(
                new Location(world, 0, 0, 0),
                bukkitStructureType,
                searchRadius,
                false
        );

        if (structureSearchResult == null) {
            Game.getLogger().warning("Could not find structure " + bukkitStructureType
                    + " within " + searchRadius
                    + " blocks in world type " + world.getEnvironment()
            );
            return;
        }

        // Get the chunk at the structure location and cast to CraftChunk
        CraftChunk craftChunk = (CraftChunk) world.getChunkAt(structureSearchResult.getLocation());

        // Get the chunk from the reference
        ChunkAccess chunk = craftChunk.getHandle(ChunkStatus.STRUCTURE_STARTS);

        if (chunk == null) {
            Game.getLogger().warning("Chunk access from craft chunk is null");
            return;
        }

        // Get the structure start map from the NMS Chunk
        // Suppress warnings are again needed due to generic cast and type erasure
        Map<Structure, StructureStart> structureStartMap = chunk.getAllStarts();

        StructureStart structureStart = null;

        for (Structure structure : structureStartMap.keySet()) {
            // Check type of structure feature
            if (structure.type().equals(nmsStructureType)) {
                structureStart = structureStartMap.get(structure);
                break;
            }
        }

        // Check if the structure was actually found
        if (structureStart == null) {
            Game.getLogger().warning("Structure start map does not contain structure with type: " + bukkitStructureType);
            return;
        }

        // Finally, get the bounding box of the structure
        BoundingBox boundingBox = structureStart.getBoundingBox();

        // Increase size of border if structure does not fit
        if (boundingBox.getXSpan() > size) {
            size = boundingBox.getXSpan();
        }
        if (boundingBox.getZSpan() > size) {
            size = boundingBox.getZSpan();
        }

        // Make sure size is divisible by 2
        if (size % 2 == 1) {
            size += 1;
        }

        // Calculate mins and maxes based on bounding box and border size
        int centerMinX = boundingBox.maxX() - size / 2;
        int centerMinZ = boundingBox.maxZ() - size / 2;

        int centerMaxX = boundingBox.minX() + size / 2;
        int centerMaxZ = boundingBox.minZ() + size / 2;

        // Uncomment below to center world border on structure
//        int centerX = centerMinX + (centerMaxX - centerMinX) / 2;
//        int centerZ = centerMinZ + (centerMaxZ - centerMinZ) / 2;

        // Put border at random location to include the structure
        Random random = new Random(world.getSeed());
        int centerX = random.nextInt(centerMaxX - centerMinX + 1) + centerMinX;
        int centerZ = random.nextInt(centerMaxZ - centerMinZ + 1) + centerMinZ;

        WorldBorder border = world.getWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(size);
    }

    /**
     * Resets the gamerules of the overworld to default vanilla behaviour
     */
    public void onGameStart() {
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
    }

    /**
     * Instructs the pre-generation manager to construct the given worlds
     * @param start The start index of the worlds
     * @param numberOfWorlds The number of worlds to create
     */
    public void createWorlds(int start, int numberOfWorlds) {
        this.preGenerationManager.createWorlds(start, numberOfWorlds);
    }

    /**
     * Instructs the pre-generation manager to stop the pre-generation process
     */
    public void stopPreGeneration() {
        this.preGenerationManager.stop();
    }

    /**
     * Gets the spawn location of the overworld
     * @return The spawn location
     */
    public Location getSpawnLocation() {
        return world.getSpawnLocation();
    }

    public World getWorld() {
        return world;
    }

    public World getNether() {
        return nether;
    }

    public World getEnd() {
        return end;
    }
}
