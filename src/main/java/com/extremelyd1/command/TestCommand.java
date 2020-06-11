package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.StructureBoundingBox;
import net.minecraft.server.v1_15_R1.StructureStart;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;

public class TestCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public TestCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!CommandUtil.checkCommandSender(sender)) {
            return true;
        }

        Player player = (Player) sender;

        Location strongholdLocation = game.getWorldManager().getWorld().locateNearestStructure(
                player.getLocation(),
                StructureType.STRONGHOLD,
                10000,
                false
        );

        if (strongholdLocation == null) {
            player.sendMessage("Not found");

            return true;
        }

        player.sendMessage("Location: " + strongholdLocation.getX() + ", "
                + strongholdLocation.getY() + ", " + strongholdLocation.getZ());

        CraftChunk craftChunk = (CraftChunk) game.getWorldManager().getWorld().getChunkAt(strongholdLocation);

        for (Field field : craftChunk.getClass().getFields()) {
            System.out.println("CraftChunk field: " + field.getName());
        }
        for (Field field : craftChunk.getClass().getDeclaredFields()) {
            System.out.println("CraftChunk declared field: " + field.getName());
        }

        Field weakChunkField = null;

        try {
            weakChunkField = craftChunk.getClass().getDeclaredField("weakChunk");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        if (weakChunkField == null) {
            System.out.println("Weak chunk field is null");

            return true;
        }

        weakChunkField.setAccessible(true);

        Object objectInField = null;

        try {
            objectInField = weakChunkField.get(craftChunk);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (objectInField == null) {
            System.out.println("Value in field null");
            return true;
        }

        if (!(objectInField instanceof WeakReference)) {
            System.out.println("Not a weak reference");
            return true;
        }

        WeakReference<Chunk> weakReference = (WeakReference<Chunk>) objectInField;

        Chunk chunk = weakReference.get();
        if (chunk == null) {
            System.out.println("Chunk is null");
            return true;
        }

        for (Field field : chunk.getClass().getFields()) {
            System.out.println("Chunk field: " + field.getName());
        }
        for (Field field : chunk.getClass().getDeclaredFields()) {
            System.out.println("Chunk declared field: " + field.getName());
        }

        Field structureStartMapField = null;

        try {
            structureStartMapField = chunk.getClass().getDeclaredField("l");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        if (structureStartMapField == null) {
            System.out.println("structure start map field is null");
            return true;
        }

        structureStartMapField.setAccessible(true);

        try {
            objectInField = structureStartMapField.get(chunk);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Map<String, StructureStart> structureStartMap = (Map<String, StructureStart>) objectInField;

        if (structureStartMap == null) {
            System.out.println("structure start map is null");
            return true;
        }

        for (String key : structureStartMap.keySet()) {
            System.out.println("Key=" + key);
            System.out.println("Value=" + structureStartMap.get(key));
        }

        StructureStart structureStart = structureStartMap.get("Stronghold");

        StructureBoundingBox boundingBox = structureStart.c();

        System.out.println("Stronghold bounding box:");
        System.out.println("  Min x=" + boundingBox.a);
        System.out.println("  Min y=" + boundingBox.b);
        System.out.println("  Min z=" + boundingBox.c);

        System.out.println("  Max x=" + boundingBox.d);
        System.out.println("  Max y=" + boundingBox.e);
        System.out.println("  Max z=" + boundingBox.f);

        return true;
    }
}
