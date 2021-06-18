package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ItemDistributionCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public ItemDistributionCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        if (args.length != 5) {
            sendItemDistributionError(sender);
            return true;
        }

        int numSTierItems = parseItemDistribution(args[0], sender);
        int numATierItems = parseItemDistribution(args[1], sender);
        int numBTierItems = parseItemDistribution(args[2], sender);
        int numCTierItems = parseItemDistribution(args[3], sender);
        int numDTierItems = parseItemDistribution(args[4], sender);

        if (numSTierItems == -1
                || numATierItems == -1
                || numBTierItems == -1
                || numCTierItems == -1
                || numDTierItems == -1) {
            return true;
        }

        if (numSTierItems + numATierItems + numBTierItems + numCTierItems + numDTierItems != 25) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "The distribution must add up to 25 in total"
            );

            return true;
        }

        game.getConfig().setItemDistribution(
                numSTierItems,
                numATierItems,
                numBTierItems,
                numCTierItems,
                numDTierItems
        );

        sender.sendMessage(
                ChatColor.GREEN + "Successfully"
                        + ChatColor.WHITE + " set item distribution to "
                        + ChatColor.YELLOW
                        + numSTierItems + " "
                        + numATierItems + " "
                        + numBTierItems + " "
                        + numCTierItems + " "
                        + numDTierItems + " "
                        + ChatColor.WHITE + "(S, A, B, C, D)"
        );

        game.onPregameUpdate();

        return true;
    }

    /**
     * Parse the item in the given string, or send an error message to the given command sender
     * @param item The item in string format
     * @param sender The sender to send the error to
     * @return The parsed integer of this string value or -1 if it cannot be parsed
     */
    private int parseItemDistribution(String item, CommandSender sender) {
        int value;

        try {
            value = Integer.parseInt(item);
        } catch (NumberFormatException e) {
            sendItemDistributionError(sender);
            return -1;
        }

        return value;
    }

    /**
     * Sends the item distribution error message to the sender
     * @param sender The command sender to send the error message to
     */
    private void sendItemDistributionError(CommandSender sender) {
        sender.sendMessage(
                ChatColor.DARK_RED + "Error: "
                        + ChatColor.WHITE + "Please provide a valid item distribution"
        );
        sender.sendMessage(
                ChatColor.BLUE + "Example: "
                        + ChatColor.WHITE + "2 6 9 6 2"
        );
    }
}
