package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.gameboard.boardEntry.BlankBoardEntry;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import com.extremelyd1.gameboard.boardEntry.DynamicBoardEntry;
import com.extremelyd1.util.StringUtil;
import com.extremelyd1.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class IngameBoard extends GameBoard {

    private final Team team;

    private final DynamicBoardEntry<Integer> numItemsEntry;
    private final DynamicBoardEntry<String> timeLeftEntry;

    public IngameBoard(Game game, Team team) {
        super(game);

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

        if (game.getConfig().isTimerEnabled()) {
            timeLeftEntry = new DynamicBoardEntry<>(
                    "Time left: " + ChatColor.YELLOW + "%s",
                    "0:00"
            );
            this.boardEntries.add(timeLeftEntry);
        } else {
            timeLeftEntry = null;
        }

        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));
        this.boardEntries.add(new BoardEntry(
                "Team: " + team.getColor() + team.getName()
        ));
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));
        this.boardEntries.add(new BoardEntry("Number of items collected:"));
        numItemsEntry = new DynamicBoardEntry<>(ChatColor.AQUA + "  %d", 0);
        this.boardEntries.add(numItemsEntry);
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces));
    }

    public void update(int numItems) {
        numItemsEntry.setValue(numItems);

        super.update();
    }

    public void updateTime(long timeLeft) {
        timeLeftEntry.setValue(TimeUtil.formatTimeLeft(timeLeft));

        super.update();
    }

    public void broadcast() {
        for (Player player : team.getPlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

}
