package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.chat.ChatChannelController;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import java.util.Collections;

public class ChannelCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public ChannelCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String @NotNull [] args
    ) {
        if (!CommandUtil.checkCommandSender(sender, false, false)) {
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(sender, command);
            return true;
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
            sendUsage(sender, command);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String @NotNull [] args
    ) {
        if (!(sender instanceof Player) || args.length != 1) {
            return Collections.emptyList();
        }

        return Arrays.asList("team", "global");
    }

    /**
     * Send the usage of this command to the given sender
     * @param sender The sender to send the command to
     * @param command The command instance
     */
    private void sendUsage(CommandSender sender, Command command) {
        sender.sendMessage(Component
                .text("Usage: ")
                .color(NamedTextColor.DARK_RED)
                .append(Component
                        .text("/" + command.getName() + " <team|global>")
                        .color(NamedTextColor.WHITE)
                )
        );
    }
}
