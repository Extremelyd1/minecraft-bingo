package com.extremelyd1.bingo.item;

import org.bukkit.Material;

/**
 * Represents an single item on the bingo card for a single team
 */
public class BingoItem {

    /**
     * The material that this item represents
     */
    private final Material material;
    /**
     * Whether the item has been collected by the team
     */
    private boolean collected;

    public BingoItem(Material material) {
        this.material = material;
        this.collected = false;
    }

    /**
     * Get the material pertaining to this item
     * @return The material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Set this item as being collected
     */
    public void setCollected() {
        this.collected = true;
    }

    /**
     * Whether this item has been collected
     * @return Whether this item has been collected
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Make a copy of this BingoItem instance
     * @return A copy of this BingoItem instance
     */
    public BingoItem copy() {
        return new BingoItem(this.material);
    }

}
