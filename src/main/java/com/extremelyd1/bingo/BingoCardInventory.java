package com.extremelyd1.bingo;

import com.extremelyd1.bingo.item.BingoItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents the Bukkit inventory with the bingo card items.
 */
public class BingoCardInventory {

    /**
     * The Bukkit inventory to store the items.
     */
    private final Inventory inventory;

    /**
     * Create a bingo card inventory with the given bingo card.
     * @param bingoItems The bingo card items to make the inventory from.
     */
    public BingoCardInventory(BingoItem[][] bingoItems) {
        // Create the inventory and set the items in it
        inventory = Bukkit.createInventory(
                null,
                9 * 5,
                Component.text("Bingo Card").color(NamedTextColor.AQUA)
        );

        for (int y = 0; y < BingoCard.BOARD_SIZE; y++) {
            for (int x = 0; x < BingoCard.BOARD_SIZE; x++) {
                Material material = bingoItems[y][x].getMaterial();
                ItemStack itemStack = new ItemStack(material, 1);

                inventory.setItem(y * 9 + x + 2, itemStack);
            }
        }
    }

    /**
     * Show the given player this inventory.
     * @param player The player to show the inventory to.
     */
    public void show(Player player) {
        player.openInventory(inventory);
    }

    /**
     * Whether the given inventory is the bingo card inventory.
     * @param inventory The inventory to check.
     * @return True if the given inventory is the bingo card inventory.
     */
    public boolean isBingoCard(Inventory inventory) {
        return this.inventory == inventory;
    }
}
