package com.extremelyd1.config;

import org.bukkit.plugin.java.JavaPlugin;

public class Config {

    private final boolean enableBlacklist;

    private int numSTierItems;
    private int numATierItems;
    private int numBTierItems;
    private int numCTierItems;
    private int numDTierItems;

    private final int defaultNumLinesComplete;

    private final boolean borderEnabled;
    private final int borderSize;

    private boolean timerEnabled;
    private long timerLength;

    public Config(JavaPlugin plugin) {
        plugin.saveDefaultConfig();

        enableBlacklist = plugin.getConfig().getBoolean("enableBlacklist");
        String defaultItemDistributionString = plugin.getConfig().getString("defaultItemDistribution");

        if (defaultItemDistributionString == null
                || !defaultItemDistributionString.contains(",")) {
            throw new IllegalArgumentException("Default item distribution config value is not parsable");
        }

        String[] itemDistributions = defaultItemDistributionString.split(",");
        if (itemDistributions.length != 5) {
            throw new IllegalArgumentException("Default item distribution config value does not have 5 items");
        }

        numSTierItems = parseItemDistribution(itemDistributions[0]);
        numATierItems = parseItemDistribution(itemDistributions[1]);
        numBTierItems = parseItemDistribution(itemDistributions[2]);
        numCTierItems = parseItemDistribution(itemDistributions[3]);
        numDTierItems = parseItemDistribution(itemDistributions[4]);

        defaultNumLinesComplete = plugin.getConfig().getInt("defaultBingoLinesCompleteForWin");

        borderEnabled = plugin.getConfig().getBoolean("border.enable");
        borderSize = plugin.getConfig().getInt("border.size");

        timerEnabled = plugin.getConfig().getBoolean("timer.enable");
        timerLength = plugin.getConfig().getInt("timer.length");
    }

    private int parseItemDistribution(String stringValue) {
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Default item distribution config value has a non-integer value");
        }
    }

    public boolean isBlacklistEnabled() {
        return enableBlacklist;
    }

    public int getNumSTier() {
        return numSTierItems;
    }

    public int getNumATier() {
        return numATierItems;
    }

    public int getNumBTier() {
        return numBTierItems;
    }

    public int getNumCTier() {
        return numCTierItems;
    }

    public int getNumDTier() {
        return numDTierItems;
    }

    public void setItemDistribution(
            int numSTierItems,
            int numATierItems,
            int numBTierItems,
            int numCTierItems,
            int numDTierItems
    ) {
       this.numSTierItems = numSTierItems;
       this.numATierItems = numATierItems;
       this.numBTierItems = numBTierItems;
       this.numCTierItems = numCTierItems;
       this.numDTierItems = numDTierItems;
    }

    public int getDefaultNumLinesComplete() {
        return defaultNumLinesComplete;
    }

    public boolean isBorderEnabled() {
        return borderEnabled;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public long getTimerLength() {
        return timerLength;
    }

    public void setTimerLength(long timerLength) {
        this.timerLength = timerLength;
    }
}
