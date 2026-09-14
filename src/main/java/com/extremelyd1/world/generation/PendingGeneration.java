package com.extremelyd1.world.generation;

import org.bukkit.World;
import org.jspecify.annotations.NonNull;

/**
 * @param index       The index of this world
 * @param environment The type of environment of this world
 */
public record PendingGeneration(int index, World.Environment environment) {

    public @NonNull String toString() {
        return environment.toString() + index;
    }
}
