package com.extremelyd1.bingo;

import com.extremelyd1.bingo.item.BingoItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BingoCard {

    private final static int BOARD_SIZE = 5;

    private final BingoItem[][] bingoItems;

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

    public BingoCard(BingoItem[][] bingoItems) {
        this.bingoItems = bingoItems;
    }

    public boolean containsItem(Material material) {
        return getItemByMaterial(material) != null;
    }

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
                if (bingoItem.getMaterial().equals(material)) {
                    bingoItem.setCollected();
                }
            }
        }
    }

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

    public int getNumberOfCollectedItems() {
        int numCollected = 0;

        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                BingoItem bingoItem = bingoItems[y][x];
                if (bingoItem.isCollected()) {
                    numCollected++;
                }
            }
        }

        return numCollected;
    }

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

    private boolean checkRow(int y) {
        for (int x = 0; x < BOARD_SIZE; x++) {
            if (!bingoItems[y][x].isCollected()) {
                return false;
            }
        }

        return true;
    }

    private boolean checkColumn(int x) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            if (!bingoItems[y][x].isCollected()) {
                return false;
            }
        }

        return true;
    }

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

    public BingoCard copy() {
        BingoItem[][] newItemArray = new BingoItem[BOARD_SIZE][BOARD_SIZE];

        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                newItemArray[y][x] = this.bingoItems[y][x].copy();
            }
        }

        return new BingoCard(newItemArray);
    }

    public BingoItem[][] getBingoItems() {
        return bingoItems;
    }

}
