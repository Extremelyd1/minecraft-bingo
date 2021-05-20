package com.extremelyd1.bingo.item;

import com.extremelyd1.game.team.PlayerTeam;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an single item on the bingo card for a single team
 */
public class BingoItem {

    /**
     * The material that this item represents
     */
    private final Material material;
    /**
     * A list of which teams have collected this item
     */
    private final List<PlayerTeam> collectors;

    public BingoItem(Material material) {
        this.material = material;
        this.collectors = new ArrayList<>();
    }

    /**
     * Get the material pertaining to this item
     * @return The material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Set this item as being collected for the given team
     */
    public void addCollector(PlayerTeam team) {
        collectors.add(team);
    }

    /**
     * Check whether a team has collected the item
     * @param team The team
     * @return Whether the team has collected the item
     */
    public boolean hasCollected(PlayerTeam team) {
        return collectors.contains(team);
    }

    public Iterable<PlayerTeam> getCollectors() {
        return collectors;
    }

    /**
     * Make a copy of this BingoItem instance
     * @return A copy of this BingoItem instance
     */
    public BingoItem copy() {
        return new BingoItem(this.material);
    }

}
