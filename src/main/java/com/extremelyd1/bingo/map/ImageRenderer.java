package com.extremelyd1.bingo.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

/**
 * Custom map renderer, that renders the map exactly once
 */
public class ImageRenderer extends MapRenderer {

    /**
     * The image to render
     */
    private BufferedImage image;
    /**
     * Whether this image has been rendered already
     */
    private boolean isRendered;

    public ImageRenderer(BufferedImage image) {
        this.image = image;
        this.isRendered = false;
    }

    /**
     * Renders the given image onto the map
     * @param image The image to render
     */
    public void renderNewImage(BufferedImage image) {
        this.image = image;
        this.isRendered = false;
    }

    @Override
    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
        if (isRendered) {
            return;
        }

        mapCanvas.drawImage(0, 0, image);

        isRendered = true;
    }
}
