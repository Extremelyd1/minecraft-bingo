package com.extremelyd1.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtil {

    public static boolean checkCommandSender(CommandSender sender) {
        return checkCommandSender(sender, true);
    }

    public static boolean checkCommandSender(CommandSender sender, boolean noConsole) {
        if (!(sender instanceof Player)) {
            if (noConsole) {
                sender.sendMessage("This command can only be executed as a player");

                return false;
            } else {
                return true;
            }
        }

        Player player = (Player) sender;
        // TODO: implement better permissions system
        if (!player.isOp()) {
            player.sendMessage(
                    ChatColor.DARK_RED + "Error: " + ChatColor.WHITE + "No permission"
            );

            return false;
        }

        return true;
    }

}
