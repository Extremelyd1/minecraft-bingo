package com.extremelyd1.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtil {

    /**
     * Checks whether the command sender has permission to execute a command
     * @param sender The sender to check
     * @return True if the sender is a player and has OP rights; False otherwise
     */
    public static boolean checkCommandSender(CommandSender sender) {
        return checkCommandSender(sender, false, true);
    }

    /**
     * Checks whether the command sender has permission to execute a command
     * @param sender The sender to check
     * @param allowConsole Whether to allow console to execute
     * @param needsOp Whether the player needs to be OP to execute
     * @return True if the sender is allowed to execute the given command
     */
    public static boolean checkCommandSender(CommandSender sender, boolean allowConsole, boolean needsOp) {
        if (!(sender instanceof Player)) {
            if (!allowConsole) {
                sender.sendMessage("This command can only be executed as a player");

                return false;
            } else {
                return true;
            }
        }

        if (!needsOp) {
            return true;
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
