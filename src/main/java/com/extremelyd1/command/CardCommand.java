package com.extremelyd1.command;

import com.extremelyd1.bingo.BingoItem;
import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ItemUtil;
import com.extremelyd1.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CardCommand implements CommandExecutor {

    private final Game game;

    public CardCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute this command as console");
            return true;
        }

        Player player = (Player) sender;

        if (!game.getState().equals(Game.State.IN_GAME)) {
            player.sendMessage(
                    ChatColor.DARK_RED + "Error: " + ChatColor.WHITE + "Can not execute this command now"
            );

            return true;
        }

        Team team = null;
        for (Team possibleTeam : game.getTeamManager().getTeams()) {
            if (possibleTeam.getPlayers().contains(player)) {
                team = possibleTeam;
                break;
            }
        }

        if (team == null) {
            return true;
        }

        if (ItemUtil.hasBingoCard(player)) {
            player.sendMessage(Game.PREFIX + "You already have a bingo card in your inventory");

            return true;
        }

        player.getInventory().addItem(
                game.getBingoCardItemFactory().create(team.getBingoCard())
        );
        player.sendMessage(
                Game.PREFIX + "You have been given a new bingo card"
        );

        return true;
    }
}
