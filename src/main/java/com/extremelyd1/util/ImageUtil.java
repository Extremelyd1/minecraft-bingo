package com.extremelyd1.util;

import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {

    public static BufferedImage readItemImage(File dataFolder, Material material) {
        String filePath = dataFolder.getPath()
                + "/item_data/"
                + material.name().toLowerCase()
                + ".png";

        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

}
