package com.extremelyd1.bingo;

import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.game.team.PlayerTeam;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a bingo card containing bingo items
 */
public class BingoCard {

    /**
     * The size of the board
     */
    public final static int BOARD_SIZE = 5;

    /**
     * A 2D array of bingo items
     */
    private final BingoItem[][] bingoItems;

    /**
     * The corresponding inventory that holds ItemStacks of this bingo card
     */
    private final BingoCardInventory bingoCardInventory;

    /**
     * Creates a bingo card by randomly picking from the list of materials given
     * @param materials The list of materials to pick from
     */
    public BingoCard(List<Material> materials) {
        if (materials.size() < 25) {
            throw new IllegalArgumentException("The size of the given material list is less than 25");
        }

        List<Material> materialsLeft = new ArrayList<>(materials);
        bingoItems = new BingoItem[BOARD_SIZE][BOARD_SIZE];

        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                Material material = materialsLeft.get(
                        new Random().nextInt(materialsLeft.size())
                );
                BingoItem bingoItem = new BingoItem(material);
                this.bingoItems[y][x] = bingoItem;

                materialsLeft.remove(material);
            }
        }

        bingoCardInventory = new BingoCardInventory(bingoItems);
    }

    /**
     * Creates a bingo card exactly with the given 2D array of bingo items
     * @param bingoItems The 2D array of bingo items
     */
    public BingoCard(BingoItem[][] bingoItems) {
        this.bingoItems = bingoItems;

        bingoCardInventory = new BingoCardInventory(bingoItems);
    }

    /**
     * Whether this bingo card contains the given material
     * @param material The material to check for
     * @return Whether this bingo card contains the given material
     */
    public boolean containsItem(Material material) {
        return getItemByMaterial(material) != null;
    }

    /**
     * Gets the bingo item with the given material
     * @param material The material to search for
     * @return The bingo item with the given material or null if no such item exists
     */
    public BingoItem getItemByMaterial(Material material) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (bingoItems[y][x].getMaterial().equals(material)) {
                    return bingoItems[y][x];
                }
            }
        }

        return null;
    }

    /**
     * Sets the item to be collected on the card for the given team
     * @param material The material of the item collected
     * @param team The team to mark the item for
     */
    public void addItemCollected(Material material, PlayerTeam team) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                BingoItem bingoItem = bingoItems[y][x];
                if (bingoItem.getMaterial().equals(material)
                        && !bingoItem.hasCollected(team)) {
                    bingoItem.addCollector(team);

                    // Does literally nothing
                    team.incrementCollected();
                }
            }
        }
    }

    /**
     * Gets the number of lines (rows, columns or diagonals) that is completed on this bingo card for the given Team
     * @param team The team to check for
     * @return The number of lines completed
     */
    public int getNumLinesComplete(PlayerTeam team) {
        int numLinesComplete = 0;

        for (int y = 0; y < BOARD_SIZE; y++) {
            if (checkRow(team, y)) {
                numLinesComplete++;
            }
        }

        for (int x = 0; x < BOARD_SIZE; x++) {
            if (checkColumn(team, x)) {
                numLinesComplete++;
            }
        }

        if (checkDiagonal(team, true)) {
            numLinesComplete++;
        }

        if (checkDiagonal(team, false)) {
            numLinesComplete++;
        }

        return numLinesComplete;
    }

    /**
     * Whether this bingo card is fully completed for the given team
     * @param team The team to check for
     * @return Whether this bingo is fully completed
     */
    public boolean isCardComplete(PlayerTeam team) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                BingoItem bingoItem = bingoItems[y][x];
                if (!bingoItem.hasCollected(team)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check whether the row with index y is completed for the given team
     * @param team The team to check for
     * @param y The index to check for
     * @return Whether the row is completed
     */
    private boolean checkRow(PlayerTeam team, int y) {
        for (int x = 0; x < BOARD_SIZE; x++) {
            if (!bingoItems[y][x].hasCollected(team)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether the column with index x is completed for the given team
     * @param team The team to check for
     * @param x The index to check for
     * @return Whether the column is completed
     */
    private boolean checkColumn(PlayerTeam team, int x) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            if (!bingoItems[y][x].hasCollected(team)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether a diagonal is completed for the given team
     * @param team The team to check for
     * @param startTopLeft Whether to check the diagonal starting in the top left
     *                     or the diagonal starting in the top right
     * @return Whether the diagonal is completed
     */
    private boolean checkDiagonal(PlayerTeam team, boolean startTopLeft) {
        if (startTopLeft) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (!bingoItems[i][i].hasCollected(team)) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (!bingoItems[i][BOARD_SIZE - 1 - i].hasCollected(team)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Makes a copy of this bingo card by copying each individual bingo item
     * @return A copy of this bingo card
     */
    public BingoCard copy() {
        BingoItem[][] newItemArray = new BingoItem[BOARD_SIZE][BOARD_SIZE];

        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                newItemArray[y][x] = this.bingoItems[y][x].copy();
            }
        }

        return new BingoCard(newItemArray);
    }

    /**
     * Get the 2D array of bingo items on this bingo card
     * @return A 2D array of bingo items
     */
    public BingoItem[][] getBingoItems() {
        return bingoItems;
    }

    public BingoCardInventory getBingoCardInventory() {
        return bingoCardInventory;
    }

}
