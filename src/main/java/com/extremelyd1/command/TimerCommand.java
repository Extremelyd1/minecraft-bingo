package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import com.extremelyd1.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Please provide either 'enable/disable' or a max time length in seconds"
            );

            return true;
        }

        if (!game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(
                    ChatColor.DARK_RED
                            + "Error: "
                            + ChatColor.WHITE
                            + "Cannot execute this command now"
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            game.getConfig().setTimerEnabled(true);

            Bukkit.broadcastMessage(
                    Game.PREFIX + "Game timer is now " + ChatColor.GREEN + "enabled"
            );

            game.onPregameUpdate();

            return true;
        } else if (args[0].equalsIgnoreCase("disable")) {
            game.getConfig().setTimerEnabled(false);

            Bukkit.broadcastMessage(
                    Game.PREFIX + "Game timer is now " + ChatColor.DARK_RED + "disabled"
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
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Could not parse timer length. "
                            + ChatColor.YELLOW + "Example: "
                            + ChatColor.WHITE + "1h23m45s or 34m"
            );

            return true;
        }

        trySetTimer(sender, parsedTime);

        game.onPregameUpdate();

        return true;
    }

    private void trySetTimer(CommandSender sender, int seconds) {
        if (seconds < 1) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Timer length cannot be "
                            + ChatColor.BOLD + "negative"
            );

            return;
        }

        game.getConfig().setTimerLength(seconds);

        Bukkit.broadcastMessage(
                Game.PREFIX + "Max timer length has been set to "
                        + ChatColor.YELLOW + TimeUtil.formatTimeLeft(seconds)
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!game.getState().equals(Game.State.PRE_GAME)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("enable", "disable");
        }

        return Collections.emptyList();
    }
}
