package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodListener implements Listener {

    private final Game game;

    public FoodListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (!game.getState().equals(Game.State.IN_GAME)) {
            e.setCancelled(true);
        }
    }

}
