package com.extremelyd1.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtil {

    /**
     * Checks whether the command sender has permission to execute a command
     * @param sender The sender to check
     * @return True if the sender is a player and has OP rights; False otherwise
     */
    public static boolean checkCommandSender(CommandSender sender) {
        return checkCommandSender(sender, true);
    }

    /**
     * Checks whether the command sender has permission to execute a command
     * @param sender The sender to check
     * @param noConsole Whether to allow console to execute
     * @return True if the sender has OP rights, or is the console and console is allowed; False otherwise
     */
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
