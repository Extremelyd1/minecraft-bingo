package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a scoreboard with GameBoardEntry instances in it
 */
public class GameBoard {

    /**
     * The last id of a created objective
     * Makes sure that there are no collisions between objective names
     */
    protected static int lastObjectiveId = 0;

    /**
     * The game instance
     */
    private final Game game;

    /**
     * The Scoreboard instance of the game board
     */
    protected Scoreboard scoreboard;
    /**
     * The Objective instance of this Scoreboard
     */
    protected Objective objective;

    /**
     * The list of entries on this board
     */
    protected List<BoardEntry> boardEntries;

    public GameBoard(Game game) {
        this.game = game;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective(
                String.valueOf(lastObjectiveId++),
                "dummy",
                "scoreboard"
        );
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.boardEntries = new ArrayList<>();
    }

    /**
     * Updates this board by updating all board entries and resetting the prefixes of players
     */
    public void update() {
        // Clear all existing scores
        for (String entry : this.scoreboard.getEntries()) {
            this.scoreboard.resetScores(entry);
        }

        // Add the new scores
        for (int i = 0; i < boardEntries.size(); i++) {
            this.objective.getScore(boardEntries.get(i).getString()).setScore(boardEntries.size() - i);
        }

        // Update team colors
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            com.extremelyd1.game.team.Team gameTeam = game.getTeamManager().getTeamByPlayer(onlinePlayer);
            if (gameTeam == null) {
                // Player is not in a team, remove from existing teams
                Team scoreboardTeam = scoreboard.getEntryTeam(onlinePlayer.getName());

                if (scoreboardTeam != null) {
                    scoreboardTeam.removeEntry(onlinePlayer.getName());
                }

                continue;
            }

            // Get scoreboard team with color enum name
            Team scoreboardTeam = scoreboard.getTeam(gameTeam.getColor().name());

            if (scoreboardTeam == null) {
                // Create new scoreboard team with color enum name
                scoreboardTeam = scoreboard.registerNewTeam(gameTeam.getColor().name());

                // Set prefix to color code
                scoreboardTeam.setColor(gameTeam.getColor());
            }

            // Add player name to this scoreboard team
            // This also removes the entry from all other teams it is on
            scoreboardTeam.addEntry(onlinePlayer.getName());
        }
    }

    /**
     * Broadcasts this board to all online players
     */
    public void broadcast() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

}
