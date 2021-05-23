package com.extremelyd1.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    public static Inventory copyInventory(Inventory inventory) {
        Inventory copiedInventory = Bukkit.createInventory(
                null,
                inventory.getType()
        );

        copiedInventory.setContents(inventory.getContents().clone());

        return copiedInventory;
    }

    /**
     * Checks whether the given item stack can be shift clicked into the given array of candidate item stacks
     * @param itemStacks The array of item stacks to check in
     * @param itemStack The item stack to check for
     * @return True if the ItemStack can be moved to the Inventory, false otherwise
     */
    public static boolean canShiftClickItem(ItemStack[] itemStacks, ItemStack itemStack) {
        for (ItemStack inventoryItemStack : itemStacks) {
            if (inventoryItemStack == null) {
                return true;
            }

            if (inventoryItemStack.getType().equals(itemStack.getType())
                    && inventoryItemStack.getAmount() < inventoryItemStack.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

}
