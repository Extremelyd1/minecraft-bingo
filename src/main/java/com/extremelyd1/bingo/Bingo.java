package com.extremelyd1.bingo;

import com.extremelyd1.game.Game;
import org.bukkit.plugin.java.JavaPlugin;

public class Bingo extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Creating Game instance");
        new Game();
    }

    @Override
    public void onDisable() {
    }

}
