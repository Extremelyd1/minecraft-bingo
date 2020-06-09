package com.extremelyd1.game.team;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.BingoCardInventory;
import com.extremelyd1.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a list of players that are on the same team
 */
public class Team {

    /**
     * The name of the team
     */
    private final String name;
    /**
     * The color of the team
     */
    private final ChatColor color;
    /**
     * The list of players in this team, stored as UUIDs
     */
    private final List<UUID> uuids;

    /**
     * The bingo card associated with this team
     */
    private BingoCard bingoCard;
    /**
     * The bingo card inventory associated with this team
     */
    private BingoCardInventory bingoCardInventory;

    public Team(String name, ChatColor color) {
        this.name = name;
        this.color = color;

        this.uuids = new ArrayList<>();
    }

    /**
     * Add a player to this team
     * @param player The player to add
     */
    void addPlayer(Player player) {
        this.addPlayer(player, false);
    }

    /**
     * Add a player to this team, and whether to notify them of their new team
     * @param player The player to add
     * @param notify Whether to notify the player of their new team
     */
    void addPlayer(Player player, boolean notify) {
        this.uuids.add(player.getUniqueId());

        if (notify) {
            player.sendMessage(
                    Game.PREFIX +
                            "Joined "
                            + color + name
                            + ChatColor.WHITE + " team"
            );
        }
    }

    /**
     * Remove a player from this team
     * @param player The player to remove
     */
    void removePlayer(Player player) {
        this.uuids.remove(player.getUniqueId());
    }

    /**
     * Check whether the given player is on this team
     * @param player The player to check for
     * @return Whether to player is on this team
     */
    public boolean contains(Player player) {
        return this.uuids.contains(player.getUniqueId());
    }

    /**
     * Removes all players from this team
     */
    public void clear() {
        uuids.clear();
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    /**
     * Gets player objects in this team by retrieving them by UUID,
     * does not return any null objects
     * @return An iterable of Player instances
     */
    public Iterable<Player> getPlayers() {
        return () -> uuids.stream()
                // Filter out players that are not valid anymore
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                // Map everything to their respective Player instance
                .map(Bukkit::getPlayer)
                // Return an iterator
                .iterator();
    }

    public Iterable<UUID> getUUIDs() {
        return uuids;
    }

    /**
     * Gets the number of players on this team
     * @return The number of players on this team
     */
    public int getNumPlayers() {
        return uuids.size();
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
}
