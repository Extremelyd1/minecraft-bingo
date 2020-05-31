package com.extremelyd1.gameboard;

import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class GameBoard {

    protected static int lastObjectiveId = 0;

    protected Scoreboard scoreboard;
    protected Objective objective;

    protected List<BoardEntry> boardEntries;

    public GameBoard() {
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
    }

    public void broadcast() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

}
