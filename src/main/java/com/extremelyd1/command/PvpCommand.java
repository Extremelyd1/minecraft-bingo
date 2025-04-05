package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PvpCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public PvpCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String s, String
            @NotNull [] strings
    ) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        game.togglePvp();

        Component message = Component.text("PvP is now ");
        if (game.isPvpDisabled()) {
            message = message.append(Component
                    .text("disabled")
                    .color(NamedTextColor.DARK_RED)
            );
        } else {
            message = message.append(Component
                    .text("enabled")
                    .color(NamedTextColor.GREEN)
            );
        }

        Bukkit.broadcast(message);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        return Collections.emptyList();
    }
}
