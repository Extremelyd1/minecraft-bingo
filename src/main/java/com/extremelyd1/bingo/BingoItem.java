package com.extremelyd1.bingo;

import org.bukkit.Material;

public class BingoItem {

    private final Material material;
    private boolean collected;

    public BingoItem(Material material) {
        this.material = material;
        this.collected = false;
    }

    public Material getMaterial() {
        return material;
    }

    public void setCollected() {
        this.collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public BingoItem copy() {
        return new BingoItem(this.material);
    }

}
