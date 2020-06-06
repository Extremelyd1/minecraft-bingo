package com.extremelyd1.util;

import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    /**
     * Updates the bingo card itemstack in each of the team's player's inventories
     * @param team The team for which to update
     * @param factory The bingo card item factory from which to create the itemstack
     */
    public static void updateBingoCard(Team team, BingoCardItemFactory factory) {
        for (Player player : team.getPlayers()) {
            boolean itemFound = false;

            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack itemStack = player.getInventory().getContents()[i];
                if (itemStack == null) {
                    continue;
                }

                if (itemStack.getItemMeta() != null
                        && itemStack.getItemMeta().getDisplayName().contains("Bingo Card")) {
                    player.getInventory().setItem(i, factory.create(
                            team.getBingoCard()
                    ));

                    itemFound = true;

                    break;
                }
            }

            if (!itemFound) {
                player.getInventory().addItem(factory.create(
                        team.getBingoCard()
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
