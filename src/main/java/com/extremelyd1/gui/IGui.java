package com.extremelyd1.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface IGui extends InventoryHolder {
    void onGUIClick(Player whoClicked, int slot, ItemStack clickedItem);
}