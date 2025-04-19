package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.chat.ChatChannelController;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

import java.util.Collections;

@SuppressWarnings("UnstableApiUsage")
public class ChannelCommand implements BasicCommand {

    /**
     * The game instance.
     */
    private final Game game;

    public ChannelCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, false, false)) {
            return;
        }

        Player player = (Player) commandSourceStack.getSender();

        if (args.length == 0) {
            sendUsage(commandSourceStack);
            return;
        }

        try {
            ChatChannelController.ChatChannel channel = ChatChannelController.ChatChannel.valueOf(args[0].toUpperCase());
            game.getChatChannelController().setPlayerChatChannel(player, channel);
            player.sendMessage(ChatUtil.successPrefix().append(Component
                    .text("Updated chat channel to ")
                    .color(NamedTextColor.WHITE)
                    .append(Component
                            .text(channel.name())
                            .color(NamedTextColor.YELLOW)
                    )
            ));
        } catch (IllegalArgumentException ex) {
            sendUsage(commandSourceStack);
        }
    }

    @Override
    public @NotNull Collection<String> suggest(CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!(commandSourceStack.getSender() instanceof Player) || args.length != 1) {
            return Collections.emptyList();
        }

        return Arrays.asList("team", "global");
    }

    /**
     * Send the usage of this command to the given sender.
     * @param commandSourceStack The command source to send the usage to.
     */
    private void sendUsage(@NotNull CommandSourceStack commandSourceStack) {
        commandSourceStack.getSender().sendMessage(Component
                .text("Usage: ")
                .color(NamedTextColor.DARK_RED)
                .append(Component
                        .text("/channel <team|global>")
                        .color(NamedTextColor.WHITE)
                )
        );
    }
}
