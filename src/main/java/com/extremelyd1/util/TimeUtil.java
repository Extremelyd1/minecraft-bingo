package com.extremelyd1.util;

import com.extremelyd1.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class TimeUtil {

    /**
     * Broadcasts a time left message to all online players
     * This method decides when to send the message
     * @param timeLeft The time left in seconds
     */
    public static void broadcastTimeLeft(long timeLeft) {
        String message = Game.PREFIX + "Time left: " + ChatColor.YELLOW + "%d" + ChatColor.WHITE + " %s";
        long minutesLeft = timeLeft / 60;

        // More than 5 minutes left
        if (timeLeft > 5 * 60) {
            // Broadcast every 5 minutes
            if (timeLeft % (5 * 60) == 0) {
                message = String.format(message, minutesLeft, "minutes");

                Bukkit.broadcastMessage(message);
            }
        } else if (timeLeft > 10) {
            // Less than 5 minutes left, more than 10 seconds
            // Broadcast every minute
            if (timeLeft % 60 == 0) {
                String minutes = "minutes";
                if (timeLeft == 60) {
                    minutes = "minute";
                }
                message = String.format(message, minutesLeft, minutes);

                Bukkit.broadcastMessage(message);
            }
        } else if (timeLeft > 0) {
            // At most 10 seconds left
            String seconds = "seconds";
            if (timeLeft == 1) {
                seconds = "second";
            }
            message = String.format(message, timeLeft, seconds);

            Bukkit.broadcastMessage(message);
        }
    }

    /**
     * Format the time left in seconds to a nice human-readable string
     * @param timeLeft The time left in seconds
     * @return A nice human-readable string
     */
    public static String formatTimeLeft(long timeLeft) {
        long hours = timeLeft / (60 * 60);
        long minutes = timeLeft % (60 * 60) / 60;
        long seconds = timeLeft % (60 * 60) % 60;

        String timeString = "";

        if (hours > 0) {
            timeString += String.format("%02d:", hours);
        }
        timeString += String.format("%02d:%02d", minutes, seconds);

        return timeString;
    }

}
