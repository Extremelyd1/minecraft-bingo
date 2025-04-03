package com.extremelyd1.command;

import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import com.extremelyd1.util.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CardCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    /**
     * The bingo card item factory instance to check whether a given item stack is a bingo card.
     */
    private final BingoCardItemFactory bingoCardItemFactory;

    public CardCommand(Game game, BingoCardItemFactory bingoCardItemFactory) {
        this.game = game;
        this.bingoCardItemFactory = bingoCardItemFactory;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String s,
            String @NotNull [] strings
    ) {
        if (!CommandUtil.checkCommandSender(sender, game, false, false, true, true)) {
            return true;
        }

        Player player = (Player) sender;
        Team team = game.getTeamManager().getTeamByPlayer(player);

        if (ItemUtil.hasBingoCard(player, bingoCardItemFactory)) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("You already have a bingo card in your inventory")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        player.getInventory().addItem(
                game.getBingoCardItemFactory().create(game.getBingoCard(), (PlayerTeam) team)
        );

        player.sendMessage(ChatUtil.successPrefix().append(Component
                .text("You have been given a new bingo card")
                .color(NamedTextColor.WHITE)
        ));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        return Collections.emptyList();
    }
}
