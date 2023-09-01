package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;


public class CoordinatesCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public CoordinatesCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!CommandUtil.checkCommandSender(sender, false, false)) {
            return true;
        }

        if (game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Cannot execute this command in pregame"
            );
            return true;
        }

        Player player = (Player) sender;

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null || team.isSpectatorTeam()) {
            player.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Cannot execute this command as spectator"
            );

            return true;
        }

        Location location = player.getLocation();

        // Either empty or the text that the player sends after the command
        String description = "";
        if (args.length > 0) {
            StringBuilder descBuilder = new StringBuilder();
            for (String arg : args) {
                descBuilder.append(" ").append(arg);
            }

            description = descBuilder.toString();
        }

        for (Player teamPlayer : team.getPlayers()) {
            teamPlayer.sendMessage(
                    team.getColor() + "TEAM "
                            + player.getName()
                            + ChatColor.WHITE + ": "
                            + ChatColor.AQUA + "["
                            + Math.round(location.getX()) + ", "
                            + Math.round(location.getY()) + ", "
                            + Math.round(location.getZ()) + "]"
                            + ChatColor.WHITE + description
            );
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
