package com.extremelyd1.bingo.item;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.FileUtil;
import org.bukkit.Material;

import java.io.File;
import java.util.*;

/**
 * A class that handles loading/storing of material names from file
 * Also provides methods to create bingo item sets and randomly picking from these sets
 */
public class BingoItemMaterials {
    /**
     * The number of times to attempt picking materials while respecting groups.
     */
    private static final int MAX_MATERIAL_PICK_TRIES = 10;

    /**
     * The game instance
     */
    private final Game game;

    /**
     * A list of bingo items of S tier rarity
     */
    private List<Material> sTierItems;
    /**
     * A list of bingo items of A tier rarity
     */
    private List<Material> aTierItems;
    /**
     * A list of bingo items of B tier rarity
     */
    private List<Material> bTierItems;
    /**
     * A list of bingo items of C tier rarity
     */
    private List<Material> cTierItems;
    /**
     * A list of bingo items of D tier rarity
     */
    private List<Material> dTierItems;
    /**
     * A list of bingo items that are blacklisted
     */
    private Set<Material> blacklist;

    /**
     * A mapping from material to all other materials that it shares a group with
     */
    private Map<Material, Set<Material>> mapMaterialToGroupMates;

    public BingoItemMaterials(Game game) {
        this.game = game;
    }

    /**
     * Load the materials of all tiers and group (and blacklist if enabled) into the data structures
     *
     * @param dataFolder The data folder in which the data files are stored
     */
    public void loadMaterials(File dataFolder) {
        String path = dataFolder.getPath() + "/item_data/";

        this.sTierItems = readMaterialsFile(path, "s_tier.txt");
        this.aTierItems = readMaterialsFile(path, "a_tier.txt");
        this.bTierItems = readMaterialsFile(path, "b_tier.txt");
        this.cTierItems = readMaterialsFile(path, "c_tier.txt");
        this.dTierItems = readMaterialsFile(path, "d_tier.txt");

        this.blacklist = new HashSet<>(readMaterialsFile(path, "blacklist.txt"));

        this.mapMaterialToGroupMates = new HashMap<>();
        for (Collection<Material> group : readGroupsFile(path)) {
            for (Material material : group) {
                if (!this.mapMaterialToGroupMates.containsKey(material)) {
                    this.mapMaterialToGroupMates.put(material, new HashSet<>());
                }
                this.mapMaterialToGroupMates.get(material).addAll(group);
            }
        }
    }

    /**
     * Read a file that contains a set of items/materials, one per line, returning the list of these materials
     *
     * @param path     The path at which the file resides
     * @param fileName The name of the file
     * @return A list of materials that are stored in the given file
     */
    private List<Material> readMaterialsFile(String path, String fileName) {
        String fileString = FileUtil.readFileToString(path + fileName);
        if (fileString == null) {
            Game.getLogger().severe("Could not read materials file " + fileName);
            return new ArrayList<>();
        }

        List<Material> materials = new ArrayList<>();

        for (String line : fileString.split("\n")) {
            try {
                materials.add(Material.valueOf(line));
            } catch (IllegalArgumentException e) {
                Game.getLogger().warning(String.format("Could not find material with name %s in file %s", line, fileName));
            }
        }

        return materials;
    }

    /**
     * Get the collection of groups (sets of items/materials) from a material groups file
     * which contains a line per group, which is represented as a '|'-separated list of items.
     *
     * @param path The path at which the file resides
     * @return The collection of groups that are stored in the given file
     */
    private Collection<Set<Material>> readGroupsFile(String path) {
        String fileString = FileUtil.readFileToString(path + "groups.txt");
        if (fileString == null) {
            Game.getLogger().severe("Could not read material groups file " + "groups.txt");
            return new HashSet<>();
        }

        Collection<Set<Material>> groups = new HashSet<>();

        for (String line : fileString.split("\n")) {
            if (!line.isBlank()) {
                Set<Material> group = new HashSet<>();
                for (String material : line.split("\\|")) {
                    try {
                        group.add(Material.valueOf(material));
                    } catch (IllegalArgumentException e) {
                        Game.getLogger().warning(String.format("Could not find material with name %s in file %s", material, "groups.txt"));
                    }
                }
                groups.add(group);
            }
        }

        return groups;
    }

    /**
     * Randomly pick a list of materials from the tiers based on the distribution denoted by the config values.
     *
     * @return A list of materials
     */
    public List<Material> pickMaterials() {
        return pickMaterials(
                game.getConfig().getNumSTier(),
                game.getConfig().getNumATier(),
                game.getConfig().getNumBTier(),
                game.getConfig().getNumCTier(),
                game.getConfig().getNumDTier()
        );
    }

    /**
     * Randomly pick a list of materials from the tiers.
     *
     * <p>
     * Loops over the tiers, going over the items of that tier in random order, picking the first num(tier) materials
     * that are not excluded either by being in the blacklist or by sharing a group with an already picked material.
     * If this leads to no options remaining for a tier, the process is restarted.
     * After MAX_MATERIAL_PICK_TRIES, one more attempt is made disregarding the grouping.
     * </p>
     *
     * @param numSTier The number of S tier materials to pick
     * @param numATier The number of A tier materials to pick
     * @param numBTier The number of B tier materials to pick
     * @param numCTier The number of C tier materials to pick
     * @param numDTier The number of D tier materials to pick
     * @return A list of materials
     */
    public List<Material> pickMaterials(int numSTier, int numATier, int numBTier, int numCTier, int numDTier) {
        Random random = new Random();

        record Tier(List<Material> items, int numRequired) {
        }

        Tier[] tiers = new Tier[] {
                new Tier(sTierItems, numSTier),
                new Tier(aTierItems, numATier),
                new Tier(bTierItems, numBTier),
                new Tier(cTierItems, numCTier),
                new Tier(dTierItems, numDTier),
        };

        // Try to create a selection (trying again if the random picking results in no options remaining)
        for (int attempt = 0; attempt < MAX_MATERIAL_PICK_TRIES + 1; attempt++) { // +1 to disregard groups in last try
            boolean ignoreGroups = attempt == MAX_MATERIAL_PICK_TRIES;
            Game.getLogger().info("Starting attempt " + (attempt + 1) + " at material picking.");
            if (ignoreGroups) Game.getLogger().warning("Ignoring grouping as last resort to pick items.");

            List<Material> result = new ArrayList<>();
            Set<Material> exclude = new HashSet<>();
            if (game.getConfig().isBlacklistEnabled()) {
                exclude.addAll(blacklist);
            }

            for (Tier tier : tiers) {
                // Randomize the order of the items in the tier
                Collections.shuffle(tier.items, random);

                // Pick as many of this tier as required, skipping excluded items
                int numLeftInTier = tier.numRequired;
                for (Material material : tier.items) {
                    if (numLeftInTier == 0) {
                        break;
                    }

                    if (exclude.contains(material)) {
                        Game.getLogger().info("Skipping excluded material " + material + "(" + exclude.size() + " excluded)");
                        continue;
                    }

                    // Include the material
                    result.add(material);
                    numLeftInTier--;

                    if (!ignoreGroups) {
                        // Add the materials that the picked material shares a group with to the excluded materials,
                        // to prevent picking multiple materials that share a group
                        var addToExclude = mapMaterialToGroupMates.getOrDefault(material, new HashSet<>());
                        exclude.addAll(addToExclude);
                        Game.getLogger().info("Included material " + material +
                                ", thus newly excluding " + addToExclude.size() + " group materials.");
                    } else {
                        Game.getLogger().info("Included material " + material);
                    }

                }
            }

            // Check whether enough items were picked (
            if (result.size() == numSTier + numATier + numBTier + numCTier + numDTier) {
                Game.getLogger().info("Completed material selection of " + result.size() + " items.");
                return result;
            } else {
                Game.getLogger().info("Material selection incomplete, only picked " + result.size() + " items.");
            }
        }

        throw new IllegalStateException("Could not make material selection, even when disregarding groups.");
    }

}
