package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {

    private final Game game;

    public WorldListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        // Check if there exists a location that we are portalling to
        if (e.getTo() == null) {
            return;
        }

        // Check if the both portal ends have a world
        if (e.getTo().getWorld() == null
                || e.getFrom().getWorld() == null) {
            return;
        }

        World.Environment toEnvironment = e.getTo().getWorld().getEnvironment();
        World.Environment fromEnvironment = e.getFrom().getWorld().getEnvironment();

        // Check whether we are travelling from/to overworld to/from nether
        if (toEnvironment.equals(fromEnvironment)
                || toEnvironment.equals(World.Environment.THE_END)
                || fromEnvironment.equals(World.Environment.THE_END)) {
            return;
        }

        // Get the centers of the world borders for both overworld and nether
        Location worldCenterOffset = game.getWorldManager().getWorld().getWorldBorder().getCenter();
        Location netherCenterOffset = game.getWorldManager().getNether().getWorldBorder().getCenter();

        // The ratio of overworld border size and nether border size
        double ratio = (float) game.getConfig().getOverworldBorderSize() / game.getConfig().getNetherBorderSize();

        double x = e.getTo().getX();
        double z = e.getTo().getZ();

        // Offset result location by world border centers in both dimensions
        if (toEnvironment.equals(World.Environment.NETHER)) {
            // Initial multiplication by 8 is because the to location is already divided by 8
            x = (x * 8.0D - worldCenterOffset.getX()) / ratio + netherCenterOffset.getX();
            z = (z * 8.0D - worldCenterOffset.getZ()) / ratio + netherCenterOffset.getZ();
        } else if (toEnvironment.equals(World.Environment.NORMAL)) {
            // Initial division by 8 is because the to location is already multiplied by 8
            x = (x / 8.0D - netherCenterOffset.getX()) * ratio + worldCenterOffset.getX();
            z = (z / 8.0D - netherCenterOffset.getZ()) * ratio + worldCenterOffset.getZ();
        }

        e.getTo().setX(x);
        e.getTo().setZ(z);
    }

}
