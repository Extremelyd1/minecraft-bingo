package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.chat.ChatChannelController;
import com.extremelyd1.game.team.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.entity.CraftPlayer;
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
    public void onAsyncPlayerChat(@SuppressWarnings("deprecation") AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Team team = game.getTeamManager().getTeamByPlayer(player);
        ChatChannelController.ChatChannel chatChannel = game.getChatChannelController().getPlayerChatChannel(player);
        if (team == null) {
            Bukkit.broadcast(Component
                    .text(player.getName() + ": " + e.getMessage())
            );
        } else if (chatChannel == ChatChannelController.ChatChannel.GLOBAL) {
            Bukkit.broadcast(Component
                    .text(player.getName())
                    .color(team.getColor())
                    .append(Component
                            .text(": " + e.getMessage())
                            .color(NamedTextColor.WHITE)
                    )
            );
        } else {
            for (Player teamPlayer : team.getPlayers()) {
                teamPlayer.sendMessage(Component
                        .text("TEAM " + player.getName())
                        .color(team.getColor())
                        .append(Component
                                .text(": " + e.getMessage())
                                .color(NamedTextColor.WHITE)
                        )
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

        // Get the NMS ChatFormatting color for the component
        ChatFormatting chatFormatting = ChatFormatting.getByHexValue(team.getColor().value());
        if (chatFormatting == null) {
            Game.getLogger().warning("Could not find matching ChatFormatting for team color: " + team.getColor().value());
            return;
        }

        // Get the component for formatting the advancement message
        net.minecraft.network.chat.Component playerNameComponent = serverPlayer.getName().copy().withStyle(chatFormatting);

        // Create NMS chat component with translation key
        MutableComponent mutableComponent = net.minecraft.network.chat.Component.translatable(
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
