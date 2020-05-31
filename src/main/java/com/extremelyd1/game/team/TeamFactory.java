package com.extremelyd1.game.team;

import org.bukkit.ChatColor;

public class TeamFactory {

    private int lastTeamIndex;

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

    public Team createTeam() {
        lastTeamIndex++;

        return new Team(names[lastTeamIndex], colors[lastTeamIndex]);
    }

}
