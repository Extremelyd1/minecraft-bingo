package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeaveListener implements Listener {

    /**
     * The game instance
     */
    private final Game game;

    public PlayerJoinLeaveListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        if (game.isMaintenance()) {
            for (OfflinePlayer offlinePlayer : Bukkit.getOperators()) {
                if (e.getUniqueId().equals(offlinePlayer.getUniqueId())) {
                    return;
                }
            }

            e.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Game is currently in maintenance mode"
            );
            return;
        }

        if (!game.getState().equals(Game.State.PRE_GAME)) {
            for (Team team : game.getTeamManager().getTeams()) {
                for (Player player : team.getPlayers()) {
                    if (player.getUniqueId().equals(e.getUniqueId())) {
                        return;
                    }
                }
            }

            e.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Game is currently in progress"
            );
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.game.onPregameUpdate();

        Player player = e.getPlayer();

        if (game.getState().equals(Game.State.PRE_GAME)) {
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20D);
            player.setFoodLevel(20);
            player.setSaturation(5);

            Location spawnLocation = game.getWorldManager().getSpawnLocation();
            player.teleport(spawnLocation);
            player.setBedSpawnLocation(spawnLocation);
        }

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null) {
            e.setJoinMessage(
                    ChatColor.GREEN + "+ "
                            + player.getName()
                            + ChatColor.WHITE + " joined"
            );

            return;
        }

        e.setJoinMessage(
                ChatColor.GREEN + "+ "
                        + team.getColor() + player.getName()
                        + ChatColor.WHITE + " joined"
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent e) {
        this.game.onPregameUpdate();

        Player player = e.getPlayer();

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null) {
            e.setQuitMessage(
                    ChatColor.RED + "- "
                            + player.getName()
                            + ChatColor.WHITE + " left"
            );

            return;
        }

        e.setQuitMessage(
                ChatColor.RED + "- "
                        + team.getColor() + player.getName()
                        + ChatColor.WHITE + " left"
        );
    }

}
