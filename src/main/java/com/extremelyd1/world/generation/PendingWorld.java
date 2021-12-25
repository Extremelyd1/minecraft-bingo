package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PendingWorld {

    /**
     * The folder where this world resides
     */
    protected File worldFolder;

    public PendingWorld() {
    }

    /**
     * Delete the world folder
     */
    public void deleteWorldFolder() {
        Game.getLogger().info("Deleting world folder for " + toString());
        try {
            FileUtils.deleteDirectory(worldFolder);
        } catch (IOException e) {
            Game.getLogger().warning("Could not delete world folder for " + toString() + ":");
            e.printStackTrace();
        }
    }

    public File getWorldFolder() {
        return worldFolder;
    }

    public void setWorldFolder(File worldFolder) {
        this.worldFolder = worldFolder;
    }
}
