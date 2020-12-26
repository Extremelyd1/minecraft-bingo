package com.extremelyd1.sound;

import com.extremelyd1.game.team.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Manager class to send sounds to players
 */
public class SoundManager {

    public SoundManager() {
    }

    /**
     * Play the item collection sound for all players online
     * Different sound for the players of the team that collected the item than for others
     * @param team The team that collected the item
     */
    public void broadcastItemCollected(PlayerTeam team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (team.contains(player)) {
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

    /**
     * Broadcast the start sound
     */
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

    /**
     * Broadcast the end sound
     */
    public void broadcastEnd() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_PORTAL_TRAVEL,
                    0.1f,
                    1f
            );
        }
    }

}
