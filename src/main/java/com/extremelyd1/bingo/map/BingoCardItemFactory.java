package com.extremelyd1.bingo.map;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
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
     * The integer color of the default map background
     */
    private static final int MAP_BACKGROUND_COLOR = ColorUtil.getFromRgb(214, 190, 150);
    /**
     * The background color of an item if it has not been collected yet
     */
    private static final int NOT_COLLECTED_COLOR = ColorUtil.getFromRgb(120, 120, 120);
    /**
     * The background color of an item if it has been collected
     */
    private static final int COLLECTED_COLOR = ColorUtil.getFromRgb(0, 220, 0);
    /**
     * The background color of an item if it has been locked
     */
    private static final int LOCKED_COLOR = ColorUtil.getFromRgb(180, 0, 0);

    /**
     * The size of the drawable map canvas
     */
    private static final int CANVAS_SIZE = 128;
    /**
     * The number of pixels of padding around the outside of the card
     */
    private static final int CARD_PADDING = 5;
    /**
     * The number of pixels of border around the background of the card
     */
    private static final int BACKGROUND_BORDER_SIZE = 3;
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
     * The size of the squares that show the collections for other teams, otherwise known as indicators
     */
    private static final int TEAM_INDICATOR_SIZE = 2;
    /**
     * The padding between the squares of the team indicators
     */
    private static final int TEAM_INDICATOR_PADDING = 1;

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
     *
     * @param bingoCard      The BingoCard to make the itemstack from
     * @param team           The team that this card should be created for
     * @return The created ItemStack
     */
    public ItemStack create(BingoCard bingoCard, PlayerTeam team) {
        return create(bingoCard, team, MAP_BACKGROUND_COLOR);
    }

    /**
     * Create an ItemStack from the given bingo card
     *
     * @param bingoCard      The BingoCard to make the itemstack from
     * @param team           The team that this card should be created for
     * @param borderColor    The color of the border of the bingo card
     * @return The created ItemStack
     */
    public ItemStack create(BingoCard bingoCard, PlayerTeam team, int borderColor) {
        BufferedImage image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB);
        // Base layer of map color
        for (int x = 0; x < CANVAS_SIZE; x++) {
            for (int y = 0; y < CANVAS_SIZE; y++) {
                // Test whether we are drawing the border
                if (x < BACKGROUND_BORDER_SIZE
                        || x >= CANVAS_SIZE - BACKGROUND_BORDER_SIZE
                        || y < BACKGROUND_BORDER_SIZE
                        || y >= CANVAS_SIZE - BACKGROUND_BORDER_SIZE
                ) {
                    // Draw the given border color
                    image.setRGB(
                            x,
                            y,
                            borderColor
                    );
                } else {
                    // Otherwise, draw the default map color
                    image.setRGB(
                            x,
                            y,
                            MAP_BACKGROUND_COLOR
                    );
                }
            }
        }

        BingoItem[][] bingoItems = bingoCard.getBingoItems();
        for (int y = 0; y < bingoItems.length; y++) {
            for (int x = 0; x < bingoItems[y].length; x++) {
                BingoItem bingoItem = bingoItems[y][x];

                int baseColor;
                if (bingoItem.hasCollected(team)) {
                    baseColor = COLLECTED_COLOR;
                } else {
                    if (bingoCard.isItemLocked(bingoItem)) {
                        baseColor = LOCKED_COLOR;
                    } else {
                        baseColor = NOT_COLLECTED_COLOR;
                    }
                }

                int backgroundStartX = CARD_PADDING + x * (ITEM_PADDING + ITEM_SIZE);
                int backgroundStartY = CARD_PADDING + y * (ITEM_PADDING + ITEM_SIZE);

                // Write background
                for (int imageX = 0; imageX < ITEM_SIZE; imageX++) {
                    for (int imageY = 0; imageY < ITEM_SIZE; imageY++) {
                        image.setRGB(
                                backgroundStartX + imageX,
                                backgroundStartY + imageY,
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
                                backgroundStartX + IMAGE_PADDING + imageX,
                                backgroundStartY + IMAGE_PADDING + imageY,
                                colorToSet
                        );
                    }
                }

                // Check whether we need to draw the team indicators for other teams.
                // Either if the setting is set to false, or when it is lockout with 1 completion to lock
                // and there are only 2 teams. Then it is trivial which team completed it when it locks.
                if (!game.getConfig().notifyOtherTeamCompletions()
                        || (game.getWinConditionChecker().getCompletionsToLock() == 1
                        && game.getTeamManager().getNumActiveTeams() == 2)) {
                    continue;
                }

                // Calculate the start positions of the indicators
                int indicatorStartX = backgroundStartX + TEAM_INDICATOR_PADDING;
                int indicatorStartY = backgroundStartY + TEAM_INDICATOR_PADDING;

                // Loop over the teams that have collected this item
                for (PlayerTeam collector : bingoItem.getCollectors()) {
                    // Skip the team that this card is created for
                    if (team.equals(collector)) {
                        continue;
                    }

                    // Retrieve the color of the pixels we need to set
                    int colorToSet = ColorUtil.chatColorToInt(collector.getColor());

                    for (int indicatorX = indicatorStartX; indicatorX < indicatorStartX + TEAM_INDICATOR_SIZE; indicatorX++) {
                        for (int indicatorY = indicatorStartY; indicatorY < indicatorStartY + TEAM_INDICATOR_SIZE; indicatorY++) {
                            image.setRGB(
                                    indicatorX,
                                    indicatorY,
                                    colorToSet
                            );
                        }
                    }

                    // Advance the start X position of the indicator by the size and padding
                    indicatorStartX += TEAM_INDICATOR_SIZE + TEAM_INDICATOR_PADDING;
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
