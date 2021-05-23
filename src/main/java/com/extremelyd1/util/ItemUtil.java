package com.extremelyd1.util;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.team.PlayerTeam;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    /**
     * Updates the bingo card itemstack in each of the team's player's inventories
     * @param team The team for which to update
     * @param factory The bingo card item factory from which to create the itemstack
     */
    public static void updateBingoCard(BingoCard bingoCard, PlayerTeam team, BingoCardItemFactory factory) {
        for (Player player : team.getPlayers()) {
            boolean itemFound = false;

            // Loop over the contents of the player's inventory and try to find an existing bingo card item stack
            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack itemStack = player.getInventory().getContents()[i];
                if (itemStack == null) {
                    continue;
                }

                if (itemStack.getItemMeta() != null
                        && itemStack.getItemMeta().getDisplayName().contains("Bingo Card")) {
                    factory.updateBingoCardItemStack(itemStack, bingoCard, team);

                    itemFound = true;

                    break;
                }
            }

            // There was no item stack found, so we create a new one
            if (!itemFound) {
                player.getInventory().addItem(factory.create(
                        bingoCard,
                        team
                ));
            }
        }
    }

    /**
     * Checks whether the given player has a bingo card itemstack in their inventory
     * @param player The player to check
     * @return Whether the player has the bingo card itemstack in their inventory
     */
    public static boolean hasBingoCard(Player player) {
        return getBingoCardItemStack(player) != null;
    }

    /**
     * Get the itemstack of the bingo card from the given player
     * @param player The player to get the itemstack from
     * @return The ItemStack of the bingo card item
     */
    public static ItemStack getBingoCardItemStack(Player player) {
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack itemStack = player.getInventory().getContents()[i];
            if (itemStack == null) {
                continue;
            }

            if (itemStack.getItemMeta() != null
                    && itemStack.getItemMeta().getDisplayName().contains("Bingo Card")) {
                return itemStack;
            }
        }

        return null;
    }

}
