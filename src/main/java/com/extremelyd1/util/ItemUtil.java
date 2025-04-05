package com.extremelyd1.util;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.team.PlayerTeam;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    /**
     * Updates the bingo card item stack in each of the team's player's inventories
     * @param team The team for which to update
     * @param factory The bingo card item factory from which to create the item stack
     */
    public static void updateBingoCard(BingoCard bingoCard, PlayerTeam team, BingoCardItemFactory factory) {
        for (Player player : team.getPlayers()) {
            boolean itemFound = false;

            // Loop over the contents of the player's inventory and try to find an existing bingo card item stack
            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack itemStack = player.getInventory().getContents()[i];
                if (factory.isBingoCard(itemStack)) {
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
     * Checks whether the given player has a bingo card item stack in their inventory
     * @param player The player to check
     * @return Whether the player has the bingo card item stack in their inventory
     */
    public static boolean hasBingoCard(Player player, BingoCardItemFactory factory) {
        return getBingoCardItemStack(player, factory) != null;
    }

    /**
     * Get the item stack of the bingo card from the given player
     * @param player The player to get the item stack from
     * @return The ItemStack of the bingo card item
     */
    public static ItemStack getBingoCardItemStack(Player player, BingoCardItemFactory factory) {
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack itemStack = player.getInventory().getContents()[i];
            if (factory.isBingoCard(itemStack)) {
                return itemStack;
            }
        }

        return null;
    }

}
