package com.extremelyd1.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LocationUtil {

    /**
     * A list of air materials
     */
    private static final List<Material> airMaterials = new ArrayList<>(Arrays.asList(
            Material.AIR,
            Material.VOID_AIR
    ));
    /**
     * A list of non-valid spawn biomes
     */
    private static final List<Biome> invalidSpawnBiomes = new ArrayList<>(Arrays.asList(
            Biome.BEACH,
            Biome.OCEAN,
            Biome.RIVER,
            Biome.COLD_OCEAN,
            Biome.DEEP_OCEAN,
            Biome.WARM_OCEAN,
            Biome.SNOWY_BEACH,
            Biome.STONY_SHORE,
            Biome.FROZEN_RIVER,
            Biome.FROZEN_OCEAN,
            Biome.LUKEWARM_OCEAN,
            Biome.DEEP_COLD_OCEAN,
            Biome.DEEP_FROZEN_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN
    ));

    /**
     * Gets a list of a number of random locations on a circle with given center and radius
     * @param center The center of the circle
     * @param numLocations The number of locations to get
     * @param radius The radius of the circle
     * @return A list of locations on the circle
     */
    public static List<Location> getRandomCircleLocations(
            Location center,
            int numLocations,
            float radius
    ) {
        if (center == null || center.getWorld() == null) {
            return null;
        }

        double radiansPerLocation = 2 * Math.PI / numLocations;
        double currentAngle = new Random().nextDouble() * 2 * Math.PI;

        List<Location> result = new ArrayList<>();

        for (int i = 0; i < numLocations; i++) {
            Location circleLocation = new Location(
                    center.getWorld(),
                    Math.floor(center.getX() + Math.cos(currentAngle) * radius),
                    255,
                    Math.floor(center.getZ() + Math.sin(currentAngle) * radius)
            );

            // Simply get highest block's Y value at the calculated position and increase it by 1
            circleLocation.setY(
                    center.getWorld().getHighestBlockYAt(circleLocation)
            );

            // Add 0.5 to X and Z to spawn on center of block
            result.add(circleLocation.add(0.5, 0, 0.5));

            currentAngle += radiansPerLocation;
        }

        return result;
    }

    /**
     * Checks whether the block column given by x and z contains a valid spawn location at the highest y block
     * @param x The x coordinate
     * @param z The z coordinate
     * @return True if the highest y block in this column is a valid spawn
     */
    public static boolean containsValidSpawnLocation(World world, int x, int z) {
        Location location = new Location(
                world,
                x,
                world.getHighestBlockYAt(x, z),
                z
        );
        return isValidSpawnLocation(location);
    }

    /**
     * Checks whether a given location is valid for spawning a player
     * @param location The location to check
     * @return Whether this is a valid location to spawn
     */
    public static boolean isValidSpawnLocation(Location location) {
        Material topMaterial = copyLocation(location).add(0, 2, 0).getBlock().getType();
        Material middleMaterial = copyLocation(location).add(0, 1, 0).getBlock().getType();
        Block bottomBlock = location.getBlock();

        // Check whether the bottom block is valid
        // Non-empty, non-liquid, non-passable
        boolean bottomValid = !bottomBlock.isEmpty()
                && !bottomBlock.isLiquid()
                && !bottomBlock.isPassable();

        // If the top is not air, middle is not air, or bottom is not a non-air block,
        // it is not a valid spawn
        return airMaterials.contains(topMaterial)
                && airMaterials.contains(middleMaterial)
                && bottomValid;
    }

    /**
     * Checks whether the given biome is valid for spawning a player
     * @param biome The biome to check
     * @return True if it is a valid spawn location, false otherwise
     */
    public static boolean isValidSpawnBiome(Biome biome) {
        return !invalidSpawnBiomes.contains(biome);
    }

    /**
     * Checks whether a given location is inside the worldborder
     * @param location The location
     * @return True if it is in the world border, false otherwise
     */
    public static boolean isInsideWorldBorder(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        return world.getWorldBorder().isInside(location);
    }

    /**
     * Copies a Location instance
     * @param location The location to copy
     * @return A copy of the location
     */
    public static Location copyLocation(Location location) {
        return new Location(
                location.getWorld(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

}
