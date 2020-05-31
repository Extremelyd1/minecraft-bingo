package com.extremelyd1.bingo;

import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BingoItemMaterials {

    private final List<Material> materials;

    public BingoItemMaterials() {
        this.materials = new ArrayList<>();
    }

    public void loadMaterials(File dataFolder) {
        String path = dataFolder.getPath() + "/item_data/";

        File[] materialFiles = new File(path).listFiles();
        if (materialFiles == null) {
            return;
        }

        for (File file : materialFiles) {
            String fileName = file.getName().toUpperCase().replace(".PNG", "");
            try {
                materials.add(Material.valueOf(fileName));
            } catch (IllegalArgumentException e) {
                System.out.println("Could not find material with name: " + fileName);
            }
        }
    }

    public List<Material> getMaterials() {
        return materials;
    }

}
