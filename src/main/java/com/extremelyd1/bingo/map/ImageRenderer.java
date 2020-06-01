package com.extremelyd1.bingo.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class ImageRenderer extends MapRenderer {

    private final BufferedImage image;
    private boolean isRendered;

    public ImageRenderer(BufferedImage image) {
        this.image = image;
        this.isRendered = false;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (isRendered) {
            return;
        }

        mapCanvas.drawImage(0, 0, image);

        isRendered = true;
    }
}
