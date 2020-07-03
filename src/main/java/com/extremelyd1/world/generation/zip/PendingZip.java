package com.extremelyd1.world.generation.zip;

import com.extremelyd1.util.FileUtil;
import com.extremelyd1.world.generation.PendingWorld;
import org.bukkit.World;

import java.io.File;

public class PendingZip extends PendingWorld {

    /**
     * The zip archive where this world folder needs to be added to
     */
    private final File zipFile;
    /**
     * The path at which this world folder needs to end up in the zip archive
     * This does not include the name of the folder in the zip archive
     */
    private final String zipStartPath;
    /**
     * The resulting name of the world folder inside the zip archive
     */
    private final String dirName;

    /**
     * The type of environment of this world
     * Used for pretty printing of this instance
     */
    private final World.Environment environment;
    /**
     * The index of the associated world
     * Used for pretty printing of this instance
     */
    private final int index;

    public PendingZip(
            File zipFile,
            String zipStartPath,
            File worldFolder,
            String dirName,
            World.Environment environment,
            int index
    ) {
        this.zipFile = zipFile;
        this.zipStartPath = zipStartPath;
        this.worldFolder = worldFolder;
        this.dirName = dirName;

        this.environment = environment;
        this.index = index;
    }

    /**
     * Zip this instance
     */
    public void zip() {
        FileUtil.packZip(zipFile, zipStartPath, worldFolder, dirName);
    }

    public String toString() {
        return environment.toString() + index;
    }
}
