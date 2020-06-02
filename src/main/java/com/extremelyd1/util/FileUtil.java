package com.extremelyd1.util;

import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileUtil {

    public static BufferedImage readItemImage(File dataFolder, Material material) {
        String filePath = dataFolder.getPath()
                + "/item_data/images/"
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

    public static String readFileToString(String path) {
        try {
            return Files.lines(Paths.get(path)).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeStringToFile(String path, String value) {
        try (PrintStream out = new PrintStream(new FileOutputStream(path))) {
            out.print(value);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
