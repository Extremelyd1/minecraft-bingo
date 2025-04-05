package com.extremelyd1.game.chat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatChannelController {

    private static final ChatChannel DEFAULT_CHANNEL = ChatChannel.TEAM;

    public final Map<UUID, ChatChannel> playerChatChannels = new HashMap<>();

    public void setPlayerChatChannel(Player player, ChatChannel channel) {
        playerChatChannels.put(player.getUniqueId(), channel);
    }

    public ChatChannel getPlayerChatChannel(Player player) {
        if (playerChatChannels.containsKey(player.getUniqueId())) {
            ChatChannel channel = playerChatChannels.get(player.getUniqueId());

            if (channel != null) {
                return channel;
            }
        }

        return DEFAULT_CHANNEL;
    }

    public enum ChatChannel {
        TEAM,
        GLOBAL
    }
}
