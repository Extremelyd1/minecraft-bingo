package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    /**
     * The game instance.
     */
    private final Game game;

    public MoveListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (game.getState().equals(Game.State.PRE_GAME) && !game.isMaintenance()) {
            Location spawnLocation = game.getWorldManager().getSpawnLocation();
            if (e.getTo().distance(spawnLocation) > game.getConfig().getPreGameBorderRadius()) {
                e.getPlayer().sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("You cannot move away from the spawn in the pre-game")
                        .color(NamedTextColor.WHITE)
                ));
                e.getPlayer().teleport(spawnLocation);
            }
        }
    }

}
