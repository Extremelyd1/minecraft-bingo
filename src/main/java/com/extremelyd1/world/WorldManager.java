package com.extremelyd1.world;

import com.extremelyd1.config.Config;
import com.extremelyd1.game.Game;
import com.extremelyd1.util.ReflectionUtil;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.StructureBoundingBox;
import net.minecraft.server.v1_15_R1.StructureStart;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Random;

public class WorldManager {

    /**
     * The config instance
     */
    private final Config config;

    /**
     * The overworld world instance
     */
    private World world;
    /**
     * The nether world instance
     */
    private World nether;
    /**
     * The end world instance
     */
    private World end;

    public WorldManager(Config config) {
        this.config = config;

        if (Bukkit.getWorlds().size() > 3) {
            throw new IllegalStateException("There is no support for more than 3 worlds");
        }

        for (World world : Bukkit.getWorlds()) {
            switch (world.getEnvironment()) {
                case NORMAL:
                    this.world = world;
                    break;
                case NETHER:
                    this.nether = world;
                    break;
                case THE_END:
                    this.end = world;
                    break;
            }
        }

        if (world == null) {
            throw new IllegalStateException("There is no overworld loaded");
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
        }
        if (end != null) {
            end.setAutoSave(false);
        }

        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(0);

        if (config.isBorderEnabled()) {
            setWorldBorder(
                    world,
                    StructureType.STRONGHOLD,
                    "Stronghold",
                    // According to the wiki: https://minecraft.gamepedia.com/Stronghold
                    // Strongholds spawn in rings of which the first ring spawn at most 2688 blocks aways from 0, 0, 0
                    3000,
                    config.getOverworldBorderSize()
            );

            // Set the world spawn location to the world border center
            // with the Y coordinate as the highest at that location
            Location spawnLocation = world.getWorldBorder().getCenter();
            spawnLocation.setY(world.getHighestBlockYAt(spawnLocation));
            world.setSpawnLocation(spawnLocation);

            setWorldBorder(
                    nether,
                    StructureType.NETHER_FORTRESS,
                    "Fortress",
                    // Don't know what the search radius should be, but I think
                    // there should be a fortress within this radius always
                    3000,
                    config.getNetherBorderSize()
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

        // Get the chunk reference from the CraftChunk
        // Suppress warnings are needed due to generic cast and type erasure
        @SuppressWarnings("unchecked")
        WeakReference<Chunk> chunkReference = (WeakReference<Chunk>) ReflectionUtil.getField(craftChunk, "weakChunk");

        if (chunkReference == null) {
            Game.getLogger().warning("Weak reference in CraftChunk field is null");
            return;
        }

        // Get the chunk from the reference
        Chunk chunk = chunkReference.get();

        if (chunk == null) {
            Game.getLogger().warning("Chunk in weak reference is null");
            return;
        }

        // Get the structure start map from the NMS Chunk
        // Suppress warnings are again needed due to generic cast and type erasure
        @SuppressWarnings("unchecked")
        Map<String, StructureStart> structureStartMap =
                (Map<String, StructureStart>) ReflectionUtil.getField(chunk, "l");

        if (structureStartMap == null) {
            Game.getLogger().warning("Structure start map is null");
            return;
        }

        if (!structureStartMap.containsKey(structureName)) {
            Game.getLogger().warning("Structure start map does not contain structure with name: " + structureName);
            return;
        }

        // Finally get the bounding box of the structure
        StructureBoundingBox boundingBox = structureStartMap.get(structureName).c();

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
        int centerMinX = boundingBox.d - size / 2;
        int centerMinZ = boundingBox.f - size / 2;

        int centerMaxX = boundingBox.a + size / 2;
        int centerMaxZ = boundingBox.c + size / 2;

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
     * Gets the spawn location of the given environment
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
}
