package com.extremelyd1.bingo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BingoCardInventory {

    private final Inventory inventory;

    public BingoCardInventory(BingoCard bingoCard) {
        // Create the inventory and set the items in it
        inventory = Bukkit.createInventory(
                null,
                9 * 5,
                ChatColor.AQUA + "Bingo Card"
        );

        for (int y = 0; y < BingoCard.BOARD_SIZE; y++) {
            for (int x = 0; x < BingoCard.BOARD_SIZE; x++) {
                Material material = bingoCard.getBingoItems()[y][x].getMaterial();
                ItemStack itemStack = new ItemStack(material, 1);

                inventory.setItem(y * 9 + x + 2, itemStack);
            }
        }
    }

    public void show(Player player) {
        player.openInventory(inventory);
    }

}
