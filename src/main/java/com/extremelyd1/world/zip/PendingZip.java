package com.extremelyd1.world.zip;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.FileUtil;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PendingZip {

    private final File zipFile;
    private final String zipStartPath;
    private final File worldFolder;
    private final String dirName;

    public PendingZip(File zipFile, String zipStartPath, File worldFolder, String dirName) {
        this.zipFile = zipFile;
        this.zipStartPath = zipStartPath;
        this.worldFolder = worldFolder;
        this.dirName = dirName;
    }

    public void zip() {
        FileUtil.packZip(zipFile, zipStartPath, worldFolder, dirName);
    }

    public void deleteWorldFolder() {
        Game.getLogger().info("Deleting world folder for " + toString());
        try {
            FileUtils.deleteDirectory(worldFolder);
        } catch (IOException e) {
            Game.getLogger().warning("Could not delete world folder for " + toString() + ":");
            e.printStackTrace();
        }
    }

    public String toString() {
        return "PendingZip[" + zipStartPath + ", " + dirName + "]";
    }
}
