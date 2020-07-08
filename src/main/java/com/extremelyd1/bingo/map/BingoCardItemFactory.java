package com.extremelyd1.bingo.map;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.game.Game;
import com.extremelyd1.util.ColorUtil;
import com.extremelyd1.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class BingoCardItemFactory {

    /**
     * The number of pixels of padding around the outside of the card
     */
    private static final int CARD_PADDING = 5;
    /**
     * The number of pixels of padding between each item
     */
    private static final int ITEM_PADDING = 2;
    /**
     * The size in pixels of each item
     */
    private static final int ITEM_SIZE = 22;
    /**
     * The size in pixels of each image within the items
     */
    private static final int IMAGE_SIZE = 16;
    /**
     * The number of pixels of padding between the images and the item border
     */
    private static final int IMAGE_PADDING = 3;

    /**
     * The game instance
     */
    private final Game game;

    /**
     * A map containing cached buffered images
     */
    private final Map<Material, BufferedImage> cachedImages;

    public BingoCardItemFactory(Game game) {
        this.game = game;

        this.cachedImages = new HashMap<>();
    }

    /**
     * Create an ItemStack from the given bingo card
     * @param bingoCard The BingoCard to make the itemstack from
     * @return The created ItemStack
     */
    public ItemStack create(BingoCard bingoCard) {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        // Base layer of map color
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                image.setRGB(
                        x,
                        y,
                        ColorUtil.getFromRgb(214, 190, 150)
                );
            }
        }

        BingoItem[][] bingoItems = bingoCard.getBingoItems();
        for (int y = 0; y < bingoItems.length; y++) {
            for (int x = 0; x < bingoItems[y].length; x++) {
                BingoItem bingoItem = bingoItems[y][x];

                int baseColor;
                if (bingoItem.isCollected()) {
                    // Green color
                    baseColor = ColorUtil.getFromRgb(0, 220, 0);
                } else {
                    // Gray color
                    baseColor = ColorUtil.getFromRgb(120, 120, 120);
                }

                // Write background
                for (int imageX = 0; imageX < ITEM_SIZE; imageX++) {
                    for (int imageY = 0; imageY < ITEM_SIZE; imageY++) {
                        image.setRGB(
                                CARD_PADDING + x * (ITEM_PADDING + ITEM_SIZE) + imageX,
                                CARD_PADDING + y * (ITEM_PADDING + ITEM_SIZE) + imageY,
                                baseColor
                        );
                    }
                }

                // Write image
                BufferedImage itemImage;

                if (!cachedImages.containsKey(bingoItem.getMaterial())) {
                    itemImage = FileUtil.readItemImage(
                            game.getDataFolder(),
                            bingoItem.getMaterial()
                    );
                    cachedImages.put(bingoItem.getMaterial(), itemImage);
                } else {
                    itemImage = cachedImages.get(bingoItem.getMaterial());
                }

                if (itemImage == null) {
                    Game.getLogger().warning(
                            "Could not find image file for material: " + bingoItem.getMaterial()
                    );
                    continue;
                }

                for (int imageX = 0; imageX < IMAGE_SIZE; imageX++) {
                    for (int imageY = 0; imageY < IMAGE_SIZE; imageY++) {
                        int colorToSet = itemImage.getRGB(imageX, imageY);
                        int alpha = (colorToSet >>> 24);

                        if (alpha == 0) {
                            colorToSet = baseColor;
                        }

                        image.setRGB(
                                CARD_PADDING + x * (ITEM_PADDING + ITEM_SIZE) + IMAGE_PADDING + imageX,
                                CARD_PADDING + y * (ITEM_PADDING + ITEM_SIZE) + IMAGE_PADDING + imageY,
                                colorToSet
                        );
                    }
                }
            }
        }

        ItemStack itemStack = new ItemStack(Material.FILLED_MAP, 1);

        MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));

        // We get a copy of the list from mapView.getRenderers()
        // So loop over it and individually delete all renderers
        for (MapRenderer mapRenderer : mapView.getRenderers()) {
            mapView.removeRenderer(mapRenderer);
        }
        mapView.addRenderer(new ImageRenderer(image));

        MapMeta meta = (MapMeta) itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Bingo Card");
            meta.setMapView(mapView);

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

}
