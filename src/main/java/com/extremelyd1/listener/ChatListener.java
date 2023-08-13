package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.chat.ChatChannelController;
import com.extremelyd1.game.team.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_20_R1.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
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
        // This code is based on AdvancementDataPlayer in CraftBukkit net/minecraft/server/AdvancementDataPlayer.java
        // Get the NMS advancement
        Advancement advancement = ((CraftAdvancement) e.getAdvancement()).getHandle();

        DisplayInfo displayInfo = advancement.getDisplay();
        // Skip if there is no display info or this advancement shouldn't be announced to chat
        if (displayInfo == null || !displayInfo.shouldAnnounceChat()) {
            return;
        }

        Player player = e.getPlayer();
        // Get the team for the color
        Team team = game.getTeamManager().getTeamByPlayer(player);
        // Create NMS chat component with translation key
        MutableComponent mutableComponent = Component.translatable(
                "chat.type.advancement." + displayInfo.getFrame().getName(),
                Component.literal(player.getDisplayName()).setStyle(
                        Style.EMPTY.withColor(ChatFormatting.valueOf(team.getColor().name()))
                ),
                advancement.getChatComponent()
        );
        // Send the message to all players
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) onlinePlayer).getHandle().sendSystemMessage(mutableComponent);
        }
    }

}
