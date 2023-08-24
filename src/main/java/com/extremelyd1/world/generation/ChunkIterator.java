package com.extremelyd1.world.generation;

import org.bukkit.Location;
import org.bukkit.WorldBorder;

import java.util.NoSuchElementException;

public class ChunkIterator {
    private final int x1, x2, z1, z2;
    private final long total;
    private int x, z;
    private boolean hasNext = true;

    public ChunkIterator(final WorldBorder worldBorder) {
        double size = worldBorder.getSize();

        Location corner1 = worldBorder.getCenter().clone().add(size / 2.0D, size / 2.0D, size / 2.0D);
        Location corner2 = worldBorder.getCenter().clone().subtract(size / 2.0D, size / 2.0D, size / 2.0D);

        final int CHUNK_SIZE = 16;
        final int BUFFER = 2;

        this.x1 = Integer.min(corner1.getBlockX() / CHUNK_SIZE, corner2.getBlockX() / CHUNK_SIZE) - BUFFER;
        this.z1 = Integer.min(corner1.getBlockZ() / CHUNK_SIZE, corner2.getBlockZ() / CHUNK_SIZE) - BUFFER;
        this.x2 = Integer.max(corner1.getBlockX() / CHUNK_SIZE, corner2.getBlockX() / CHUNK_SIZE) + BUFFER;
        this.z2 = Integer.max(corner1.getBlockZ() / CHUNK_SIZE, corner2.getBlockZ() / CHUNK_SIZE) + BUFFER;

        this.total = (long) (x2 - x1 + 1) * (z2 - z1 + 1);

        this.x = x1;
        this.z = z1;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public ChunkCoordinate next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        final ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z);
        if (++z > z2) {
            z = z1;
            if (++x > x2) {
                hasNext = false;
            }
        }
        return chunkCoord;
    }

    public long total() {
        return total;
    }
}
