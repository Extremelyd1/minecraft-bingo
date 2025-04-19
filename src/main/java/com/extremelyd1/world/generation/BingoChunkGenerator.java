package com.extremelyd1.world.generation;

import com.extremelyd1.game.Game;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Chunk generator that generates only barrier blocks in all chunks.
 */
public class BingoChunkGenerator extends ChunkGenerator {
    /**
     * The game instance to get access to the worlds.
     */
    private final Game game;

    public BingoChunkGenerator(Game game) {
        this.game = game;
    }

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // If we don't use vanilla generation, we fill the chunk with barrier blocks instead for fast generation
        if (!shouldGenerate(worldInfo, chunkX, chunkZ)) {
            chunkData.setRegion(0, chunkData.getMinHeight(), 0, 16, chunkData.getMaxHeight(), 16, Material.BARRIER);
        }
    }

    /**
     * Whether to use vanilla generation for the given chunk in the world with the given world info.
     * @param worldInfo The world info for the generation.
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @return True if vanilla generation should be used, false otherwise.
     */
    private boolean shouldGenerate(@NotNull WorldInfo worldInfo, int chunkX, int chunkZ) {
        if (game.getWorldManager() == null) {
            return true;
        }

        World world = Bukkit.getWorld(worldInfo.getName());
        if (world == null) {
            return true;
        }

        WorldBorder border = world.getWorldBorder();

        return border.isInside(new Location(world, chunkX * 16, 0, chunkZ * 16)) ||
                border.isInside(new Location(world, (chunkX + 1) * 16, 0, chunkZ * 16)) ||
                border.isInside(new Location(world, (chunkX + 1) * 16, 0, (chunkZ + 1) * 16)) ||
                border.isInside(new Location(world, chunkX * 16, 0, (chunkZ + 1) * 16));
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return shouldGenerate(worldInfo, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return shouldGenerate(worldInfo, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return shouldGenerate(worldInfo, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return shouldGenerate(worldInfo, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return shouldGenerate(worldInfo, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return shouldGenerate(worldInfo, chunkX, chunkZ);
    }
}
