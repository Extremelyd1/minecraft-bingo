package com.extremelyd1.util;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class ColorUtil {

    private static final Map<ChatColor, Integer> chatColorIntegers = new HashMap<ChatColor, Integer>() {{
        put(ChatColor.BLACK, getFromRgb(0, 0, 0));
        put(ChatColor.DARK_BLUE, getFromRgb(0, 0, 170));
        put(ChatColor.DARK_GREEN, getFromRgb(0, 170, 0));
        put(ChatColor.DARK_AQUA, getFromRgb(0, 170, 170));
        put(ChatColor.DARK_RED, getFromRgb(170, 0, 0));
        put(ChatColor.DARK_PURPLE, getFromRgb(170, 0, 170));
        put(ChatColor.GOLD, getFromRgb(170, 170, 0));
        put(ChatColor.GRAY, getFromRgb(170, 170, 170));
        put(ChatColor.DARK_GRAY, getFromRgb(85, 85, 85));
        put(ChatColor.BLUE, getFromRgb(85, 85, 255));
        put(ChatColor.GREEN, getFromRgb(85, 255, 85));
        put(ChatColor.AQUA, getFromRgb(43, 199, 172));
        put(ChatColor.RED, getFromRgb(255, 0, 0));
        put(ChatColor.LIGHT_PURPLE, getFromRgb(255, 85, 255));
        put(ChatColor.YELLOW, getFromRgb(255, 255, 85));
        put(ChatColor.WHITE, getFromRgb(255, 255, 255));
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
     * Get an integer value representing the given ChatColor
     * @param color The ChatColor
     * @return An integer value representing the color
     */
    public static int chatColorToInt(ChatColor color) {
        return chatColorIntegers.get(color);
    }

}
