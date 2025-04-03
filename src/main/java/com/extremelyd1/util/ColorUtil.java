package com.extremelyd1.util;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;

public class ColorUtil {

    private static final Map<NamedTextColor, Integer> textColorIntegers = new HashMap<>() {{
        put(NamedTextColor.BLACK, getFromRgb(0, 0, 0));
        put(NamedTextColor.DARK_BLUE, getFromRgb(0, 0, 170));
        put(NamedTextColor.DARK_GREEN, getFromRgb(0, 170, 0));
        put(NamedTextColor.DARK_AQUA, getFromRgb(0, 170, 170));
        put(NamedTextColor.DARK_RED, getFromRgb(170, 0, 0));
        put(NamedTextColor.DARK_PURPLE, getFromRgb(170, 0, 170));
        put(NamedTextColor.GOLD, getFromRgb(170, 170, 0));
        put(NamedTextColor.GRAY, getFromRgb(170, 170, 170));
        put(NamedTextColor.DARK_GRAY, getFromRgb(85, 85, 85));
        put(NamedTextColor.BLUE, getFromRgb(85, 85, 255));
        put(NamedTextColor.GREEN, getFromRgb(127, 178, 56)); // Same as GRASS on maps (https://minecraft.fandom.com/wiki/Map_item_format)
        put(NamedTextColor.AQUA, getFromRgb(43, 199, 172));
        put(NamedTextColor.RED, getFromRgb(255, 0, 0));
        put(NamedTextColor.LIGHT_PURPLE, getFromRgb(255, 85, 255));
        put(NamedTextColor.YELLOW, getFromRgb(255, 255, 85));
        put(NamedTextColor.WHITE, getFromRgb(255, 255, 255));
    }};

    /**
     * Get an integer value representing the given r, g, and b values of a color
     * @param r The red part of the color
     * @param g The green part of the color
     * @param b The blue part of the color
     * @return An integer value representing the color
     */
    public static int getFromRgb(int r, int g, int b) {
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;

        return rgb;
    }

    /**
     * Get an integer value representing the given NamedTextColor
     * @param color The NamedTextColor
     * @return An integer value representing the color
     */
    public static int textColorToInt(NamedTextColor color) {
        return textColorIntegers.get(color);
    }

}
