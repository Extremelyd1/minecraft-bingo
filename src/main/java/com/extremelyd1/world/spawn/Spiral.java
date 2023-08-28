package com.extremelyd1.world.spawn;

import org.bukkit.Location;

public class Spiral {

    /**
     * The center chunk coords of the spiral
     */
    private final WorldChunkCoordinate center;

    /**
     * The current chunk coords of the spiral
     */
    private final WorldChunkCoordinate currentChunk;
    /**
     * The current step size
     * How many steps are taking in a direction until the direction is switched
     */
    private int stepSize;
    /**
     * The current direction to take steps in
     */
    private Direction direction;
    /**
     * The current width of the spiral
     */
    private int currentWidth;
    /**
     * The number of iterations this spiral has done
     */
    private int numIterations;

    /**
     * The number of steps since the direction has been switched
     */
    private int stepCounter;

    public Spiral(Location center) {
        this(new WorldChunkCoordinate(center));
    }

    public Spiral(WorldChunkCoordinate center) {
        this.center = center;

        this.currentChunk = center.copy();
        this.stepSize = 1;
        this.direction = Direction.NORTH;
        this.currentWidth = 1;

        this.stepCounter = 0;

        this.numIterations = 0;
    }

    /**
     * Makes a step on the spiral and returns a copy of the resulting chunk coords
     * @return A copy of the resulting chunk coords
     */
    public WorldChunkCoordinate step() {
        switch (direction) {
            case NORTH:
                this.currentChunk.add(0, -1);
                break;
            case EAST:
                this.currentChunk.add(1, 0);
                break;
            case SOUTH:
                this.currentChunk.add(0, 1);
                break;
            case WEST:
                this.currentChunk.add(-1, 0);
                break;
        }

        this.stepCounter++;
        // If we have stepped the step size in the given direction,
        // switch directions (and increase step size)
        if (this.stepCounter >= this.stepSize) {
            this.direction = nextDirection(this.direction);
            // Change the step size after switching to south or north
            // And increase the spiral width
            if (this.direction.equals(Direction.SOUTH)
                    || this.direction.equals(Direction.NORTH)) {
                this.stepSize++;
                this.currentWidth++;
            }

            this.stepCounter = 0;
        }

        this.numIterations++;

        return this.currentChunk;
    }

    public WorldChunkCoordinate getCenter() {
        return center;
    }

    public int getCurrentWidth() {
        return currentWidth;
    }

    public int getNumIterations() {
        return numIterations;
    }

    /**
     * Gets the next direction based on clockwise direction movement
     * @param direction The current direction
     * @return The next direction
     */
    private static Direction nextDirection(Direction direction) {
        switch (direction) {
            case NORTH:
                return Direction.EAST;
            case EAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.WEST;
            case WEST:
                return Direction.NORTH;
        }

        // Fallback values just in case
        return Direction.NORTH;
    }

    private enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

}
