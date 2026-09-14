package com.extremelyd1.bingo.item;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.FileUtil;
import org.bukkit.Material;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
     * A set of sets of materials that represents all groups of bingo items of S tier rarity
     */
    private Set<Set<Material>> sTierGroups;
    /**
     * A set of sets of materials that represents all groups of bingo items of A tier rarity
     */
    private Set<Set<Material>> aTierGroups;
    /**
     * A set of sets of materials that represents all groups of bingo items of B tier rarity
     */
    private Set<Set<Material>> bTierGroups;
    /**
     * A set of sets of materials that represents all groups of bingo items of C tier rarity
     */
    private Set<Set<Material>> cTierGroups;
    /**
     * A set of sets of materials that represents all groups of bingo items of D tier rarity
     */
    private Set<Set<Material>> dTierGroups;
    /**
     * A list of bingo items that are blacklisted
     */
    private Set<Material> blacklist;

    /**
     * A mapping from material to all other materials that it shares a global group with
     */
    private Map<Material, Set<Material>> materialToGlobalGroupMap;

    public BingoItemMaterials(Game game) {
        this.game = game;
    }

    /**
     * Load the materials of all tier groups and global groups (and blacklist if enabled) into the data structures
     *
     * @param dataFolder The data folder in which the data files are stored
     */
    public void loadMaterials(File dataFolder) {
        String path = dataFolder.getPath() + "/item_data/";

        Set<Set<Material>> globalGroups = readGlobalGroupsFile(path);

        this.sTierGroups = createTierGroups(readTierFile(path, "s_tier.txt"), globalGroups);
        this.aTierGroups = createTierGroups(readTierFile(path, "a_tier.txt"), globalGroups);
        this.bTierGroups = createTierGroups(readTierFile(path, "b_tier.txt"), globalGroups);
        this.cTierGroups = createTierGroups(readTierFile(path, "c_tier.txt"), globalGroups);
        this.dTierGroups = createTierGroups(readTierFile(path, "d_tier.txt"), globalGroups);

        this.blacklist = new HashSet<>(readTierFile(path, "blacklist.txt"));

        this.materialToGlobalGroupMap = new HashMap<>();
        for (Set<Material> globalGroup : globalGroups) {
            for (Material material : globalGroup) {
                if (!this.materialToGlobalGroupMap.containsKey(material)) {
                    this.materialToGlobalGroupMap.put(material, new HashSet<>());
                }
                this.materialToGlobalGroupMap.get(material).addAll(globalGroup);
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
    private Set<Material> readTierFile(String path, String fileName) {
        String fileString = FileUtil.readFileToString(path + fileName);
        if (fileString == null) {
            Game.getLogger().severe("Could not read materials file " + fileName);
            return new HashSet<>();
        }

        Set<Material> materials = new HashSet<>();

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
     * Creates the groups within a tier from a list of items and the global groups. Items within a tier will be
     * grouped together based on their global groups to ensure a fair distribution of items within the tier for
     * picking materials later.
     *
     * @param tierItems Set of materials of the tier for which the groups need to be created
     * @param globalGroups The global tier-independent groups of materials
     * @return A set containing sets of materials representing all groups of materials of the tier
     */
    private Set<Set<Material>> createTierGroups(Set<Material> tierItems, Set<Set<Material>> globalGroups) {
        List<Material> tierItemsCopy = new ArrayList<>(tierItems);
        Set<Set<Material>> tierGroups = new HashSet<>();

        for (Set<Material> globalGroup : globalGroups) {
            Set<Material> tierGroup = new HashSet<>();
            for (int i = 0; i < tierItemsCopy.size(); i++) {
                Material material = tierItemsCopy.get(i);

                if (globalGroup.contains(material)) {
                    tierGroup.add(material);
                    tierItemsCopy.remove(i);
                    i--;
                }
            }

            if (!tierGroup.isEmpty()) {
                tierGroups.add(tierGroup);
            }
        }

        return tierGroups;
    }

    /**
     * Get the set of groups (where each group is a set of items/materials) from a material groups file
     * which contains a line per group, which is represented as a '|'-separated list of items.
     *
     * @param path The path at which the file resides
     * @return The collection of groups that are stored in the given file
     */
    private Set<Set<Material>> readGlobalGroupsFile(String path) {
        String fileString = FileUtil.readFileToString(path + "groups.txt");
        if (fileString == null) {
            Game.getLogger().severe("Could not read material groups file " + "groups.txt");
            return new HashSet<>();
        }

        Set<Set<Material>> groups = new HashSet<>();

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
     * Loops over the tiers, going over the groups of items in that tier in random order, picking the first num(tier)
     * materials that are not excluded either by being in the blacklist or by sharing a group with an already picked
     * material. If this leads to no options remaining for a tier, the process is restarted.
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

        record Tier(List<List<Material>> groups, int numRequired) {
        }

        Tier[] tiers = new Tier[] {
                new Tier(sTierGroups.stream().map(ArrayList::new).collect(Collectors.toList()), numSTier),
                new Tier(aTierGroups.stream().map(ArrayList::new).collect(Collectors.toList()), numATier),
                new Tier(bTierGroups.stream().map(ArrayList::new).collect(Collectors.toList()), numBTier),
                new Tier(cTierGroups.stream().map(ArrayList::new).collect(Collectors.toList()), numCTier),
                new Tier(dTierGroups.stream().map(ArrayList::new).collect(Collectors.toList()), numDTier),
        };

        // Try to create a selection (trying again if the random picking results in no options remaining)
        for (int attempt = 0; attempt < MAX_MATERIAL_PICK_TRIES + 1; attempt++) { // +1 to disregard groups in last try
            boolean ignoreGroups = attempt == MAX_MATERIAL_PICK_TRIES;
            Game.getLogger().info("Starting attempt " + (attempt + 1) + " at material picking");
            if (ignoreGroups) Game.getLogger().warning("Ignoring grouping as last resort to pick items");

            List<Material> result = new ArrayList<>();
            Set<Material> exclude = new HashSet<>();
            if (game.getConfig().isBlacklistEnabled()) {
                exclude.addAll(blacklist);
            }

            for (Tier tier : tiers) {
                // Randomize the order of the groups in the tier
                Collections.shuffle(tier.groups, random);

                // Pick as many of this tier as required, skipping excluded items
                int numLeftInTier = tier.numRequired;

                // Start with looping over the groups from the tier
                for (List<Material> materials : tier.groups) {
                    if (numLeftInTier == 0) {
                        break;
                    }

                    // Also randomize the order of the items in the group in the tier
                    Collections.shuffle(materials, random);

                    // Now loop over the materials in the group and find the first material from that group that
                    // isn't excluded already
                    for (Material material : materials) {
                        if (exclude.contains(material)) {
                            Game.getLogger().info("Skipping excluded material " + material + " (" + exclude.size() + " excluded)");
                            continue;
                        }

                        // Include the material
                        result.add(material);
                        numLeftInTier--;

                        if (!ignoreGroups) {
                            // Add the materials that the picked material shares a group with to the excluded materials,
                            // to prevent picking multiple materials that share a group
                            var addToExclude = materialToGlobalGroupMap.getOrDefault(material, new HashSet<>());
                            exclude.addAll(addToExclude);
                            Game.getLogger().info("Included material " + material +
                                    ", excluding " + addToExclude.size() + " more group materials");
                        } else {
                            Game.getLogger().info("Included material " + material);
                        }

                        break;
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
