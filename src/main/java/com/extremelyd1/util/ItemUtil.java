package com.extremelyd1.util;

import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

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

    public static boolean hasBingoCard(Player player) {
        return getBingoCardItemStack(player) != null;
    }

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
