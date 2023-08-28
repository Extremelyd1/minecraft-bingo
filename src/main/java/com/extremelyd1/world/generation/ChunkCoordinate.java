package com.extremelyd1.world.generation;

import java.util.Objects;

/**
 * Record class containing the x and z coordinate of a chunk.
 *
 * @param x The x coordinate of the chunk.
 * @param z The z coordinate of the chunk.
 */
public record ChunkCoordinate(int x, int z) implements Comparable<ChunkCoordinate> {
    @Override
    public int compareTo(final ChunkCoordinate o) {
        return this.x == o.x ? Integer.compare(this.z, o.z) : Integer.compare(this.x, o.x);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ChunkCoordinate that = (ChunkCoordinate) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return String.format("%d, %d", x, z);
    }
}
