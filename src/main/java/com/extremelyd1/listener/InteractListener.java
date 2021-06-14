package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.gui.IGui;
import com.extremelyd1.util.InventoryUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class InteractListener implements Listener {

    /**
     * The game instance
     */
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
        Action action = e.getAction();
        if (game.getState().equals(Game.State.POST_GAME)) {
            // Check if we right clicked a block
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Block clickedBlock = e.getClickedBlock();
                if (clickedBlock != null) {
                    // Get block state and check if it is a container
                    BlockState blockState = clickedBlock.getState();
                    if (blockState instanceof Container) {
                        // Create copy of inventory
                        Inventory inventory = InventoryUtil.copyInventory(((Container) blockState).getInventory());
                        // Show the player the copied inventory
                        e.getPlayer().openInventory(inventory);
                    }
                }
            }

            e.setCancelled(true);
            return;
        }

        if (game.getState().equals(Game.State.PRE_GAME)) {
            e.setCancelled(true);
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_AIR)
                && !action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (e.getHand() == null || !e.getHand().equals(EquipmentSlot.HAND)) {
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
        if (team == null || team.isSpectatorTeam()) {
            return;
        }

        game.getBingoCard().getBingoCardInventory().show(e.getPlayer());
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        // Disallow interaction with entities in pre-game
        if (game.getState().equals(Game.State.PRE_GAME)) {
            e.setCancelled(true);
            return;
        }

        if (game.getState().equals(Game.State.POST_GAME)) {
            Entity entity = e.getRightClicked();

            // If the player clicked an entity with an inventory
            // show them the inventory
            // TODO: replicate villager trade window, since they only allow a single
            //  player to interact with them
            if (entity.getType().equals(EntityType.VILLAGER)) {
                return;
            }

            // Only allow the following inventory holding entities as interaction targets
            if (entity.getType().equals(EntityType.MINECART_CHEST)
                    || entity.getType().equals(EntityType.MINECART_FURNACE)
                    || entity.getType().equals(EntityType.MINECART_HOPPER)) {
                InventoryHolder inventoryHolder = (InventoryHolder) entity;

                e.getPlayer().openInventory(inventoryHolder.getInventory());
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getWhoClicked();

        if (e.getInventory().getHolder() instanceof IGui) {
            e.setCancelled(true);
            IGui gui = (IGui) e.getInventory().getHolder();
            gui.onGUIClick(player, e.getRawSlot(), e.getCurrentItem());
            return;
        }

        if (game.getState().equals(Game.State.POST_GAME)) {
            e.setCancelled(true);
            return;
        }

        if (e.getClick().equals(ClickType.MIDDLE)) {
            ItemStack itemStack = e.getCurrentItem();
            if (itemStack != null
                    && itemStack.hasItemMeta()
                    && itemStack.getItemMeta().getDisplayName().contains("Bingo Card")) {
                Team team = game.getTeamManager().getTeamByPlayer(player);
                if (team == null || team.isSpectatorTeam()) {
                    return;
                }

                game.getBingoCard().getBingoCardInventory().show((Player) e.getWhoClicked());
            }
        }

        if (e.getView().getTitle().contains("Bingo Card")) {
            e.setCancelled(true);
            return;
        }

        if (e.getClickedInventory() == null) {
            return;
        }

        ItemStack currentItem = e.getCurrentItem();

        if (currentItem == null) {
            return;
        }

        InventoryType invType = e.getClickedInventory().getType();

        // In certain inventory types and while performing certain inventory actions,
        // we need to check whether we can actually perform the action.
        // Because if this can't be done, it will not consume the ingredients in the recipe.
        if (invType.equals(InventoryType.WORKBENCH)
                || invType.equals(InventoryType.CRAFTING)
                || invType.equals(InventoryType.SMITHING)
                || invType.equals(InventoryType.STONECUTTER)
                || invType.equals(InventoryType.MERCHANT)) {
            PlayerInventory playerInventory = e.getWhoClicked().getInventory();

            if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                // We are dealing with shift-click, now we need to check whether
                // there is space in the player inventory to insert this item
                if (!InventoryUtil.canShiftClickItem(
                        playerInventory.getStorageContents(),
                        currentItem
                )) {
                    return;
                }
            }

            if (e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
                // We are dealing with a move to specific hotbar position
                if (playerInventory.getStorageContents()[e.getHotbarButton()] != null) {
                    // The slot we are trying to move the result to is non-empty, so minecraft will not move the item
                    return;
                }
            }

            if (e.getAction().equals(InventoryAction.NOTHING)) {
                return;
            }
        }

        game.onMaterialCollected(player, currentItem.getType());
    }
}
