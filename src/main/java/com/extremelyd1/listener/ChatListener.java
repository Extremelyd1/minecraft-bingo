package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.chat.ChatChannelController;
import com.extremelyd1.game.team.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Optional;

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
            Bukkit.getServer().broadcastMessage(
                    player.getName() + ": " + e.getMessage()
            );
        } else if (chatChannel == ChatChannelController.ChatChannel.GLOBAL) {
            Bukkit.getServer().broadcastMessage(
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
        AdvancementHolder advancementHolder = ((CraftAdvancement) e.getAdvancement()).getHandle();
        Advancement advancement = advancementHolder.value();

        // Skip if there is no display info or this advancement shouldn't be announced to chat
        if (advancement.display().isEmpty() || !advancement.display().get().shouldAnnounceChat()) {
            return;
        }

        DisplayInfo displayInfo = advancement.display().get();

        Player player = e.getPlayer();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        // Get the team for the color
        Team team = game.getTeamManager().getTeamByPlayer(player);

        // Get the component for formatting the advancement message either from the display name with team color
        // or, as a fallback, the player name
        Component playerNameComponent;
        if (serverPlayer.getDisplayName() != null) {
            playerNameComponent = serverPlayer.getDisplayName().copy().withStyle(ChatFormatting.valueOf(team.getColor().name()));
        } else {
            playerNameComponent = serverPlayer.getName();
        }

        // Create NMS chat component with translation key
        MutableComponent mutableComponent = Component.translatable(
                "chat.type.advancement." + displayInfo.getType().getSerializedName(),
                playerNameComponent,
                Advancement.name(advancementHolder)
        );

        // Send the message to all players
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) onlinePlayer).getHandle().sendSystemMessage(mutableComponent);
        }
    }

}
