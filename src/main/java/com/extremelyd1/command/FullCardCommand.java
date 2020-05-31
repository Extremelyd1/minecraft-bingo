package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FullCardCommand implements CommandExecutor {

    private final Game game;

    public FullCardCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender)) {
            return true;
        }

        game.toggleFullCard();

        if (!game.isFullCard()) {
            Bukkit.broadcastMessage(
                    Game.PREFIX + "Full bingo card is now " + ChatColor.DARK_RED + "disabled"
            );
        } else {
            Bukkit.broadcastMessage(
                    Game.PREFIX + "Full bingo card is now " + ChatColor.GREEN + "enabled"
            );
        }

        return true;
    }
}
