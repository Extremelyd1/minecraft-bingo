package com.extremelyd1.util;

import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileUtil {

    /**
     * Reads a buffered image from the given data folder given the material
     * @param dataFolder The folder in which the image resides
     * @param material The material
     * @return A buffered image of this file
     */
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

    /**
     * Reads a file to string
     * @param path The path at which the file resides
     * @return The string value of the read data
     */
    public static String readFileToString(String path) {
        try {
            return Files.lines(Paths.get(path)).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Writes a string value to file
     * @param path The path to which to write
     * @param value The string value to write
     */
    public static void writeStringToFile(String path, String value) {
        try (PrintStream out = new PrintStream(new FileOutputStream(path))) {
            out.print(value);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
