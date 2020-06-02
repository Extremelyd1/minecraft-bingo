package com.extremelyd1.bingo.item;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.FileUtil;
import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BingoItemMaterials {

    private final String S_TIER_FILE_NAME = "s_tier.txt";
    private final String A_TIER_FILE_NAME = "a_tier.txt";
    private final String B_TIER_FILE_NAME = "b_tier.txt";
    private final String C_TIER_FILE_NAME = "c_tier.txt";
    private final String D_TIER_FILE_NAME = "d_tier.txt";

    private final String BLACKLIST_FILE_NAME = "blacklist.txt";


    private final Game game;

    private List<BingoItemSet> sTierItemSets;
    private List<BingoItemSet> aTierItemSets;
    private List<BingoItemSet> bTierItemSets;
    private List<BingoItemSet> cTierItemSets;
    private List<BingoItemSet> dTierItemSets;

    public BingoItemMaterials(Game game) {
        this.game = game;
    }

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
                game.getLogger().warning(
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

    public BingoItemSet createBingoItemSet(String line, String fileName) {
        List<Material> itemSetMaterials = new ArrayList<>();
        for (String material : line.split("\\|")) {
            if (material.isEmpty()) {
                continue;
            }

            try {
                itemSetMaterials.add(Material.valueOf(material));
            } catch (IllegalArgumentException e) {
                game.getLogger().warning(
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

    public void filterBingoItemSets(List<BingoItemSet> bingoItemSets, List<Material> blacklist) {
        for (int i = 0; i < bingoItemSets.size(); i++) {
            BingoItemSet bingoItemSet = bingoItemSets.get(i);
            if (!bingoItemSet.filter(blacklist)) {
                bingoItemSets.remove(i);
                i--;
            }
        }
    }

    public List<Material> pickMaterials() {
        return pickMaterials(
                game.getConfig().getNumSTier(),
                game.getConfig().getNumATier(),
                game.getConfig().getNumBTier(),
                game.getConfig().getNumCTier(),
                game.getConfig().getNumDTier()
        );
    }

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

    private BingoItemSet pickRandomFromItemSets(List<BingoItemSet> bingoItemSets, Random random) {
        return bingoItemSets.get(
                random.nextInt(bingoItemSets.size())
        );
    }

}
