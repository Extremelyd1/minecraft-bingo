package com.extremelyd1.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class TimeUtil {

    /**
     * Broadcasts a time left message to all online players
     * This method decides when to send the message
     * @param timeLeft The time left in seconds
     */
    public static void broadcastTimeLeft(long timeLeft) {
        Component message = Component.text("Time left: ");

        long minutesLeft = timeLeft / 60;

        // More than 5 minutes left
        if (timeLeft > 5 * 60) {
            // Broadcast every 5 minutes
            if (timeLeft % (5 * 60) == 0) {
                message = message.append(Component
                        .text(minutesLeft)
                        .color(NamedTextColor.YELLOW)
                ).append(Component
                        .text(" minutes")
                        .color(NamedTextColor.WHITE)
                );

                Bukkit.broadcast(message);
            }
        } else if (timeLeft > 10) {
            // Less than 5 minutes left, more than 10 seconds
            // Broadcast every minute
            if (timeLeft % 60 == 0) {
                String minutes = "minutes";
                if (timeLeft == 60) {
                    minutes = "minute";
                }
                message = message.append(Component
                        .text(minutesLeft)
                        .color(NamedTextColor.YELLOW)
                ).append(Component
                        .text(" " + minutes)
                        .color(NamedTextColor.WHITE)
                );

                Bukkit.broadcast(message);
            }
        } else if (timeLeft > 0) {
            // At most 10 seconds left
            String seconds = "seconds";
            if (timeLeft == 1) {
                seconds = "second";
            }
            message = message.append(Component
                    .text(timeLeft)
                    .color(NamedTextColor.YELLOW)
            ).append(Component
                    .text(" " + seconds)
                    .color(NamedTextColor.WHITE)
            );

            Bukkit.broadcast(message);
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

    /**
     * Try to parse the given argument into seconds, given it is in the format
     * [integer h][integer m][integer s]
     * @param argument The argument to parse
     * @return An integer representing the number of seconds or -1 if not parsable
     */
    public static int parseTimeArgument(String argument) {
        // If there is no "h", "m" or "s" in the string, we cannot parse it
        if (!argument.contains("h") && !argument.contains("m") && !argument.contains("s")) {
            return -1;
        }

        // Keep track of result in seconds
        int result = 0;

        // Check whether there is an "h" in the string
        int hourIndex = argument.indexOf("h");
        if (hourIndex > 0) {
            String hourString = argument.substring(0, hourIndex);
            try {
                result += Integer.parseInt(hourString) * 3600;
            } catch (NumberFormatException e) {
                return -1;
            }
            argument = argument.substring(hourIndex + 1);
        } else if (hourIndex == 0) {
            // Provided an "h" but no integer before it
            return -1;
        }

        // Check whether there is an "m" in the string
        int minuteIndex = argument.indexOf("m");
        if (minuteIndex > 0) {
            String minuteString = argument.substring(0, minuteIndex);
            try {
                result += Integer.parseInt(minuteString) * 60;
            } catch (NumberFormatException e) {
                return -1;
            }
            argument = argument.substring(minuteIndex + 1);
        } else if (minuteIndex == 0) {
            // Provided an "m" but no integer before it
            return -1;
        }

        // Check whether there is an "s" in the string
        int secondIndex = argument.indexOf("s");
        if (secondIndex > 0) {
            String secondString = argument.substring(0, secondIndex);
            try {
                result += Integer.parseInt(secondString);
            } catch (NumberFormatException e) {
                return -1;
            }
        } else if (secondIndex == 0) {
            // Provided an "s" but no integer before it
            return -1;
        }

        return result;
    }

}
