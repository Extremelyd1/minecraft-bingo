package com.extremelyd1.main;

import com.extremelyd1.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Bingo extends JavaPlugin {

    @Override
    public void onEnable() {
        System.setProperty("java.awt.headless", "true");
        getLogger().info("Creating Game instance");
        try {
            new Game(this);
        } catch (IllegalArgumentException e) {
            getLogger().severe(String.format("Could not start plugin: %s", e.getMessage()));
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
    }

}
