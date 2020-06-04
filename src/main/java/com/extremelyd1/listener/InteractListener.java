package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

            return;
        }

        Action action = e.getAction();
        if (!action.equals(Action.RIGHT_CLICK_AIR)
                && !action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (!e.hasItem()) {
            return;
        }

        ItemStack itemStack = e.getItem();
        if (!itemStack.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (!meta.getDisplayName().contains("Bingo Card")) {
            return;
        }

        Team team = game.getTeamManager().getTeamByPlayer(e.getPlayer());
        if (team == null) {
            return;
        }

        team.getBingoCardInventory().show(e.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("Bingo Card")) {
            event.setCancelled(true);
        }
    }
}
