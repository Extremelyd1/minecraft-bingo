package com.extremelyd1.util;

import com.extremelyd1.game.Game;
import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    /**
     * Packages a file/directory into a zip.
     * @param zipFile The destination zip file.
     * @param file The file or directory to zip.
     * @param dirName If the file is a directory, the name of the directory in the zip.
     */
    public static void packZip(File zipFile, File file, String dirName) {
        packZip(zipFile, "", file, dirName);
    }

    /**
     * Packages a file/directory into a zip
     * @param zipFile The destination zip file
     * @param startPath The path to store the file/directory at
     * @param file A file or directory to zip
     * @param dirName If the file is a directory, the name of the directory in the zip
     */
    public static void packZip(File zipFile, String startPath, File file, String dirName) {
        Game.getLogger().info(String.format(
                "Packaging %s%s into zip %s",
                (startPath.equals("") ? "" : startPath + "/"),
                (dirName.equals("") ? file.getName() : dirName),
                zipFile.getName()
        ));

        try {
            if (!zipFile.exists()) {
                if (!zipFile.createNewFile()) {
                    Game.getLogger().warning(String.format(
                            "Could not create zip file '%s', skipping it",
                            zipFile.getName()
                    ));

                    return;
                }
            } else {
                Game.getLogger().info(String.format(
                        "Zip file '%s' already exists, overwriting contents",
                        zipFile.getName()
                ));
            }

            // Create a temp file
            File tempFile = File.createTempFile(zipFile.getName(), null, zipFile.getParentFile());

            // Delete it to rename existing zip to it
            if (!tempFile.delete()) {
                Game.getLogger().info("Could not delete temp file for zip");
            }

            Files.move(zipFile.toPath(), tempFile.toPath());

            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(tempFile));
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);

            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String name = entry.getName();

                // Add existing zip entry to output zip
                zipOut.putNextEntry(new ZipEntry(name));

                // Transfer bytes from existing zip to output zip
                byte[] buffer = new byte[4092];
                int byteCount = 0;
                while ((byteCount = zipIn.read(buffer)) != -1) {
                    zipOut.write(buffer, 0, byteCount);
                }

                zipOut.closeEntry();
                entry = zipIn.getNextEntry();
            }

            // Close stream
            zipIn.close();

            if (file.isDirectory()) {
                zipDirectory(zipOut, startPath, file, dirName);
            } else {
                zipFile(zipOut, startPath, file);
            }

            // Close output stream
            zipOut.flush();
            zipOut.close();

            // Delete temp file
            Files.delete(tempFile.toPath());
        } catch (IOException e) {
            Game.getLogger().warning("Could not package files:");
            e.printStackTrace();
            return;
        }

        Game.getLogger().info("Done packaging");
    }

    /**
     * Adds a directory to a zip file
     * @param zos The zip file
     * @param path The path to which to write the file
     * @param dir The directory
     */
    private static void zipDirectory(ZipOutputStream zos, String path, File dir) throws IOException {
        zipDirectory(zos, path, dir, "");
    }

    /**
     * Adds a directory to a zip file
     * @param zos The zip file
     * @param path The path to which to write the file
     * @param dir The directory
     * @param dirName The name the directory should have in the zip
     */
    private static void zipDirectory(ZipOutputStream zos, String path, File dir, String dirName) throws IOException {
        if (!dir.isDirectory()) {
            return;
        }

        if (!dir.canRead()) {
            Game.getLogger().warning(
                    "Cannot read " + dir.getName() + " to zip"
            );

            return;
        }

        File[] files = dir.listFiles();
        if (dirName.equals("")) {
            dirName = dir.getName();
        }
        path = buildPath(path, dirName);

        Game.getLogger().info("Adding directory " + path + " to zip");

        for (File source : files) {
            if (source.isDirectory()) {
                zipDirectory(zos, path, source);
            } else {
                zipFile(zos, path, source);
            }
        }

        Game.getLogger().info("Leaving directory " + path);
    }

    /**
     * Adds a file to a zip file
     * @param zos The zip file
     * @param path The path to which to write the file
     * @param file The file
     */
    private static void zipFile(ZipOutputStream zos, String path, File file) throws IOException {
        if (!file.canRead()) {
            Game.getLogger().warning(
                    "Cannot read file " + file.getName() + " to zip"
            );

            return;
        }

        Game.getLogger().info("Compressing " + file.getName());
        zos.putNextEntry(new ZipEntry(buildPath(path, file.getName())));

        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[4092];
        int byteCount = 0;
        while ((byteCount = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, byteCount);
        }

        fis.close();
        zos.closeEntry();
    }

    /**
     * Builds a path string from the given existing path string and a file
     * @param path The existing path string
     * @param file The file to append to this path string
     * @return A string containing a path
     */
    private static String buildPath(String path, String file) {
        if (path == null || path.isEmpty()) {
            return file;
        } else {
            return path + "/" + file;
        }
    }

}
