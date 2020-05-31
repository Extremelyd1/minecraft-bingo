package com.extremelyd1.bingo.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class ImageRenderer extends MapRenderer {

    private final BufferedImage image;

    public ImageRenderer(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        mapCanvas.drawImage(0, 0, image);
    }
}
