package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class RerollCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public RerollCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String @NotNull [] strings) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        if (!game.getState().equals(Game.State.IN_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Can only use this command in-game")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        game.rerollCard();

        sender.sendMessage(ChatUtil.successPrefix().append(Component
                .text("Rerolled bingo card")
                .color(NamedTextColor.WHITE)
        ));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return Collections.emptyList();
    }
}
