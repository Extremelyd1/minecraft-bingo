package com.extremelyd1.util;

import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.team.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ChatUtil {
    /**
     * Formats the given material into a nice human-readable string
     * @param material The material to format
     * @return A nice human-readable string
     */
    public static String formatMaterialName(Material material) {
        String[] wordSplit = material.name().split("_");

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < wordSplit.length; i++) {
            String word = wordSplit[i];

            result.append(
                    word.substring(0, 1).toUpperCase()
            ).append(
                    word.substring(1).toLowerCase()
            );

            if (i != wordSplit.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    public static Component divider() {
        return Component.text("                                                                        ")
                .style(Style.style(TextDecoration.STRIKETHROUGH));
    }

    public static Component errorPrefix() {
        return Component.text("Error: ").color(NamedTextColor.DARK_RED);
    }

    public static Component waitPrefix() {
        return Component.text("Wait: ").color(NamedTextColor.GOLD);
    }

    public static Component successPrefix() {
        return Component.text("Success: ").color(NamedTextColor.GREEN);
    }
}
