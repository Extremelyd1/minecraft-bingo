package com.extremelyd1.game.progress;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.winCondition.WinConditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

import static com.extremelyd1.game.Game.PREFIX;

public class ProgressController {

    /**
     * Rules for lines notifications
     */
    private final List<String> linesRules;

    /**
     * Rules for full card count notifications
     */
    private final List<String> fullCardCountRules;

    /**
     * Rules for lockout gamemode
     */
    private final List<String> lockoutRules;

    public ProgressController(FileConfiguration config) {
        linesRules = config.getStringList("notify-progress.lines");
        fullCardCountRules = config.getStringList("notify-progress.full-card");
        lockoutRules = config.getStringList("notify-progress.lockout");
    }

    public void onCollection(Game game, PlayerTeam collectorTeam, int linesCompletedBefore) {
        WinConditionChecker winConditionChecker = game.getWinConditionChecker();

        if (winConditionChecker.isFullCard()) {
            if (shouldNotifyCountAmount(collectorTeam.getNumCollected())) {
                Bukkit.broadcastMessage(
                        PREFIX +
                                collectorTeam.getColor() + collectorTeam.getName()
                                + ChatColor.WHITE + " team has collected "
                                + ChatColor.AQUA + collectorTeam.getNumCollected() + ChatColor.WHITE + " items"
                );
            }
        } else if (winConditionChecker.getNumLinesToComplete() > 0) {
            int linesCompletedNow = game.getBingoCard().getNumLinesComplete(collectorTeam);

            if (linesCompletedNow > linesCompletedBefore) {
                if (shouldNotifyProgressLines(linesCompletedNow)) {
                    Bukkit.broadcastMessage(
                            PREFIX +
                                    collectorTeam.getColor() + collectorTeam.getName()
                                    + ChatColor.WHITE + " team has completed "
                                    + ChatColor.AQUA + linesCompletedNow + ChatColor.WHITE + " lines"
                    );
                }
            }
        } else if (winConditionChecker.getCompletionsToLock() > 0) {
            if (shouldNotifyLockoutAmount(collectorTeam.getNumCollected())) {
                Bukkit.broadcastMessage(
                        PREFIX +
                                collectorTeam.getColor() + collectorTeam.getName()
                                + ChatColor.WHITE + " team has collected "
                                + ChatColor.AQUA + collectorTeam.getNumCollected() + ChatColor.WHITE + " items"
                );
            }
        }
    }

    private boolean notifyEachLine() {
        return linesRules.contains("each");
    }

    public boolean shouldNotifyProgressLines(int linesComplete) {
        return linesRules.contains(String.valueOf(linesComplete)) || notifyEachLine();
    }

    public boolean shouldNotifyCountAmount(int itemsCollected) {
        return fullCardCountRules.contains(String.valueOf(itemsCollected));
    }

    public boolean shouldNotifyLockoutAmount(int itemsCollected) {
        return lockoutRules.contains(String.valueOf(itemsCollected));
    }
}
