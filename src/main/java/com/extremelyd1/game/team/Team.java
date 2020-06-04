package com.extremelyd1.game.team;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.BingoCardInventory;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private final String name;
    private final ChatColor color;
    private final List<Player> players;

    private BingoCard bingoCard;
    private BingoCardInventory bingoCardInventory;

    public Team(String name, ChatColor color) {
        this.name = name;
        this.color = color;

        this.players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setBingoCard(BingoCard bingoCard) {
        this.bingoCard = bingoCard;
    }

    public BingoCard getBingoCard() {
        return bingoCard;
    }

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
