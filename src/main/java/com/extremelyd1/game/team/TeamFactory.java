package com.extremelyd1.game.team;

import org.bukkit.ChatColor;

/**
 * Factory class that creates team instances
 */
public class TeamFactory {

    /**
     * The index of the last team created
     */
    private int lastTeamIndex;

    /**
     * The names of the teams
     */
    private final String[] names = {
            "Red",
            "Blue",
            "Green",
            "Yellow",
            "Pink",
            "Aqua",
            "Orange",
            "Gray"
    };

    /**
     * The colors of the teams
     */
    private final ChatColor[] colors = {
            // Red
            ChatColor.RED,
//            ChatColor.of(new Color(255, 0, 0)),
            // Blue
            ChatColor.BLUE,
//            ChatColor.of(new Color(0, 0, 255)),
            // Green
            ChatColor.GREEN,
//            ChatColor.of(new Color(0, 255, 0)),
            // Yellow
            ChatColor.YELLOW,
//            ChatColor.of(new Color(255, 255, 0)),
            // Pink/Magenta
            ChatColor.LIGHT_PURPLE,
//            ChatColor.of(new Color(240, 40, 240)),
            // Aqua
            ChatColor.AQUA,
//            ChatColor.of(new Color(0, 255, 255)),
            // Orange
            ChatColor.GOLD,
//            ChatColor.of(new Color(255, 128, 0)),
            // Gray
            ChatColor.GRAY
//            ChatColor.of(new Color(128, 128, 128)),
    };

    public TeamFactory() {
        lastTeamIndex = -1;
    }

    /**
     * Creates a new team
     *
     * @return A new team
     */
    public PlayerTeam createTeam() {
        lastTeamIndex++;

        return new PlayerTeam(names[lastTeamIndex], colors[lastTeamIndex]);
    }

}
