package com.extremelyd1.game.progress;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ProgressController {

    /**
     * Rules for lines notifications
     */
    private List<String> linesRules;

    /**
     * Rules for full card count notifications
     */
    private List<String> fullCardCountRules;

    /**
     * Rules for lockout gamemode
     */
    private List<String> lockoutRules;

    public ProgressController(FileConfiguration config) {
        linesRules = config.getStringList("notify-progress.lines");
        fullCardCountRules = config.getStringList("notify-progress.full-card");
        lockoutRules = config.getStringList("notify-progress.lockout");
    }

    private Boolean notifyEachLine() {
        return linesRules.contains("each");
    }

    public Boolean shouldNotifyProgressLines(int linesComplete) {
        return linesRules.contains(String.valueOf(linesComplete)) || notifyEachLine();
    }

    public Boolean shouldNotifyCountAmount(int itemsCollected) {
        return fullCardCountRules.contains(String.valueOf(itemsCollected));
    }

    public Boolean shouldNotifyLockoutAmount(int itemsCollected) {
        return lockoutRules.contains(String.valueOf(itemsCollected));
    }
}
