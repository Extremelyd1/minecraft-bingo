package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AllCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public AllCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!CommandUtil.checkCommandSender(sender, false, false)) {
            return true;
        }

        Player player = (Player) sender;

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null) {
            player.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Cannot execute this command without a team"
            );

            return true;
        }

        if (args.length == 0) {
            player.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Please specify a message"
            );

            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String string : args) {
            message.append(" ").append(string);
        }

        Bukkit.broadcastMessage(
                team.getColor() + player.getName()
                        + ChatColor.WHITE + ":" + message
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
