package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamChatCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public TeamChatCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, false, false)) {
            return true;
        }

        Player player = (Player) sender;

        Team team = game.getTeamManager().getTeamByPlayer(player);

        String message = String.join(" ", strings);

        if (team == null) {
            player.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.WHITE + "You are not on a team");
        } else {
            for (Player p : team.getPlayers()) {
                p.sendMessage(team.getColor() + "TEAM " + player.getName() + ChatColor.WHITE + ": " + message);
            }
        }

        return true;
    }
}
