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
            "White",
            "Gray"
    };

    /**
     * The colors of the teams
     */
    private final ChatColor[] colors = {
            ChatColor.RED,
            ChatColor.BLUE,
            ChatColor.GREEN,
            ChatColor.YELLOW,
            ChatColor.LIGHT_PURPLE,
            ChatColor.AQUA,
            ChatColor.WHITE,
            ChatColor.GRAY
    };

    public TeamFactory() {
        lastTeamIndex = -1;
    }

    /**
     * Creates a new team
     * @return A new team
     */
    public Team createTeam() {
        lastTeamIndex++;

        return new Team(names[lastTeamIndex], colors[lastTeamIndex]);
    }

}
