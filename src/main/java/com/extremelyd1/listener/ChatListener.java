package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.chat.ChatChannelController;
import com.extremelyd1.game.team.Team;
import net.minecraft.EnumChatFormat;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_17_R1.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class ChatListener implements Listener {

    /**
     * The game instance
     */
    private final Game game;

    public ChatListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Team team = game.getTeamManager().getTeamByPlayer(player);
        ChatChannelController.ChatChannel chatChannel = game.getChatChannelController().getPlayerChatChannel(player);
        if (team == null) {
            Bukkit.broadcastMessage(
                    player.getName() + ": " + e.getMessage()
            );
        } else if (chatChannel == ChatChannelController.ChatChannel.GLOBAL) {
            Bukkit.broadcastMessage(
                team.getColor() + player.getName() + ChatColor.RESET + ": " + e.getMessage()
            );
        } else {
            for (Player teamPlayer : team.getPlayers()) {
                teamPlayer.sendMessage(
                        team.getColor() + "TEAM "
                                + player.getName()
                                + ChatColor.WHITE + ": "
                                + e.getMessage()
                );
            }
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        // Get the NMS advancement
        Advancement advancement = ((CraftAdvancement) e.getAdvancement()).getHandle();

        // c() = the AdvancementDisplay corresponding to this advancement
        // c().i() = A boolean in AdvancementDisplay (probably something to do with whether to display it
        if (advancement.c() != null && advancement.c().i()) {
            // Get the type of advancement from the AdvancementDisplay
            Player player = e.getPlayer();
            // Get the team for the color
            Team team = game.getTeamManager().getTeamByPlayer(player);
            // Create NMS chat message with translation key
            ChatMessage message = new ChatMessage(
                    "chat.type.advancement." + advancement.c().e().a(),
                    // Color the player name with the color of their team
                    new ChatComponentText(player.getName()).a(EnumChatFormat.valueOf(team.getColor().name())),
                    // Get the display component of the Advancement
                    advancement.j()
            );
            // Send the message to all players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) onlinePlayer).getHandle().sendMessage(null, new ChatMessage[] { message });
            }
        }
    }

}
