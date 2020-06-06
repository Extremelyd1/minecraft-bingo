package com.extremelyd1.util;

public class ColorUtil {

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

}
