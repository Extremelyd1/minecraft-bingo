package com.extremelyd1.listener;

import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.Game;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ItemListener implements Listener {

    /**
     * The game instance.
     */
    private final Game game;

    /**
     * The bingo card item factory instance to check whether an item is a bingo card.
     */
    private final BingoCardItemFactory bingoCardItemFactory;

    public ItemListener(Game game, BingoCardItemFactory bingoCardItemFactory) {
        this.game = game;
        this.bingoCardItemFactory = bingoCardItemFactory;
    }

    private static final Map<EntityType, Material> FISH_TYPE_TO_MATERIAL = new HashMap<>() {{
        put(EntityType.SALMON, Material.SALMON_BUCKET);
        put(EntityType.COD, Material.COD_BUCKET);
        put(EntityType.PUFFERFISH, Material.PUFFERFISH_BUCKET);
        put(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH_BUCKET);
    }};

    private static final Map<EntityType, Material> FOOD_TYPE_TO_MATERIAL = new HashMap<>() {{
        put(EntityType.MOOSHROOM, Material.MUSHROOM_STEW);
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

        if (bingoCardItemFactory.isBingoCard(e.getItemDrop().getItemStack())) {
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
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            return;
        }

        Block clickedBlock = e.getClickedBlock();

        ItemStack item = e.getItem();

        // We are only interested in filling glass bottles
        if (item == null || !item.getType().equals(Material.GLASS_BOTTLE)) {
            return;
        }

        // Filling glass bottles can only be done with a right click
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        // Check whether we are right-clicking Beehives or Bee nests
        if (clickedBlock != null) {
            Material clickedType = clickedBlock.getType();

            if ((clickedType.equals(Material.BEE_NEST) || clickedType.equals(Material.BEEHIVE))) {
                // Check whether the honey level is at the maximum value
                Beehive beehive = (Beehive) clickedBlock.getBlockData();
                if (beehive.getHoneyLevel() == beehive.getMaximumHoneyLevel()) {
                    game.onMaterialCollected(
                            e.getPlayer(),
                            Material.HONEY_BOTTLE
                    );

                    return;
                }
            }
        }

        World world = e.getPlayer().getWorld();
        // Get nearby entities with a similar predicate as Minecraft uses in the ItemGlassBottle class
        Collection<org.bukkit.entity.Entity> nearbyEntities = world.getNearbyEntities(
                e.getPlayer().getBoundingBox().clone().expand(2.0),
                entity -> {
                    // Get the NMS entity instance
                    Entity nmsEntity = ((CraftEntity) entity).getHandle();

                    if (nmsEntity == null) {
                        return false;
                    }

                    // We only need AreaEffectCloud entities
                    if (!(nmsEntity instanceof AreaEffectCloud effectCloud)) {
                        return false;
                    }

                    // Do the same checks as the ItemGlassBottle class
                    return effectCloud.isAlive() && effectCloud.getOwner() instanceof EnderDragon;
                }
        );

        if (!nearbyEntities.isEmpty()) {
            game.onMaterialCollected(
                    e.getPlayer(),
                    Material.DRAGON_BREATH
            );
        }
    }

}
