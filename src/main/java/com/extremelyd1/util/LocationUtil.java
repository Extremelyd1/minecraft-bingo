package com.extremelyd1.util;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LocationUtil {

    /**
     * A list of air materials
     */
    private static final List<Material> airMaterials = new ArrayList<>(Arrays.asList(Material.AIR, Material.VOID_AIR));

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
                    center.getWorld().getHighestBlockYAt(circleLocation) + 1
            );

            // Add 0.5 to X and Z to spawn on center of block
            result.add(circleLocation.add(0.5, 0, 0.5));

            currentAngle += radiansPerLocation;
        }

        return result;
    }

    /**
     * Checks whether a given location is valid for spawning a player
     * @param location The location to check
     * @return Whether this is a valid location to spawn
     */
    private static boolean isValidSpawnLocation(Location location) {
        Material topMaterial = copyLocation(location).add(0, 1, 0).getBlock().getType();
        Material middleMaterial = location.getBlock().getType();
        Material bottomMaterial = copyLocation(location).subtract(0, 1, 0).getBlock().getType();

        // If the top is not air, middle is not air, or bottom is not a non-air block
        // It is not a valid spawn
        return airMaterials.contains(topMaterial)
                && airMaterials.contains(middleMaterial)
                && !airMaterials.contains(bottomMaterial);
    }

    /**
     * Copies a Location instance
     * @param location The location to copy
     * @return A copy of the location
     */
    private static Location copyLocation(Location location) {
        return new Location(
                location.getWorld(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

}
