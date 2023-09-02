package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class WorldListener implements Listener {

    private final Game game;

    public WorldListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        // Check if the both portal ends have a world
        if (e.getTo().getWorld() == null
                || e.getFrom().getWorld() == null) {
            Game.getLogger().warning("Player portal event called, but either world is null");
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

        double xFrom = e.getFrom().getX();
        double zFrom = e.getFrom().getZ();

        double x;
        double z;

        // Offset result location by world border centers in both dimensions
        if (toEnvironment.equals(World.Environment.NETHER)) {
            // First translate by local border center, divide by ratio and then translate by target border center
            x = (xFrom - worldCenterOffset.getX()) / ratio + netherCenterOffset.getX();
            z = (zFrom - worldCenterOffset.getZ()) / ratio + netherCenterOffset.getZ();
        } else if (toEnvironment.equals(World.Environment.NORMAL)) {
            // First translate by local border center, multiply by ratio and then translate by target border center
            x = (xFrom - netherCenterOffset.getX()) * ratio + worldCenterOffset.getX();
            z = (zFrom - netherCenterOffset.getZ()) * ratio + worldCenterOffset.getZ();
        } else {
            return;
        }

        e.getTo().setX(x);
        e.getTo().setZ(z);
    }

}
