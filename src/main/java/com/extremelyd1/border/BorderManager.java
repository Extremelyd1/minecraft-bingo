package com.extremelyd1.border;

import com.extremelyd1.config.Config;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class BorderManager {

    public BorderManager(Config config, World world) {
        if (config.isBorderEnabled()) {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(world.getSpawnLocation());
            border.setSize(config.getBorderSize());
        }
    }

}
