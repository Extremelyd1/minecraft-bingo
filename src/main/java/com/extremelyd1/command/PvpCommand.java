package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PvpCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public PvpCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        game.togglePvp();

        if (!game.isPvpEnabled()) {
            Bukkit.broadcastMessage(
                    Game.PREFIX + "PVP is now " + ChatColor.DARK_RED + "disabled"
            );
        } else {
            Bukkit.broadcastMessage(
                    Game.PREFIX + "PVP is now " + ChatColor.GREEN + "enabled"
            );
        }

        return true;
    }
}
