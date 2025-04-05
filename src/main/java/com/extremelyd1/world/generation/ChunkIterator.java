package com.extremelyd1.world.generation;

import org.bukkit.Location;
import org.bukkit.WorldBorder;

import java.util.NoSuchElementException;

/**
 * Class that iterates over ChunkCoordinates belonging to all the chunks within a world border.
 */
public class ChunkIterator {
    /**
     * The minimum x coordinate of the corner chunks.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int x1;
    /**
     * The maximum x coordinate of the corner chunk.
     */
    private final int x2;
    /**
     * The minimum z coordinate of the corner chunk.
     */
    private final int z1;
    /**
     * The maximum z coordinate of the corner chunk.
     */
    private final int z2;

    /**
     * The total number of chunks that this iterator will iterate over.
     */
    private final long total;
    /**
     * The current x coordinate in the iterator.
     */
    private int x;
    /**
     * The current z coordinate in the iterator.
     */
    private int z;
    /**
     * Whether the iterator has a next item.
     */
    private boolean hasNext = true;

    /**
     * Constructs the iterator that will iterator over all the chunks within the given world border.
     *
     * @param worldBorder The world border with the chunks.
     */
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

    /**
     * Whether the iterator has a next value.
     *
     * @return True if there is a next value, false otherwise.
     */
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * Get the next ChunkCoordinate from the iterator. Throws a NoSuchElementException if the iterator does not have
     * a next value.
     *
     * @return The next ChunkCoordinate in the iterator.
     */
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

    /**
     * The total number of chunks that this iterator will iterate over.
     *
     * @return The number of chunks.
     */
    public long total() {
        return total;
    }
}
