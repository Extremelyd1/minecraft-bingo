package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.TeamManager;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class WinConditionCommand implements BasicCommand {

    /**
     * The game instance
     */
    private final Game game;

    public WinConditionCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, true, true)) {
            return;
        }

        CommandSender sender = commandSourceStack.getSender();

        if (!game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Can only execute this command in pre-game")
                    .color(NamedTextColor.WHITE)
            ));

            return;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Please provide a win condition type: 'full', 'lines' or 'lockout'")
                    .color(NamedTextColor.WHITE)
            ));

            return;
        }

        if (args[0].equalsIgnoreCase("full")) {
            game.getWinConditionChecker().setFullCard();

            Bukkit.broadcast(Component
                    .text("Full bingo card has been ")
                    .color(NamedTextColor.WHITE)
                    .append(Component
                            .text("enabled")
                            .color(NamedTextColor.GREEN)
                    )
            );

            game.onPregameUpdate();

            return;
        } else if (args[0].equalsIgnoreCase("lines")) {
            if (args.length < 2) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Please provide a number to indicate how many lines need to be completed")
                        .color(NamedTextColor.WHITE)
                ));

                return;
            }

            int numLines;
            try {
                numLines = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Could not parse arguments, please provide an integer")
                        .color(NamedTextColor.WHITE)
                ));

                return;
            }

            if (numLines < 1 || numLines > 10) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Number of lines to complete must be")
                        .color(NamedTextColor.WHITE)
                        .append(Component
                                .text(" between ")
                                .decorate(TextDecoration.BOLD)
                        ).append(Component
                                .text("0")
                                .color(NamedTextColor.YELLOW)
                        ).append(Component
                                .text(" and ")
                                .color(NamedTextColor.WHITE)
                        ).append(Component
                                .text("11")
                                .color(NamedTextColor.YELLOW)
                        )
                ));

                return;
            }

            game.getWinConditionChecker().setNumLinesToComplete(numLines);

            Bukkit.broadcast(Component
                    .text("Number of lines (rows, columns or diagonals) to achieve bingo has been set to ")
                    .color(NamedTextColor.WHITE)
                    .append(Component
                            .text(numLines)
                            .color(NamedTextColor.YELLOW)
                    )
            );

            game.onPregameUpdate();

            return;
        } else if (args[0].equalsIgnoreCase("lockout")) {
            int completionsToLock = 1;
            if (args.length > 1) {
                try {
                    completionsToLock = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatUtil.errorPrefix().append(Component
                            .text("Could not parse arguments, please provide an integer")
                            .color(NamedTextColor.WHITE)
                    ));

                    return;
                }
            }

            // Check whether the given value is within bounds
            if (completionsToLock < 1 || completionsToLock > TeamManager.MAX_TEAMS) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Lockout completions must be")
                        .color(NamedTextColor.WHITE)
                        .append(Component
                                .text(" between ")
                                .decorate(TextDecoration.BOLD)
                        ).append(Component
                                .text("0")
                                .color(NamedTextColor.YELLOW)
                        ).append(Component
                                .text(" and ")
                                .color(NamedTextColor.WHITE)
                        ).append(Component
                                .text("9")
                                .color(NamedTextColor.YELLOW)
                        )
                ));

                return;
            }

            game.getWinConditionChecker().setCompletionsToLock(completionsToLock);

            Component message = Component.text("Lockout has been ")
                    .append(Component
                            .text("enabled")
                            .color(NamedTextColor.GREEN)
                    ).append(Component
                            .text(", items will lock after ")
                            .color(NamedTextColor.WHITE)
                    ).append(Component
                            .text(completionsToLock)
                            .color(NamedTextColor.YELLOW)
                    );

            if (completionsToLock == 1) {
                message = message.append(Component
                        .text(" team has collected them")
                        .color(NamedTextColor.WHITE)
                );
            } else {
                message = message.append(Component
                        .text(" teams have collected them")
                        .color(NamedTextColor.WHITE)
                );
            }

            Bukkit.broadcast(message);

            game.onPregameUpdate();

            return;
        }

        sender.sendMessage(ChatUtil.errorPrefix().append(Component
                .text("Please provide a valid win condition type: 'full', 'lines' or 'lockout'")
                .color(NamedTextColor.WHITE)
        ));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!game.getState().equals(Game.State.PRE_GAME)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("full", "lines", "lockout");
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("lines")) {
                List<String> numLines = new ArrayList<>();
                for (int i = 1; i <= 10; i++) {
                    String s = String.valueOf(i);
                    if (s.startsWith(args[1])) {
                        numLines.add(s);
                    }
                }

                return numLines;
            } else if (args[0].equalsIgnoreCase("lockout")) {
                return CommandUtil.GetTabCompletionForNumTeams(args[1]);
            }
        }

        return Collections.emptyList();
    }
}
