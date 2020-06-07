package com.extremelyd1.bingo;

import com.extremelyd1.bingo.item.BingoItem;
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
     * The number of collected items on this bingo card
     */
    private int numCollected;

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
    }

    /**
     * Creates a bingo card exactly with the given 2D array of bingo items
     * @param bingoItems The 2D array of bingo items
     */
    public BingoCard(BingoItem[][] bingoItems) {
        this.bingoItems = bingoItems;
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
     * Sets the item to be collected on the card
     * @param material The material of the item collected
     */
    public void addItemCollected(Material material) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                BingoItem bingoItem = bingoItems[y][x];
                if (bingoItem.getMaterial().equals(material)
                        && !bingoItem.isCollected()) {
                    bingoItem.setCollected();

                    numCollected++;
                }
            }
        }
    }

    /**
     * Gets the number of lines (rows, columns or diagonals) that is completed on this bingo card
     * @return The number of lines completed
     */
    public int getNumLinesComplete() {
        int numLinesComplete = 0;

        for (int y = 0; y < BOARD_SIZE; y++) {
            if (checkRow(y)) {
                numLinesComplete++;
            }
        }

        for (int x = 0; x < BOARD_SIZE; x++) {
            if (checkColumn(x)) {
                numLinesComplete++;
            }
        }

        if (checkDiagonal(true)) {
            numLinesComplete++;
        }

        if (checkDiagonal(false)) {
            numLinesComplete++;
        }

        return numLinesComplete;
    }

    /**
     * Whether at least 1 line is completed on this bingo card
     * @return Whether at least 1 lines is completed
     */
    public boolean hasLineComplete() {
        for (int y = 0; y < BOARD_SIZE; y++) {
            if (checkRow(y)) {
                return true;
            }
        }

        for (int x = 0; x < BOARD_SIZE; x++) {
            if (checkColumn(x)) {
                return true;
            }
        }

        return checkDiagonal(true) || checkDiagonal(false);
    }

    /**
     * Gets the number of collected items on this bingo card
     * @return The number of collected items
     */
    public int getNumberOfCollectedItems() {
        return numCollected;
    }

    /**
     * Whether this bingo card is fully completed
     * @return Whether this bingo is fully completed
     */
    public boolean isCardComplete() {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                BingoItem bingoItem = bingoItems[y][x];
                if (!bingoItem.isCollected()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check whether the row with index y is completed
     * @param y The index to check for
     * @return Whether the row is completed
     */
    private boolean checkRow(int y) {
        for (int x = 0; x < BOARD_SIZE; x++) {
            if (!bingoItems[y][x].isCollected()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether the column with index x is completed
     * @param x The index to check for
     * @return Whether the column is completed
     */
    private boolean checkColumn(int x) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            if (!bingoItems[y][x].isCollected()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether a diagonal is completed
     * @param startTopLeft Whether to check the diagonal starting in the top left
     *                     or the diagonal starting in the top right
     * @return Whether the diagonal is completed
     */
    private boolean checkDiagonal(boolean startTopLeft) {
        if (startTopLeft) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (!bingoItems[i][i].isCollected()) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (!bingoItems[i][BOARD_SIZE - 1 - i].isCollected()) {
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

}
