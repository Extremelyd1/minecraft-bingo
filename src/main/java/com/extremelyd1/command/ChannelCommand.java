package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.chat.ChatChannelController;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

public class ChannelCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public ChannelCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute this command as console");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(sender, command);
            return true;
        }

        ChatChannelController.ChatChannel channel = ChatChannelController.ChatChannel.valueOf(args[0].toUpperCase());

        if (channel != null) {
            game.getChatChannelController().setPlayerChatChannel(player, channel);
            player.sendMessage("Successfully updated chat channel to " + ChatColor.GREEN + channel.name());
        } else {
            sendUsage(sender, command);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            if (!sender.isOp()) return emptyList();
        }

        return Arrays.asList("team", "global");
    }

    /**
     * Send the usage of this command to the given sender
     * @param sender The sender to send the command to
     * @param command The command instance
     */
    private void sendUsage(CommandSender sender, Command command) {
        sender.sendMessage(
                ChatColor.DARK_RED
                        + "Usage: "
                        + ChatColor.WHITE
                        + "/"
                        + command.getName()
                        + " <team|global>"
        );
    }
}
