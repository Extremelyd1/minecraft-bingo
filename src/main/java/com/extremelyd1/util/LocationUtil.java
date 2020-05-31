package com.extremelyd1.util;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class LocationUtil {

    // The number of blocks in each direction of a location that
    // should be clear for it to be a valid spawn location
    private static final int VALID_SPAWN_RANGE = 1;

    public static List<Location> getRandomCircleLocations(
            Location center,
            int numLocations,
            float radius
    ) {
        double radiansPerLocation = 2 * Math.PI / numLocations;
        double currentAngle = 0;

        List<Location> result = new ArrayList<>();

        for (int i = 0; i < numLocations; i++) {
            Location circleLocation = new Location(
                    center.getWorld(),
                    center.getX() + Math.cos(currentAngle) * radius,
                    center.getY(),
                    center.getZ() + Math.sin(currentAngle) * radius
            );

            // Increase y value, until it is a valid spawn location
            while (!isValidSpawnLocation(circleLocation)) {
                circleLocation.add(0, 1, 0);
            }

            result.add(circleLocation);

            currentAngle += radiansPerLocation;
        }

        return result;
    }

    public static boolean isValidSpawnLocation(Location location) {
        for (int x = -VALID_SPAWN_RANGE; x < VALID_SPAWN_RANGE; x++) {
            for (int z = -VALID_SPAWN_RANGE; z < VALID_SPAWN_RANGE; z++) {
                Material material = copyLocation(location).add(x, 0, z).getBlock().getType();
                if (!material.equals(Material.AIR)
                        && !material.equals(Material.VOID_AIR)) {
                    return false;
                }
            }
        }

        Material topMaterial = copyLocation(location).add(0, 1, 0).getBlock().getType();
        Material bottomMaterial = location.getBlock().getType();

        if (!topMaterial.equals(Material.AIR) && !topMaterial.equals(Material.VOID_AIR)) {
            return false;
        }

        if (!bottomMaterial.equals(Material.AIR) && !bottomMaterial.equals(Material.VOID_AIR)) {
            return false;
        }

        return true;
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
