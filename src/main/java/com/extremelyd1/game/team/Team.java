package com.extremelyd1.game.team;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.BingoCardInventory;
import com.extremelyd1.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
     * The list of players in this team
     */
    private final List<Player> players;

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

        this.players = new ArrayList<>();
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
        this.players.add(player);

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
        this.players.remove(player);
    }

    /**
     * Check whether the given player is on this team
     * @param player The player to check for
     * @return Whether to player is on this team
     */
    public boolean contains(Player player) {
        return this.players.contains(player);
    }

    /**
     * Removes all players from this team
     */
    public void clear() {
        players.clear();
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public Iterable<Player> getPlayers() {
        return players;
    }

    /**
     * Gets the number of players on this team
     * @return The number of players on this team
     */
    public int getNumPlayers() {
        return players.size();
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
