package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class StartCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public StartCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        if (game.getState().equals(Game.State.IN_GAME)) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "cannot start game, it already has been started"
            );

            return true;
        }

        if (sender instanceof Player) {
            game.start((Player) sender);
        } else {
            game.start();
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
