package com.extremelyd1.gameboard;

import com.extremelyd1.game.team.Team;
import com.extremelyd1.gameboard.boardEntry.BlankBoardEntry;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import com.extremelyd1.gameboard.boardEntry.DynamicBoardEntry;
import com.extremelyd1.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class IngameBoard extends GameBoard {

    private final Team team;

    private final DynamicBoardEntry<Integer> numItemsEntry;

    public IngameBoard(Team team) {
        super();

        this.team = team;

        this.objective.setDisplayName(
                ChatColor.BOLD.toString()
                        + ChatColor.YELLOW.toString()
                        + "Minecraft Bingo"
        );

        int numberOfSpaces = 1;
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));
        this.boardEntries.add(new BoardEntry(
                "Status: " + ChatColor.YELLOW + "In-Game"
        ));
        this.boardEntries.add(new BoardEntry(
                "Team: " + team.getColor() + team.getName()
        ));
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));
        this.boardEntries.add(new BoardEntry("Number of items collected:"));
        numItemsEntry = new DynamicBoardEntry<>(ChatColor.AQUA + "  %d", 0);
        this.boardEntries.add(numItemsEntry);
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces));
    }

    public void update() {
        super.update();

        numItemsEntry.setValue(numItemsEntry.getValue() + 1);
    }

    public void broadcast() {
        for (Player player : team.getPlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

}
