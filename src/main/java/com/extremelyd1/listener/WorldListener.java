package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class WorldListener implements Listener {

    private final Game game;

    public WorldListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        
    }

}
