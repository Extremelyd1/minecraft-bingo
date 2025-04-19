package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemDistributionCommand implements BasicCommand {

    /**
     * The game instance.
     */
    private final Game game;

    public ItemDistributionCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, true, true)) {
            return;
        }

        CommandSender sender = commandSourceStack.getSender();

        if (args.length != 5) {
            sendItemDistributionError(sender);
            return;
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
            return;
        }

        if (numSTierItems + numATierItems + numBTierItems + numCTierItems + numDTierItems != 25) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("The distribution must add up to 25 in total")
                    .color(NamedTextColor.WHITE)
            ));

            return;
        }

        game.getConfig().setItemDistribution(
                numSTierItems,
                numATierItems,
                numBTierItems,
                numCTierItems,
                numDTierItems
        );

        sender.sendMessage(ChatUtil.successPrefix().append(Component
                .text("Set item distribution to ")
                .color(NamedTextColor.WHITE)
                .append(Component
                        .text(numSTierItems + " " +
                                numATierItems + " " +
                                numBTierItems + " " +
                                numCTierItems + " " +
                                numDTierItems
                        ).color(NamedTextColor.YELLOW)
                ).append(Component
                        .text(" (S, A, B, C, D)")
                        .color(NamedTextColor.WHITE)
                )
        ));

        game.onPregameUpdate();
    }

    /**
     * Parse the item in the given string, or send an error message to the given command sender.
     * @param item The item in string format.
     * @param sender The sender to send the error to.
     * @return The parsed integer of this string value or -1 if it cannot be parsed.
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
     * Sends the item distribution error message to the sender.
     * @param sender The command sender to send the error message to.
     */
    private void sendItemDistributionError(CommandSender sender) {
        sender.sendMessage(ChatUtil.errorPrefix().append(Component
                .text("Please provide a valid item distribution")
                .color(NamedTextColor.WHITE)
                .appendNewline().append(Component
                        .text("Example: ")
                        .color(NamedTextColor.BLUE)
                ).append(Component
                        .text("2 6 9 6 2")
                        .color(NamedTextColor.YELLOW)
                ).append(Component
                        .text(" (S A B C D)")
                        .color(NamedTextColor.WHITE)
                )
        ));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String[] args) {
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
            // If at least argument 'i' has been supplied
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

                // If 'i' is the last argument that the user has supplied
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
