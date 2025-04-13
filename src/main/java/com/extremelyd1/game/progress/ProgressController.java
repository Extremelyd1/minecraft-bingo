package com.extremelyd1.game.progress;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.winCondition.WinConditionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

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
     * Rules for lockout game mode
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
                Bukkit.broadcast(Component
                        .text(collectorTeam.getName())
                        .color(collectorTeam.getColor())
                        .append(Component
                                .text(" team has collected ")
                                .color(NamedTextColor.WHITE)
                        ).append(Component
                                .text(collectorTeam.getNumCollected())
                                .color(NamedTextColor.AQUA)
                        ).append(Component
                                .text(" items")
                                .color(NamedTextColor.WHITE)
                        )
                );
            }
        } else if (winConditionChecker.getNumLinesToComplete() > 0) {
            int linesCompletedNow = game.getBingoCard().getNumLinesComplete(collectorTeam);

            if (linesCompletedNow > linesCompletedBefore) {
                if (shouldNotifyProgressLines(linesCompletedNow)) {
                    Bukkit.broadcast(Component
                            .text(collectorTeam.getName())
                            .color(collectorTeam.getColor())
                            .append(Component
                                    .text(" team has completed ")
                                    .color(NamedTextColor.WHITE)
                            ).append(Component
                                    .text(linesCompletedNow)
                                    .color(NamedTextColor.AQUA)
                            ).append(Component
                                    .text(" lines")
                                    .color(NamedTextColor.WHITE)
                            )
                    );
                }
            }
        } else if (winConditionChecker.getCompletionsToLock() > 0) {
            if (shouldNotifyLockoutAmount(collectorTeam.getNumCollected())) {
                Bukkit.broadcast(Component
                        .text(collectorTeam.getName())
                        .color(collectorTeam.getColor())
                        .append(Component
                                .text(" team has collected ")
                                .color(NamedTextColor.WHITE)
                        ).append(Component
                                .text(collectorTeam.getNumCollected())
                                .color(NamedTextColor.AQUA)
                        ).append(Component
                                .text(" items")
                                .color(NamedTextColor.WHITE)
                        )
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
