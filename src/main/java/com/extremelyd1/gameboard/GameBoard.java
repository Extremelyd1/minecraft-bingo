package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.winCondition.WinConditionChecker;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a scoreboard with GameBoardEntry instances in it.
 */
public class GameBoard {

    /**
     * The last id of a created objective.
     * Makes sure that there are no collisions between objective names.
     */
    protected static int lastObjectiveId = 0;

    /**
     * The game instance.
     */
    private final Game game;

    /**
     * The Scoreboard instance of the game board.
     */
    protected Scoreboard scoreboard;
    /**
     * The Objective instance of this Scoreboard.
     */
    protected Objective objective;

    /**
     * The list of entries on this board.
     */
    protected List<BoardEntry> boardEntries;

    public GameBoard(Game game) {
        this.game = game;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective(
                String.valueOf(lastObjectiveId++),
                Criteria.DUMMY,
                Component.text("Minecraft Bingo")
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
        );
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.boardEntries = new ArrayList<>();
    }

    /**
     * Updates this board by updating all board entries and resetting the prefixes of players.
     * The following steps are taken to update the board efficiently.
     * - Remove scores that no longer exist in the board.
     * - Update the board to the most recent scores which will add new values/update existing ones.
     */
    public void update() {
        // Remove entries that no longer exist
        for (String entry : this.scoreboard.getEntries()) {
            boolean entryStillExists = false;

            Team team = this.scoreboard.getEntryTeam(entry);
            if (team != null) {
                Component prefix = team.prefix();
                for (BoardEntry newEntry : boardEntries) {
                    if (newEntry.getComponent().equals(prefix)) {
                        entryStillExists = true;
                        break;
                    }
                }
            }

            if (!entryStillExists) {
                this.scoreboard.resetScores(entry);
            }
        }

        // Add the new scores/update existing ones
        for (int i = 0; i < boardEntries.size(); i++) {
            Team team = this.scoreboard.getTeam(String.valueOf(i));
            if (team == null) {
                team = this.scoreboard.registerNewTeam(String.valueOf(i));
            }

            String entry = spacedString(i);

            team.addEntry(entry);
            team.prefix(boardEntries.get(i).getComponent());

            this.objective.getScore(entry).setScore(boardEntries.size() - i);
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
            Team scoreboardTeam = scoreboard.getTeam(gameTeam.getName());

            if (scoreboardTeam == null) {
                // Create new scoreboard team with color enum name
                scoreboardTeam = scoreboard.registerNewTeam(gameTeam.getName());

                // Set prefix to color code
                scoreboardTeam.color(gameTeam.getColor());
            }

            // Add player name to this scoreboard team
            // This also removes the entry from all other teams it is on
            scoreboardTeam.addEntry(onlinePlayer.getName());
        }
    }

    /**
     * Broadcasts this board to all online players.
     */
    public void broadcast() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    /**
     * Format the win condition of the given WinConditionChecker to a human-readable form.
     * @param winConditionChecker The class containing the win condition.
     * @return A human-readable string representing the win condition.
     */
    protected String formatWinCondition(WinConditionChecker winConditionChecker) {
        int completionsToLock = winConditionChecker.getCompletionsToLock();
        if (completionsToLock > 0) {
            // Special case for lockout with one completion to lock, which is a bit cleaner
            if (completionsToLock == 1) {
                return "Lockout";
            }

            return String.format("Lockout (%d)", winConditionChecker.getCompletionsToLock());
        }

        if (winConditionChecker.isFullCard()) {
            return "Full Card";
        }

        // Handle plurality of 'lines'
        int numLines = winConditionChecker.getNumLinesToComplete();
        return numLines + " Line" + (numLines == 1 ? "" : "s");
    }

    /**
     * Get a string consisting of spaces with the given number of spaces.
     * @param numberOfSpaces The number of spaces in the string.
     * @return A string consisting of spaces.
     */
    public static String spacedString(int numberOfSpaces) {
        return " ".repeat(Math.max(0, numberOfSpaces));
    }
}
