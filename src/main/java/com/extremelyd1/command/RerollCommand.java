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

@SuppressWarnings("UnstableApiUsage")
public class RerollCommand implements BasicCommand {

    /**
     * The game instance
     */
    private final Game game;

    public RerollCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, true, true)) {
            return;
        }

        CommandSender sender = commandSourceStack.getSender();

        if (!game.getState().equals(Game.State.IN_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Can only use this command in-game")
                    .color(NamedTextColor.WHITE)
            ));

            return;
        }

        game.rerollCard();

        sender.sendMessage(ChatUtil.successPrefix().append(Component
                .text("Rerolled bingo card")
                .color(NamedTextColor.WHITE)
        ));
    }
}
