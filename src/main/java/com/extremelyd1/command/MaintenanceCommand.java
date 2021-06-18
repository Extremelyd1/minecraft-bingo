package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MaintenanceCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public MaintenanceCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        game.toggleMaintenance();

        if (!game.isMaintenance()) {
            Bukkit.broadcastMessage(
                    Game.PREFIX + "Maintenance is now " + ChatColor.DARK_RED + "disabled"
            );
        } else {
            Bukkit.broadcastMessage(
                    Game.PREFIX + "Maintenance is now " + ChatColor.GREEN + "enabled"
            );
        }

        return true;
    }
}
