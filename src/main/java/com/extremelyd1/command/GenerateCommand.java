package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class GenerateCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public GenerateCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        if (args.length < 1) {
            sendUsage(sender, command);

            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            game.getWorldManager().stopPreGeneration();

            return true;
        } else if (args.length < 2) {
            sendUsage(sender, command);

            return true;
        }

        int start;
        try {
            start = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sendUsage(sender, command);

            return true;
        }

        int numWorlds;
        try {
            numWorlds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendUsage(sender, command);

            return true;
        }

        sender.sendMessage(
                "Pre-generating " + numWorlds + " worlds..."
        );

        game.getWorldManager().createWorlds(start, numWorlds);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("stop");
        }

        return Collections.emptyList();
    }

    /**
     * Send the usage of this command to the given commandsender
     * @param sender The commandsender to send the usage to
     * @param command The command instance
     */
    private void sendUsage(CommandSender sender, Command command) {
        sender.sendMessage(
                ChatColor.DARK_RED
                        + "Usage: "
                        + ChatColor.WHITE
                        + "/"
                        + command.getName()
                        + " stop | <start> <numberOfWorlds>"
        );
    }
}
