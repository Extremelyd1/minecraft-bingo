package com.extremelyd1.game.team;

import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 * Represents a list of players that are on the same team
 */
public class PlayerTeam extends Team {

    /**
     * The number of items this team has collected
     */
    private int numCollected;

    /**
     * The spawn location of the team
     */
    private Location spawnLocation;

    public PlayerTeam(String name, ChatColor color) {
        super(name, color, false);
    }

    public void incrementCollected() {
        ++numCollected;
    }

    public int getNumCollected() {
        return numCollected;
    }

    public void setNumCollected(int numCollected) {
        this.numCollected = numCollected;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}
