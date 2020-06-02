package com.extremelyd1.bingo.item;

import org.bukkit.Material;

import java.util.List;
import java.util.Random;

public class BingoItemSet {

    private final List<Material> materials;

    public BingoItemSet(List<Material> materials) {
        this.materials = materials;
    }

    public Material pick() {
        return materials.get(new Random().nextInt(materials.size()));
    }

    /**
     * Filter out all materials on the given blacklist, return true if resulting
     * bingo item set of non-empty
     * @param blacklist The blacklist of materials to remove from this item set
     * @return Whether this item set is non-empty
     */
    public boolean filter(List<Material> blacklist) {
        for (int i = 0; i < materials.size(); i++) {
            Material material = materials.get(i);
            if (blacklist.contains(material)) {
                materials.remove(i);
                i--;
            }
        }

        return materials.size() > 0;
    }

}
