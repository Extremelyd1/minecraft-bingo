package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoinCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public JoinCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!CommandUtil.checkCommandSender(sender, false)) {
            return true;
        }

        if (args.length <= 0) {
            sendUsage(sender, command);
            return true;
        }

        PlayerTeam checkTeam = game.getTeamManager().getTeamByName(args[0]);
        if (checkTeam != null && sender instanceof Player) {
            game.getTeamManager().addPlayerToTeam((Player) sender, checkTeam);
            game.onPregameUpdate();
        } else {
            sender.sendMessage(
                    ChatColor.DARK_RED
                            + "Error: "
                            + ChatColor.WHITE
                            + "Cannot find team for color "
                            + args[0]
            );
        }

        return true;
    }

    /**
     * Send the usage of this command to the given sender
     * @param sender The sender to send the command to
     * @param command The command instance
     */
    private void sendUsage(CommandSender sender, Command command) {
        sender.sendMessage(
                ChatColor.DARK_RED
                        + "Usage: "
                        + ChatColor.WHITE
                        + "/"
                        + command.getName()
                        + " <team color>"
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> teams = new ArrayList<>();

        if (args.length > 1) return teams;

        for (PlayerTeam team: game.getTeamManager().getAvailableTeams()) {
            if (team.getName().toLowerCase().contains(args[0].toLowerCase())) {
                teams.add(team.getName());
            }
        }

        return teams;
    }
}
