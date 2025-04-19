package com.extremelyd1.command;

import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.util.CommandUtil;
import com.extremelyd1.util.ChatUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class BingoCommand implements BasicCommand {

    /**
     * The game instance
     */
    private final Game game;

    public BingoCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (CommandUtil.checkCommandSender(commandSourceStack, game, false, false, true, true)) {
            return;
        }

        Player player = (Player) commandSourceStack.getSender();
        PlayerTeam playerTeam = (PlayerTeam) game.getTeamManager().getTeamByPlayer(player);

        List<Material> itemsCollected = new ArrayList<>();
        List<Material> itemsLeft = new ArrayList<>();

        for (BingoItem[] bingoItemRow : game.getBingoCard().getBingoItems()) {
            for (BingoItem bingoItem : bingoItemRow) {
                if (bingoItem.hasCollected(playerTeam)) {
                    itemsCollected.add(bingoItem.getMaterial());
                } else {
                    itemsLeft.add(bingoItem.getMaterial());
                }
            }
        }

        Component message = Component
                .text("Items left on the board:")
                .appendNewline();

        if (itemsLeft.isEmpty()) {
            message = message.append(Component
                    .text("No items")
                    .color(NamedTextColor.GRAY)
            );
        } else {
            appendFormattedMaterialList(message, itemsLeft);
        }

        message = message.appendNewline().append(Component
                .text("Items already collected:")
                .color(NamedTextColor.WHITE)
                .appendNewline()
        );

        if (itemsCollected.isEmpty()) {
            message = message.append(Component
                    .text("No items")
                    .color(NamedTextColor.GRAY)
            );
        } else {
            appendFormattedMaterialList(message, itemsCollected);
        }

        player.sendMessage(message);
    }

    /**
     * Append the given list of materials as formatted strings to the given component.
     * @param baseComponent The component to append strings to.
     * @param list The list of materials to format.
     */
    private static void appendFormattedMaterialList(Component baseComponent, List<Material> list) {
        for (int i = 0; i < list.size(); i++) {
            baseComponent = baseComponent.append(Component
                    .text(ChatUtil.formatMaterialName(list.get(i)))
                    .color(NamedTextColor.AQUA)
            );

            if (i != list.size() - 1) {
                baseComponent = baseComponent.append(Component
                        .text(", ")
                        .color(NamedTextColor.WHITE)
                );
            }
        }
    }
}
