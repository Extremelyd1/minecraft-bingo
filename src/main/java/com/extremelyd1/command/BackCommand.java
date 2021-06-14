package com.extremelyd1.command;

import com.extremelyd1.game.Game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {

    private Game game;

    public BackCommand(Game game){
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command command, String s, String[] args) {
        if (!(cs instanceof Player)) return true;

        Player p = (Player)cs;
        if (args.length == 0) {
            if (game.getBackloc().containsKey(p)) {
                p.sendMessage(ChatColor.RED + "Teleport will commence in 5 seconds...");
                Location loc = game.getBackloc().get(p);
                Bukkit.getScheduler().runTaskLater(game.getPlugin(), ()-> p.teleport(loc), 100L);
                game.getBackloc().remove(p);
            }
            return true;
        }
        return false;
    }
}
