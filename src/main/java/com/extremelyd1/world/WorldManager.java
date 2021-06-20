package com.extremelyd1.world;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.generation.PregenerationManager;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;

import java.util.Map;
import java.util.Random;

public class WorldManager {

    /**
     * The game instance
     */
    private final Game game;

    /**
     * The overworld world instance
     */
    private final World world;
    /**
     * The nether world instance
     */
    private final World nether;
    /**
     * The end world instance
     */
    private final World end;

    /**
     * The pregeneration manager instance
     * Only created if config value for pregeneration is true
     */
    private PregenerationManager pregenerationManager;

    public WorldManager(Game game) throws IllegalArgumentException {
        this.game = game;

        this.world = Bukkit.getWorld("world");
        this.nether = Bukkit.getWorld("world_nether");
        this.end = Bukkit.getWorld("world_the_end");

        if (this.world == null) {
            throw new IllegalArgumentException("There is no overworld named 'world' loaded, cannot start game");
        }

        initialize();
    }

    /**
     * Initializes the loaded worlds
     * Sets the world border in all dimensions
     */
    private void initialize() {
        world.setAutoSave(false);
        if (nether != null) {
            nether.setAutoSave(false);
            nether.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
        if (end != null) {
            end.setAutoSave(false);
            end.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }

        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setTime(0);

        // If the server is in pregeneration mode, create manager
        // and stop further initialization
        if (this.game.getConfig().isPreGenerateWorlds()) {
            this.pregenerationManager = new PregenerationManager(this.game);
            return;
        }

        if (game.getConfig().isBorderEnabled()) {
            Game.getLogger().info("Settings overworld world border...");
            setWorldBorder(world);
            Game.getLogger().info("Overworld border set");

            // Set the world spawn location to the world border center
            // with the Y coordinate as the highest at that location
            Location spawnLocation = world.getWorldBorder().getCenter();
            // Increase y by 1 due to block location being at the bottom of the block
            spawnLocation.setY(world.getHighestBlockYAt(spawnLocation) + 1);
            world.setSpawnLocation(spawnLocation);

            if (nether != null) {
                Game.getLogger().info("Settings nether world border...");
                setWorldBorder(nether);
                Game.getLogger().info("Nether border set");
            }
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
                    "stronghold",
                    3000,
                    this.game.getConfig().getOverworldBorderSize()
            );
        } else if (world.getEnvironment().equals(World.Environment.NETHER)) {
            setWorldBorder(
                    world,
                    StructureType.NETHER_FORTRESS,
                    "fortress",
                    3000,
                    this.game.getConfig().getNetherBorderSize()
            );
        }
    }

    /**
     * Sets the world border with given size in the given world ensuring that the closest structure given by
     * structureType and structureName are within this border
     * @param world The world in which to set the border
     * @param structureType The type of structure that needs to be encompassed in this border
     * @param structureName The internal name of the structure
     * @param searchRadius The radius for which to search for the structure
     * @param size The size of the border
     */
    private void setWorldBorder(
            World world,
            StructureType structureType,
            String structureName,
            int searchRadius,
            int size
    ) {
        Game.getLogger().info("Locating structure " + structureName + " to determine border center");
        // Find the closest structure
        Location structureLocation = world.locateNearestStructure(
                new Location(world, 0, 0, 0),
                structureType,
                searchRadius,
                false
        );

        if (structureLocation == null) {
            Game.getLogger().warning("Could not find structure " + structureName
                    + " within " + searchRadius
                    + " blocks in world type " + world.getEnvironment()
            );
            return;
        }

        // Get the chunk at the structure location and cast to CraftChunk
        CraftChunk craftChunk = (CraftChunk) world.getChunkAt(structureLocation);

        // Get the chunk from the reference
        Chunk chunk = craftChunk.getHandle();

        if (chunk == null) {
            Game.getLogger().warning("Chunk in weak reference is null");
            return;
        }

        // Get the structure start map from the NMS Chunk
        // Suppress warnings are again needed due to generic cast and type erasure
        Map<StructureGenerator<?>, StructureStart<?>> structureStartMap = chunk.g();

        if (structureStartMap == null) {
            Game.getLogger().warning("Structure start map is null");
            return;
        }

        StructureStart<?> structureStart = null;

        for (StructureGenerator<?> structureGenerator : structureStartMap.keySet()) {
            // Check name of structure generator
            if (structureGenerator.g().equals(structureName)) {
                structureStart = structureStartMap.get(structureGenerator);
                break;
            }
        }

        // Check if the structure was actually found
        if (structureStart == null) {
            Game.getLogger().warning("Structure start map does not contain structure with name: " + structureName);
            return;
        }

        // Finally get the bounding box of the structure
        StructureBoundingBox boundingBox = structureStart.c();

        // Increase size of border if structure does not fit
        // c() is size in X direction
        if (boundingBox.c() > size) {
            size = boundingBox.c();
        }
        // e() is size in Z direction
        if (boundingBox.e() > size) {
            size = boundingBox.e();
        }

        // Make sure size is divisible by 2
        if (size % 2 == 1) {
            size += 1;
        }

        // Calculate mins and maxes based on bounding box and border size
        // j() is the getter for the maximum X
        int centerMinX = boundingBox.j() - size / 2;
        // l() is the getter for the maximum Z
        int centerMinZ = boundingBox.l() - size / 2;

        // g() is the getter for the minimum X
        int centerMaxX = boundingBox.g() + size / 2;
        // i() is the getter for the minimum Z
        int centerMaxZ = boundingBox.i() + size / 2;

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
     * Instructs the pregeneration manager to construct the given worlds
     * @param start The start index of the worlds
     * @param numberOfWorlds The number of worlds to create
     */
    public void createWorlds(int start, int numberOfWorlds) {
        this.pregenerationManager.createWorlds(start, numberOfWorlds);
    }

    /**
     * Instructs the pregeneration manager to stop the pregeneration process
     */
    public void stopPregeneration() {
        this.pregenerationManager.stop();
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
