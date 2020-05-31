package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemListener implements Listener {

    private final Game game;

    public ItemListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            e.setCancelled(true);
            return;
        }

        if (!e.getEntity().getType().equals(EntityType.PLAYER)) {
            return;
        }

        Material material = e.getItem().getItemStack().getType();
        Player player = (Player) e.getEntity();

        game.onMaterialCollected(player, material);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            e.setCancelled(true);
            return;
        }

        ItemMeta itemMeta = e.getItemDrop().getItemStack().getItemMeta();

        if (itemMeta != null && itemMeta.getDisplayName().contains("Bingo Card")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            e.setCancelled(true);
            return;
        }

        if (!(e.getWhoClicked() instanceof Player)) {
            e.setCancelled(true);
            return;
        }

        Player player = (Player) e.getWhoClicked();
        game.onMaterialCollected(player, e.getRecipe().getResult().getType());
    }

}
