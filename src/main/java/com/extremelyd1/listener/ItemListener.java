package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemListener implements Listener {

    /**
     * The game instance
     */
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

    /**
     * Caters for collecting milk, water or lava
     *
     * @param e - PlayerBucketFillEvent
     */
    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            e.setCancelled(true);
            return;
        }

        game.onMaterialCollected(e.getPlayer(), e.getItemStack().getType());
    }

    /**
     * This event caters for the collection of fishes into buckets, and the collection of mushroom stew from mooshrooms
     *
     * @param e - PlayerInteractAtEntityEvent
     */
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (e.isCancelled()) return;

        Map<EntityType, Material> fishes = new HashMap<EntityType, Material>() {{
           put(EntityType.SALMON, Material.SALMON_BUCKET);
           put(EntityType.COD, Material.COD_BUCKET);
           put(EntityType.PUFFERFISH, Material.PUFFERFISH_BUCKET);
           put(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH_BUCKET);
        }};

        Map<EntityType, Material> foodHarvesting = new HashMap<EntityType, Material>() {{
           put(EntityType.MUSHROOM_COW, Material.MUSHROOM_STEW);
        }};

        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.WATER_BUCKET ||
                e.getPlayer().getInventory().getItemInOffHand().getType() == Material.WATER_BUCKET) {
            Material collectedMaterial = fishes.get(e.getRightClicked().getType());
            if (collectedMaterial != null) game.onMaterialCollected(e.getPlayer(), collectedMaterial);
            return;
        }

        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.BOWL ||
                e.getPlayer().getInventory().getItemInOffHand().getType() == Material.BOWL) {
            Material collectedMaterial = foodHarvesting.get(e.getRightClicked().getType());
            if (collectedMaterial != null) game.onMaterialCollected(e.getPlayer(), collectedMaterial);
            return;
        }
    }

}
