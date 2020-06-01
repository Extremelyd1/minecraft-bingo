package com.extremelyd1.sound;

import com.extremelyd1.game.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    public SoundManager() {
    }

    public void broadcastItemCollected(Team team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (team.getPlayers().contains(player)) {
                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_PLAYER_LEVELUP,
                        1f,
                        2f
                );
            } else {
                player.playSound(
                        player.getLocation(),
                        Sound.BLOCK_NOTE_BLOCK_BASS,
                        1f,
                        1f
                );
            }
        }
    }

    public void broadcastStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (float pitch = 0f; pitch <= 2f; pitch += 1f) {
                player.playSound(
                        player.getLocation(),
                        Sound.BLOCK_NOTE_BLOCK_BELL,
                        1f,
                        pitch
                );
            }
        }
    }

    public void broadcastEnd() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_PORTAL_TRAVEL,
                    1f,
                    1f
            );
        }
    }

}
