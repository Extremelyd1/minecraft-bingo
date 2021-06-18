package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RerollCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public RerollCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        if (!game.getState().equals(Game.State.IN_GAME)) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Can only use this command in game"
            );

            return true;
        }

        game.rerollCard();

        sender.sendMessage(
                Game.PREFIX + "Rerolled bingo card"
        );

        return true;
    }
}
