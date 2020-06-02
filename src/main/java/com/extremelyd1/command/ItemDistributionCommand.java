package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ItemDistributionCommand implements CommandExecutor {

    private final Game game;

    public ItemDistributionCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!CommandUtil.checkCommandSender(sender)) {
            return true;
        }

        if (args.length == 0) {
            sendItemDistributionError(sender);
            return true;
        }

        if (!args[0].contains(",")) {
            sendItemDistributionError(sender);
            return true;
        }

        String[] itemDistribution = args[0].split(",");

        if (itemDistribution.length != 5) {
            sendItemDistributionError(sender);
            return true;
        }

        int numSTierItems = parseItemDistribution(itemDistribution[0], sender);
        int numATierItems = parseItemDistribution(itemDistribution[1], sender);
        int numBTierItems = parseItemDistribution(itemDistribution[2], sender);
        int numCTierItems = parseItemDistribution(itemDistribution[3], sender);
        int numDTierItems = parseItemDistribution(itemDistribution[4], sender);

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
                ChatColor.GREEN + "Successfully "
                        + ChatColor.WHITE + " set item distribution to "
                        + ChatColor.YELLOW
                        + numSTierItems + " "
                        + numATierItems + " "
                        + numBTierItems + " "
                        + numCTierItems + " "
                        + numDTierItems + " "
                        + ChatColor.WHITE + "(S, A, B, C, D)"
        );

        return true;
    }

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

    private void sendItemDistributionError(CommandSender sender) {
        sender.sendMessage(
                ChatColor.DARK_RED + "Error: "
                        + ChatColor.WHITE + "Please provide a valid item distribution"
        );
        sender.sendMessage(
                ChatColor.BLUE + "Example: "
                        + ChatColor.WHITE + "2,6,9,6,2"
        );
    }
}
