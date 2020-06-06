package com.extremelyd1.world;

import com.extremelyd1.config.Config;
import org.bukkit.*;

public class WorldManager {

    /**
     * The overworld world instance
     */
    private World world;
    /**
     * The nether world instance
     */
    private World nether;
    /**
     * The end world instance
     */
    private World end;

    public WorldManager(Config config) {
        if (Bukkit.getWorlds().size() > 3) {
            throw new IllegalStateException("There is not support for more than 3 worlds");
        }

        for (World world : Bukkit.getWorlds()) {
            switch (world.getEnvironment()) {
                case NORMAL:
                    this.world = world;
                    break;
                case NETHER:
                    this.nether = world;
                    break;
                case THE_END:
                    this.end = world;
                    break;
            }
        }

        if (world == null) {
            throw new IllegalStateException("There is not overworld loaded");
        }

        world.setAutoSave(false);
        if (nether != null) {
            nether.setAutoSave(false);
        }
        if (end != null) {
            end.setAutoSave(false);
        }

        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(0);

        if (config.isBorderEnabled()) {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(world.getSpawnLocation());
            border.setSize(config.getBorderSize());
        }
    }

    /**
     * Resets the gamerules of the overworld to default vanilla behaviour
     */
    public void onGameStart() {
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
    }

    /**
     * Loads the chunk at the given location
     * @param location The location to load the chunk at
     */
    public void loadChunkAt(Location location) {
        world.getChunkAt(location).load();
    }

    /**
     * Gets the spawn location of the overworld
     * @return The spawn location
     */
    public Location getSpawnLocation() {
        return world.getSpawnLocation();
    }
}
