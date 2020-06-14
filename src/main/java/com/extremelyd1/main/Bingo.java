package com.extremelyd1.main;

import com.extremelyd1.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Bingo extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Creating Game instance");
        try {
            new Game(this);
        } catch (IllegalArgumentException e) {
            getLogger().severe("Could not start plugin");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
    }

}
