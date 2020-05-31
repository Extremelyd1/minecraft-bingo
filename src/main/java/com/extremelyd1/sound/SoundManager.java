package com.extremelyd1.sound;

import com.extremelyd1.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    public SoundManager() {
    }

    public void broadcastItemCollected() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_ANVIL_USE,
                    10f,
                    1f
            );
        }
    }

    public void broadcastStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_BELL_USE,
                    10f,
                    1f
            );
        }
    }

    public void broadcastEnd() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_PORTAL_TRAVEL,
                    10f,
                    1f
            );
        }
    }

}
