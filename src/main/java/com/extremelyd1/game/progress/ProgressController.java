package com.extremelyd1.game.progress;

import com.extremelyd1.game.Game;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
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

    public ProgressController(FileConfiguration config) {
        linesRules = config.getStringList("notify-progress.lines");
        fullCardCountRules = config.getStringList("notify-progress.full-count");
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
}
