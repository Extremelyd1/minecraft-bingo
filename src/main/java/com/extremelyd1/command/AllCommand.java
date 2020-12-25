package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public AllCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute this command as console");
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
}
