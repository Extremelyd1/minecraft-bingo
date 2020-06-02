package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final Game game;

    public ChatListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null) {
            Bukkit.broadcastMessage(
                    player.getName() + ": " + e.getMessage()
            );
        } else {
            Bukkit.broadcastMessage(
                    team.getColor() + player.getName()
                            + ChatColor.WHITE + ": " + e.getMessage()
            );
        }

        e.setCancelled(true);
    }

}
