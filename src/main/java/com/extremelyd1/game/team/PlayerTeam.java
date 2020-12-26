package com.extremelyd1.game.team;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.BingoCardInventory;
import com.extremelyd1.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a list of players that are on the same team
 */
public class PlayerTeam extends Team {

    /**
     * The bingo card associated with this team
     */
    private BingoCard bingoCard;
    /**
     * The bingo card inventory associated with this team
     */
    private BingoCardInventory bingoCardInventory;

    /**
     * The spawn location of the team
     */
    private Location spawnLocation;

    public PlayerTeam(String name, ChatColor color) {
        super(name, color, false);
    }

    public void setBingoCard(BingoCard bingoCard) {
        this.bingoCard = bingoCard;
        // Reset the bingo card inventory to create a new one when requested
        this.bingoCardInventory = null;
    }

    public BingoCard getBingoCard() {
        return bingoCard;
    }

    /**
     * Gets the bingo card inventory of this team or creates one if it does not exist
     * @return The bingo card inventory of this team
     */
    public BingoCardInventory getBingoCardInventory() {
        if (bingoCardInventory == null) {
            if (bingoCard == null) {
                return null;
            }

            bingoCardInventory = new BingoCardInventory(bingoCard);
        }
        return bingoCardInventory;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}
