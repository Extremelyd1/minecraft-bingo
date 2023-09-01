package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemDistributionCommand implements TabExecutor {

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
                        + ChatColor.YELLOW + "2 6 9 6 2"
                        + ChatColor.WHITE + " (S A B C D)"
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        // If we have more than 5 arguments, we don't suggest anymore
        if (args.length > 5) {
            return list;
        }

        // If we only have 1 argument, we give the option of 0 through 25
        if (args.length == 1) {
            for (int i = 0; i < 26; i++) {
                String s = String.valueOf(i);
                if (s.startsWith(args[0])) {
                    list.add(s);
                }
            }

            return list;
        }

        // Int to keep track of total number of items that have been given in previous arguments
        int total = 0;

        // We loop over the arguments 2 through 5
        for (int i = 2; i < 6; i++) {
            // If at least argument i has been supplied
            if (args.length >= i) {
                // Figure out the integer value of the number of items for that tier
                String tierString = args[i - 2];
                int numTier;
                try {
                    numTier = Integer.parseInt(tierString);
                } catch (NumberFormatException e) {
                    // If we can't parse any previously supplied argument, there's no point in suggesting
                    // other arguments
                    return Collections.emptyList();
                }

                // Add the previous argument to the total
                total += numTier;
                if (total < 0 || total > 25) {
                    // If the total is out of the range of 0 through 25, we don't suggest anything
                    return Collections.emptyList();
                }

                // If i is the last argument that the user has supplied
                if (args.length == i) {
                    // Calculate how many items we can still put in the distribution and suggest those
                    int leftInDist = 25 - total;
                    for (int j = 0; j <= leftInDist; j++) {
                        String s = String.valueOf(j);
                        if (s.startsWith(args[i - 1])) {
                            list.add(s);
                        }
                    }

                    return list;
                }
            }
        }

        // This should never execute, but return the list anyway
        return list;
    }
}
