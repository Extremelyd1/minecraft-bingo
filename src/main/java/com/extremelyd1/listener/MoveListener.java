package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    /**
     * The maximum distance a player can move away from the spawn in pregame
     */
    private final static float SPAWN_AREA = 20;

    /**
     * The game instance
     */
    private final Game game;

    public MoveListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (game.getState().equals(Game.State.PRE_GAME) && !game.isMaintenance()) {
            // Sanity check
            if (e.getTo() == null) {
                return;
            }

            Location spawnLocation = game.getWorldManager().getSpawnLocation();
            if (e.getTo().distance(spawnLocation) > SPAWN_AREA) {
                e.getPlayer().sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "You cannot move away from the spawn in the pre-game"
                );
                e.getPlayer().teleport(spawnLocation);
            }
        }
    }

}
