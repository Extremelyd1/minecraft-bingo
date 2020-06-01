package com.extremelyd1.bingo.map;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.BingoItem;
import com.extremelyd1.game.Game;
import com.extremelyd1.util.ColorUtil;
import com.extremelyd1.util.ImageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class BingoCardItemFactory {

    private static final int CARD_PADDING = 5;
    private static final int ITEM_PADDING = 2;
    private static final int ITEM_SIZE = 22;
    private static final int IMAGE_SIZE = 16;
    private static final int IMAGE_PADDING = 3;

    private final Game game;
    private final World world;

    private final Map<Material, BufferedImage> cachedImages;

    public BingoCardItemFactory(Game game, World world) {
        this.game = game;
        this.world = world;

        this.cachedImages = new HashMap<>();
    }

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
                    itemImage = ImageUtil.readItemImage(
                            game.getDataFolder(),
                            bingoItem.getMaterial()
                    );
                    cachedImages.put(bingoItem.getMaterial(), itemImage);
                } else {
                    itemImage = cachedImages.get(bingoItem.getMaterial());
                }

                if (itemImage == null) {
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

//        try {
//            ImageIO.write(image, "png", new File("D:\\Users\\X\\Desktop\\test.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        ItemStack itemStack = new ItemStack(Material.FILLED_MAP, 1);

        MapView mapView = Bukkit.createMap(world);
        mapView.getRenderers().clear();
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
