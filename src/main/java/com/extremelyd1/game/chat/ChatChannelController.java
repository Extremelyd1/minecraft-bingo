package com.extremelyd1.game.chat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ChatChannelController {

    public enum ChatChannel {
        TEAM(), GLOBAL()
    }

    private static final ChatChannel DEFAULT_CHANNEL = ChatChannel.TEAM;

    public Map<String, ChatChannel> playerChatChannels = new HashMap<>();

    public void setPlayerChatChannel(Player player, ChatChannel channel) {
        playerChatChannels.put(player.getUniqueId().toString(), channel);
    }

    public ChatChannel getPlayerChatChannel(Player player) {
        ChatChannel channel = playerChatChannels.get(player.getUniqueId().toString());
        if (channel == null) {
            return DEFAULT_CHANNEL;
        } else {
            return channel;
        }
    }
}
