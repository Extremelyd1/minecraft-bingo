package com.extremelyd1.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class InventoryUtil {

    public static Inventory copyInventory(Inventory inventory) {
        Inventory copiedInventory = Bukkit.createInventory(
                null,
                inventory.getType()
        );

        copiedInventory.setContents(inventory.getContents().clone());

        return copiedInventory;
    }

}
