package com.extremelyd1.main;

import com.extremelyd1.game.Game;
import com.extremelyd1.world.generation.BingoChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Bingo extends JavaPlugin {
    /**
     * The game instance, used to pass to the chunk generator when it is requested.
     */
    private Game game;

    @Override
    public void onEnable() {
        getLogger().info("Creating Game instance");
        try {
            this.game = new Game(this);
            getLogger().info("Game instance created");
        } catch (IllegalArgumentException e) {
            getLogger().severe(String.format("Could not start plugin: %s", e.getMessage()));
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return new BingoChunkGenerator(game);
    }
}
