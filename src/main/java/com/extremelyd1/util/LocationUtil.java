package com.extremelyd1.util;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LocationUtil {

    private static final List<Material> airMaterials = new ArrayList<>(Arrays.asList(Material.AIR, Material.VOID_AIR));

    public static List<Location> getRandomCircleLocations(
            Location center,
            int numLocations,
            float radius
    ) {
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

            // Decrease y value, until it is a valid spawn location
            while (!isValidSpawnLocation(circleLocation)) {
                circleLocation.subtract(0, 1, 0);
            }

            Material spawnMaterial = copyLocation(circleLocation).subtract(0, 1, 0).getBlock().getType();
            System.out.println("Spawn found on block: " + spawnMaterial);

            // Add 0.5 to X and Z to spawn on center of block
            result.add(circleLocation.add(0.5, 0, 0.5));
            System.out.println("Spawn location: " + circleLocation.toString());

            currentAngle += radiansPerLocation;
        }

        return result;
    }

    public static boolean isValidSpawnLocation(Location location) {
        Material topMaterial = copyLocation(location).add(0, 1, 0).getBlock().getType();
        Material middleMaterial = location.getBlock().getType();
        Material bottomMaterial = copyLocation(location).subtract(0, 1, 0).getBlock().getType();

        // If the top is not air, middle is not air, or bottom is not a non-air block
        // It is not a valid spawn
        return airMaterials.contains(topMaterial)
                && airMaterials.contains(middleMaterial)
                && !airMaterials.contains(bottomMaterial);
    }

    private static Location copyLocation(Location location) {
        return new Location(
                location.getWorld(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

}
