package com.extremelyd1.game.team;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;

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
            ChatColor.of(new Color(255, 0, 0)),
            // Blue
            ChatColor.of(new Color(0, 0, 255)),
            // Green
            ChatColor.of(new Color(0, 255, 0)),
            // Yellow
            ChatColor.of(new Color(255, 255, 0)),
            // Pink/Magenta
            ChatColor.of(new Color(240, 40, 240)),
            // Aqua
            ChatColor.of(new Color(0, 255, 255)),
            // Orange
            ChatColor.of(new Color(255, 128, 0)),
            // Gray
            ChatColor.of(new Color(128, 128, 128)),
    };

    public TeamFactory() {
        lastTeamIndex = -1;
    }

    /**
     * Creates a new team
     *
     * @return A new team
     */
    public Team createTeam() {
        lastTeamIndex++;

        return new Team(names[lastTeamIndex], colors[lastTeamIndex]);
    }

}
