package com.extremelyd1.util;

import org.bukkit.Material;

public class StringUtil {

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

    public static String ellipsString(String input, int maxSize) {
        if (input.length() < maxSize) {
            return input;
        }

        if (maxSize < 4) {
            throw new IllegalArgumentException("Cannot ellips string to max length of 3 of less");
        }

        return input.substring(0, maxSize - 3) + "...";
    }

}
