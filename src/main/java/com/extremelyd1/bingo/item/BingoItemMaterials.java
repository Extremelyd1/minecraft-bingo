package com.extremelyd1.bingo.item;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.FileUtil;
import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A class that handles loading/storing of material names from file
 * Also provides methods to create bingo item sets and randomly picking from these sets
 */
public class BingoItemMaterials {

    private final String S_TIER_FILE_NAME = "s_tier.txt";
    private final String A_TIER_FILE_NAME = "a_tier.txt";
    private final String B_TIER_FILE_NAME = "b_tier.txt";
    private final String C_TIER_FILE_NAME = "c_tier.txt";
    private final String D_TIER_FILE_NAME = "d_tier.txt";

    private final String BLACKLIST_FILE_NAME = "blacklist.txt";

    /**
     * The game instance
     */
    private final Game game;

    /**
     * A list of bingo item sets of S tier rarity
     */
    private List<BingoItemSet> sTierItemSets;
    /**
     * A list of bingo item sets of A tier rarity
     */
    private List<BingoItemSet> aTierItemSets;
    /**
     * A list of bingo item sets of B tier rarity
     */
    private List<BingoItemSet> bTierItemSets;
    /**
     * A list of bingo item sets of C tier rarity
     */
    private List<BingoItemSet> cTierItemSets;
    /**
     * A list of bingo item sets of D tier rarity
     */
    private List<BingoItemSet> dTierItemSets;

    public BingoItemMaterials(Game game) {
        this.game = game;
    }

    /**
     * Load the materials of all tiers (and blacklist if enabled) into the data structures
     * @param dataFolder The data folder in which the data files are stored
     */
    public void loadMaterials(File dataFolder) {
        String path = dataFolder.getPath() + "/item_data/";

        this.sTierItemSets = createBingoItemSetList(path, S_TIER_FILE_NAME);
        this.aTierItemSets = createBingoItemSetList(path, A_TIER_FILE_NAME);
        this.bTierItemSets = createBingoItemSetList(path, B_TIER_FILE_NAME);
        this.cTierItemSets = createBingoItemSetList(path, C_TIER_FILE_NAME);
        this.dTierItemSets = createBingoItemSetList(path, D_TIER_FILE_NAME);

        if (game.getConfig().isBlacklistEnabled()) {
            List<Material> blacklistedMaterials = createMaterialList(path, BLACKLIST_FILE_NAME);

            filterBingoItemSets(this.sTierItemSets, blacklistedMaterials);
            filterBingoItemSets(this.aTierItemSets, blacklistedMaterials);
            filterBingoItemSets(this.bTierItemSets, blacklistedMaterials);
            filterBingoItemSets(this.cTierItemSets, blacklistedMaterials);
            filterBingoItemSets(this.dTierItemSets, blacklistedMaterials);
        }
    }

    /**
     * Create a list of materials that are stored in the file at the given path with the given file name
     * @param path The path at which the file resides
     * @param fileName The name of the file
     * @return A list of materials that are stored in the given file
     */
    public List<Material> createMaterialList(String path, String fileName) {
        String fileString = FileUtil.readFileToString(path + fileName);
        if (fileString == null) {
            return null;
        }

        List<Material> materialList = new ArrayList<>();

        for (String line : fileString.split("\n")) {
            try {
                materialList.add(Material.valueOf(line));
            } catch (IllegalArgumentException e) {
                Game.getLogger().warning(
                        String.format(
                                "Could not find material with name %s in file %s",
                                line,
                                fileName
                        )
                );
            }
        }

        return materialList;
    }

    /**
     * Create a list of bingo item sets that are stored in the file at the given path with the given file name
     * @param path The path at which the file resides
     * @param fileName The name of the file
     * @return A list of BingoItemSet instances that are stored in the given file
     */
    public List<BingoItemSet> createBingoItemSetList(String path, String fileName) {
        String fileString = FileUtil.readFileToString(path + fileName);
        if (fileString == null) {
            return null;
        }

        List<BingoItemSet> bingoItemSets = new ArrayList<>();

        for (String line : fileString.split("\n")) {
            bingoItemSets.add(createBingoItemSet(line, fileName));
        }

        return bingoItemSets;
    }

    /**
     * Create a single bingo item set given a line from the data file
     * @param line The line containing the data
     * @param fileName The name of the file of this line
     * @return A BingoItemSet instance
     */
    public BingoItemSet createBingoItemSet(String line, String fileName) {
        List<Material> itemSetMaterials = new ArrayList<>();
        for (String material : line.split("\\|")) {
            if (material.isEmpty()) {
                continue;
            }

            try {
                itemSetMaterials.add(Material.valueOf(material));
            } catch (IllegalArgumentException e) {
                Game.getLogger().warning(
                        String.format(
                                "Could not find material with name %s in file %s",
                                material,
                                fileName
                        )
                );
            }
        }

        return new BingoItemSet(itemSetMaterials);
    }

    /**
     * Filters the given list of bingo item sets and removes items from the item set if they are
     * contained in the given blacklist. Also removes bingo item sets entirely if they are empty after filtering
     * @param bingoItemSets The list of bingo item sets to filter
     * @param blacklist The blacklist on which to filter
     */
    public void filterBingoItemSets(List<BingoItemSet> bingoItemSets, List<Material> blacklist) {
        for (int i = 0; i < bingoItemSets.size(); i++) {
            BingoItemSet bingoItemSet = bingoItemSets.get(i);
            if (!bingoItemSet.filter(blacklist)) {
                bingoItemSets.remove(i);
                i--;
            }
        }
    }

    /**
     * Randomly pick a list of materials from the tiers based on the distribution denoted by the config values
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
     * Randomly pick a list of materials from the tiers
     * @param numSTier The number of S tier materials to pick
     * @param numATier The number of A tier materials to pick
     * @param numBTier The number of B tier materials to pick
     * @param numCTier The number of C tier materials to pick
     * @param numDTier The number of D tier materials to pick
     * @return A list of materials
     */
    public List<Material> pickMaterials(
            int numSTier,
            int numATier,
            int numBTier,
            int numCTier,
            int numDTier
    ) {
        List<Material> result = new ArrayList<>();

        Random random = new Random();

        pickNumberOfRandomFromItemSets(sTierItemSets, result, numSTier, random);
        pickNumberOfRandomFromItemSets(aTierItemSets, result, numATier, random);
        pickNumberOfRandomFromItemSets(bTierItemSets, result, numBTier, random);
        pickNumberOfRandomFromItemSets(cTierItemSets, result, numCTier, random);
        pickNumberOfRandomFromItemSets(dTierItemSets, result, numDTier, random);

        return result;
    }

    /**
     * Randomly pick a number of materials from the given bingo item set and store them in result
     * @param bingoItemSets The list of bingo item sets to choose from
     * @param result The list of results in which to store the picked materials
     * @param number The number of materials to pick
     * @param random Instance of Random to use
     */
    private void pickNumberOfRandomFromItemSets(
            List<BingoItemSet> bingoItemSets,
            List<Material> result,
            int number,
            Random random
    ) {
        if (number > bingoItemSets.size()) {
            throw new IllegalArgumentException("Cannot pick more than the total number of items");
        }

        List<BingoItemSet> bingoItemSetsLeft = new ArrayList<>(bingoItemSets);

        for (int i = 0; i < number; i++) {
            BingoItemSet randomItemSet = pickRandomFromItemSets(bingoItemSetsLeft, random);
            result.add(randomItemSet.pick());

            bingoItemSetsLeft.remove(randomItemSet);
        }
    }

    /**
     * Pick a random bingo item set from a list of bingo item sets
     * @param bingoItemSets The list to pick from
     * @param random Instance of Random to use
     * @return A random bingo item set
     */
    private BingoItemSet pickRandomFromItemSets(List<BingoItemSet> bingoItemSets, Random random) {
        return bingoItemSets.get(
                random.nextInt(bingoItemSets.size())
        );
    }

}
