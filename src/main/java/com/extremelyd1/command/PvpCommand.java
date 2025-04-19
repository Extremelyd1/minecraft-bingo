package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class PvpCommand implements BasicCommand {

    /**
     * The game instance
     */
    private final Game game;

    public PvpCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, true, true)) {
            return;
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
    }
}
