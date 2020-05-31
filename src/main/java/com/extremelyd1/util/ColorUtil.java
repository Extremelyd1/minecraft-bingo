package com.extremelyd1.util;

public class ColorUtil {

    public static int getFromRgb(int r, int g, int b) {
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;

        return rgb;
    }

}
