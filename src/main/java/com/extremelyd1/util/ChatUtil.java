package com.extremelyd1.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class ChatUtil {
    /**
     * Formats the given material into a nice human-readable string.
     * @param material The material to format.
     * @return A nice human-readable string.
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
        return Component.empty().append(Component
                .text(" ".repeat(80))
                .color(NamedTextColor.BLUE)
                .decorate(TextDecoration.STRIKETHROUGH)
        );
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
