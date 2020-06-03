package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class GameBoard {

    protected static int lastObjectiveId = 0;

    private final Game game;

    protected Scoreboard scoreboard;
    protected Objective objective;

    protected List<BoardEntry> boardEntries;

    public GameBoard(Game game) {
        this.game = game;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective(
                String.valueOf(lastObjectiveId++),
                "dummy",
                ""
        );
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.boardEntries = new ArrayList<>();
    }

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
                // Player is not in a team yet, skip coloring
                continue;
            }

            // Get scoreboard team with color enum name
            Team scoreboardTeam = scoreboard.getTeam(gameTeam.getColor().name());

            if (scoreboardTeam == null) {
                // Create new scoreboard team with color enum name
                scoreboardTeam = scoreboard.registerNewTeam(gameTeam.getColor().name());

                // Set prefix to color code
                scoreboardTeam.setPrefix(gameTeam.getColor().toString() + gameTeam.getName().toUpperCase() + " ");
            }

            // Add player name to this scoreboard team
            // This also removes the entry from all other teams it is on
            scoreboardTeam.addEntry(onlinePlayer.getName());
        }
    }

    public void broadcast() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

}
