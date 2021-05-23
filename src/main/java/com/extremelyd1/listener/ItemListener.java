package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

    private static final Map<EntityType, Material> FISH_TYPE_TO_MATERIAL = new HashMap<EntityType, Material>() {{
        put(EntityType.SALMON, Material.SALMON_BUCKET);
        put(EntityType.COD, Material.COD_BUCKET);
        put(EntityType.PUFFERFISH, Material.PUFFERFISH_BUCKET);
        put(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH_BUCKET);
    }};

    private static final Map<EntityType, Material> FOOD_TYPE_TO_MATERIAL = new HashMap<EntityType, Material>() {{
        put(EntityType.MUSHROOM_COW, Material.MUSHROOM_STEW);
    }};

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
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            e.setCancelled(true);
            return;
        }

        if (e.getItemStack() != null) {
            game.onMaterialCollected(e.getPlayer(), e.getItemStack().getType());
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        // We are only interested in this event if we are in-game
        if (!game.getState().equals(Game.State.IN_GAME)) {
            return;
        }

        PlayerInventory playerInventory = e.getPlayer().getInventory();

        if (playerInventory.getItemInMainHand().getType() == Material.WATER_BUCKET
                || playerInventory.getItemInOffHand().getType() == Material.WATER_BUCKET) {
            EntityType entityType = e.getRightClicked().getType();

            if (FISH_TYPE_TO_MATERIAL.containsKey(entityType)) {
                game.onMaterialCollected(
                        e.getPlayer(),
                        FISH_TYPE_TO_MATERIAL.get(entityType)
                );
            }

            return;
        }

        if (playerInventory.getItemInMainHand().getType() == Material.BOWL || playerInventory.getItemInOffHand().getType() == Material.BOWL) {
            EntityType entityType = e.getRightClicked().getType();

            if (FOOD_TYPE_TO_MATERIAL.containsKey(entityType)) {
                game.onMaterialCollected(
                        e.getPlayer(),
                        FOOD_TYPE_TO_MATERIAL.get(entityType)
                );
            }

            return;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            return;
        }

        if (e.getClickedBlock() == null) {
            return;
        }

        Block clickedBlock = e.getClickedBlock();
        Material clickedType = clickedBlock.getType();

        // We are only interested in Beehives or Bee nests
        if (!clickedType.equals(Material.BEE_NEST) && !clickedType.equals(Material.BEEHIVE)) {
            return;
        }

        // Check whether the honey level is at the maximum value
        Beehive beehive = (Beehive) clickedBlock.getBlockData();
        if (beehive.getHoneyLevel() != beehive.getMaximumHoneyLevel()) {
            return;
        }

        ItemStack item = e.getItem();
        if (item == null) {
            return;
        }

        // The item should be a glass bottle
        if (!item.getType().equals(Material.GLASS_BOTTLE)) {
            return;
        }

        game.onMaterialCollected(
                e.getPlayer(),
                Material.HONEY_BOTTLE
        );
    }

}
