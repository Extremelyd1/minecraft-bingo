package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamLocationCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public TeamLocationCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute this command as console");
            return true;
        }

        Player player = (Player) sender;

        Team team = game.getTeamManager().getTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.WHITE + "You aren't on a team");
        } else {
            String message = ChatColor.GREEN + "X > " + ChatColor.RESET
                    + (int) player.getLocation().getX()
                    + ChatColor.GREEN + " Y > " + ChatColor.RESET
                    + (int) player.getLocation().getY()
                    + ChatColor.GREEN + " Z > " + ChatColor.RESET
                    + (int) player.getLocation().getZ();

            for (Player p : team.getPlayers()) {
                p.sendMessage(team.getColor() + "TEAM " + player.getName() + ChatColor.WHITE + ": " + message);
            }
        }

        return true;
    }
}
