package com.extremelyd1.command;

import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.CommandUtil;
import com.extremelyd1.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BingoCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public BingoCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, false, false)) {
            return true;
        }

        Player player = (Player) sender;

        if (!game.getState().equals(Game.State.IN_GAME)) {
            player.sendMessage(
                    ChatColor.DARK_RED + "Error: " + ChatColor.WHITE + "Can not execute this command now"
            );

            return true;
        }

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null || team.isSpectatorTeam()) {
            player.sendMessage(
                    ChatColor.DARK_RED + "Error: " + ChatColor.WHITE + "Can not execute this command as spectator"
            );

            return true;
        }

        PlayerTeam playerTeam = (PlayerTeam) team;

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

        String itemsLeftString;

        if (itemsLeft.size() == 0) {
            itemsLeftString = ChatColor.GRAY + "No items";
        } else {
            itemsLeftString = ChatColor.AQUA + itemsLeft.stream()
                    .map(StringUtil::formatMaterialName)
                    .collect(Collectors.joining(ChatColor.WHITE + ", " + ChatColor.AQUA));
        }

        String itemsCollectedString;

        if (itemsCollected.size() == 0) {
            itemsCollectedString = ChatColor.GRAY + "No items";
        } else {
            itemsCollectedString = ChatColor.AQUA + itemsCollected.stream()
                    .map(StringUtil::formatMaterialName)
                    .collect(Collectors.joining(ChatColor.WHITE + ", " + ChatColor.AQUA));
        }

        String response = Game.PREFIX
                + "Items left on the board:\n"
                + itemsLeftString
                + ChatColor.WHITE + "\nItems already collected:\n"
                + itemsCollectedString;

        player.sendMessage(response);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
