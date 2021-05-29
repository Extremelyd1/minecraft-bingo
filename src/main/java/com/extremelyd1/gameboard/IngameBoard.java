package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.gameboard.boardEntry.BlankBoardEntry;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import com.extremelyd1.gameboard.boardEntry.DynamicBoardEntry;
import com.extremelyd1.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Represents the in game scoreboard
 */
public class IngameBoard extends GameBoard {

    /**
     * The team this scoreboard belongs to
     */
    private final Team team;

    /**
     * The entry representing the number of items collected
     */
    private final DynamicBoardEntry<Integer> numItemsEntry;
    /**
     * The entry representing the timer
     */
    private final DynamicBoardEntry<String> timeLeftEntry;
    /**
     * The entry representing the currently winning team
     */
    private final DynamicBoardEntry<String> winningTeamEntry;

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

        this.boardEntries.add(new BoardEntry(
                "Game type: " + ChatColor.YELLOW + formatWinCondition(game.getWinConditionChecker())
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

        if (!team.isSpectatorTeam()) {
            this.boardEntries.add(new BoardEntry("Number of items collected:"));
            numItemsEntry = new DynamicBoardEntry<>(ChatColor.AQUA + "  %d", 0);
            this.boardEntries.add(numItemsEntry);
            this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));
        } else {
            numItemsEntry = null;
        }

        if (game.getConfig().showCurrentlyWinningTeam()) {
            this.boardEntries.add(new BoardEntry("Leading team:"));
            winningTeamEntry = new DynamicBoardEntry<>("  %s", ChatColor.GRAY + "Tie");
            this.boardEntries.add(winningTeamEntry);
            this.boardEntries.add(new BlankBoardEntry(numberOfSpaces));
        } else {
            winningTeamEntry = null;
        }
    }

    /**
     * Updates this board with a new number of items
     * @param numItems The new number of items
     */
    public void updateNumItems(int numItems) {
        numItemsEntry.setValue(numItems);

        super.update();
    }

    /**
     * Updates this board with the new time left
     * @param timeLeft The new time left
     */
    public void updateTime(long timeLeft) {
        timeLeftEntry.setValue(TimeUtil.formatTimeLeft(timeLeft));

        super.update();
    }

    /**
     * Updates this board with the currently winning team
     * @param team The currently winning team
     */
    public void updateWinningTeam(PlayerTeam team) {
        if (winningTeamEntry == null) {
            return;
        }

        if (team == null) {
            winningTeamEntry.setValue(ChatColor.GRAY + "Tie");
        } else {
            winningTeamEntry.setValue(team.getColor() + team.getName());
        }

        super.update();
    }

    /**
     * Broadcasts this board to all the team's members
     */
    public void broadcast() {
        for (Player player : team.getPlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

}
