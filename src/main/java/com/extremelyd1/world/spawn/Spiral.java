package com.extremelyd1.world.spawn;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.LocationUtil;
import net.minecraft.server.v1_16_R1.EnumDirection;
import org.bukkit.Location;

public class Spiral {

    /**
     * The center location of the spiral
     */
    private final Location center;

    /**
     * The current location of the spiral
     */
    private final Location location;
    /**
     * The current step size
     * How many steps are taking in a direction until the direction is switched
     */
    private int stepSize;
    /**
     * The current direction to take steps in
     */
    private EnumDirection direction;
    /**
     * The current width of the spiral
     */
    private int currentWidth;

    /**
     * The number of steps since the direction has been switched
     */
    private int stepCounter;

    public Spiral(Location center) {
        this.center = center;

        this.location = LocationUtil.copyLocation(center);
        this.stepSize = 1;
        this.direction = EnumDirection.NORTH;
        this.currentWidth = 1;

        this.stepCounter = 0;

        Game.getLogger().info("Starting spiral search at location: " + this.center);
    }

    /**
     * Makes a step on the spiral and returns a copy of the resulting location
     * @return A copy of the resulting location
     */
    public Location step() {
        switch (direction) {
            case NORTH:
                location.add(0, 0, -1);
                break;
            case EAST:
                location.add(1, 0, 0);
                break;
            case SOUTH:
                location.add(0, 0, 1);
                break;
            case WEST:
                location.add(-1, 0, 0);
                break;
        }

        this.stepCounter++;
        // If we have stepped the step size in the given direction,
        // switch directions (and increase step size)
        if (this.stepCounter >= this.stepSize) {
            this.direction = nextDirection(this.direction);
            // Change the step size after switching to south or north
            // And increase the spiral width
            if (this.direction.equals(EnumDirection.SOUTH)
                    || this.direction.equals(EnumDirection.NORTH)) {
                this.stepSize++;
                this.currentWidth++;
            }

            this.stepCounter = 0;
        }

        return LocationUtil.copyLocation(this.location);
    }

    public Location getCenter() {
        return center;
    }

    public int getCurrentWidth() {
        return currentWidth;
    }

    /**
     * Gets the next direction based on clockwise direction movement
     * @param direction The current direction
     * @return The next direction
     */
    private static EnumDirection nextDirection(EnumDirection direction) {
        switch (direction) {
            case NORTH:
                return EnumDirection.EAST;
            case EAST:
                return EnumDirection.SOUTH;
            case SOUTH:
                return EnumDirection.WEST;
            case WEST:
                return EnumDirection.NORTH;
        }

        // Fallback values just in case
        return EnumDirection.NORTH;
    }

}
