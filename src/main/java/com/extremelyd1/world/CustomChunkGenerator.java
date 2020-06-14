package com.extremelyd1.world;

import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class CustomChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
//        worldServer.getChunkProvider().getChunkGenerator().
        return createChunkData(world);
    }
}
