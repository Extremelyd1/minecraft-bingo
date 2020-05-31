package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    private final Game game;

    public InteractListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        onBlock(e);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        onBlock(e);
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent e) {
        onBlock(e);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        onBlock(e);
    }

    public void onBlock(Event e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            if (e instanceof Cancellable) {
                ((Cancellable) e).setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            e.setCancelled(true);
        }
    }
}
