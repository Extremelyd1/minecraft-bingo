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
            "Aqua",
            "Black",
            "Blue",
            "Dark_aqua",
            "Dark_blue",
            "Dark_gray",
            "Dark_green",
            "Dark_purple",
            "Dark_red",
            "Gold",
            "Gray",
            "Green",
            "Light_purple",
            "Red",
            "White",
            "Yellow",
    };

    /**
     * The colors of the teams
     */
    private final ChatColor[] colors = {
            ChatColor.AQUA,
            ChatColor.BLACK,
            ChatColor.BLUE,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GRAY,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_RED,
            ChatColor.GOLD,
            ChatColor.GRAY,
            ChatColor.GREEN,
            ChatColor.LIGHT_PURPLE,
            ChatColor.RED,
            ChatColor.WHITE,
            ChatColor.YELLOW,
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
