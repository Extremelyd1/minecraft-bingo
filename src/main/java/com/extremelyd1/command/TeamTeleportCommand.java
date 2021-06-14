package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.gui.TeleportGui;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class TeamTeleportCommand implements CommandExecutor {
    /**
     * The game instance
     */
    private final Game game;

    public TeamTeleportCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command command, String s, String[] strings) {
        if (!(cs instanceof Player)) {
            cs.sendMessage("Cannot execute this command as console");
            return true;
        }

        Player player = (Player) cs;
        Team team = game.getTeamManager().getTeamByPlayer(player);

        if (!game.getState().equals(Game.State.IN_GAME)) {
            cs.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Can only use this command in game"
            );

            return true;
        }

        if (team == null) {
            player.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.WHITE + "You are not on a team");
        } else {
            List<Player> playerList = new LinkedList<>();
            team.getPlayers().forEach(playerList::add);
            Player[] players = new Player[playerList.size()];
            players = playerList.toArray(players);

            TeleportGui gui = new TeleportGui(game, players);
            gui.openInventory((Player) cs);
        }
        return true;
    }
}
