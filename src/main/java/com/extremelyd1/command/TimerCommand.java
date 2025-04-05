package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import com.extremelyd1.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TimerCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public TimerCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String @NotNull [] args
    ) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Please provide either 'enable/disable' or a time limit")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        if (!game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command now")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            game.getConfig().setTimerEnabled(true);

            Bukkit.broadcast(Component
                    .text("Game timer is now ")
                    .append(Component
                            .text("enabled")
                            .color(NamedTextColor.GREEN)
                    )
            );

            game.onPregameUpdate();

            return true;
        } else if (args[0].equalsIgnoreCase("disable")) {
            game.getConfig().setTimerEnabled(false);

            Bukkit.broadcast(Component
                    .text("Game timer is now ")
                    .append(Component
                            .text("disabled")
                            .color(NamedTextColor.DARK_RED)
                    )
            );

            game.onPregameUpdate();

            return true;
        }

        int timerLength;
        try {
            timerLength = Integer.parseInt(args[0]);
            trySetTimer(sender, timerLength);

            game.onPregameUpdate();

            return true;
        } catch (NumberFormatException ignored) {
            // Timer is not specified in a single integer
        }

        // Try to parse the argument as a length
        int parsedTime = TimeUtil.parseTimeArgument(args[0]);
        if (parsedTime == -1) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Could not parse timer length. ")
                    .color(NamedTextColor.WHITE)
                    .append(Component
                            .text("Example: ")
                            .color(NamedTextColor.YELLOW)
                    ).append(Component
                            .text("1h23m45s or 34m")
                            .color(NamedTextColor.WHITE)
                    )
            ));

            return true;
        }

        trySetTimer(sender, parsedTime);

        game.onPregameUpdate();

        return true;
    }

    private void trySetTimer(CommandSender sender, int seconds) {
        if (seconds < 1) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Timer length cannot be ")
                    .color(NamedTextColor.WHITE)
                    .append(Component
                            .text("negative")
                            .decorate(TextDecoration.BOLD)
                    )
            ));

            return;
        }

        game.getConfig().setTimerLength(seconds);

        Bukkit.broadcast(Component
                .text("Max timer length has been set to ")
                .append(Component
                        .text(TimeUtil.formatTimeLeft(seconds))
                        .color(NamedTextColor.YELLOW)
                )
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (!game.getState().equals(Game.State.PRE_GAME)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("enable", "disable");
        }

        return Collections.emptyList();
    }
}
