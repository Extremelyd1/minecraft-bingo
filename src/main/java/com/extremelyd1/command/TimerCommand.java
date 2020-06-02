package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TimerCommand implements CommandExecutor {

    private final Game game;

    public TimerCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!CommandUtil.checkCommandSender(sender)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Please provide either 'enable/disable' or a max time length in seconds"
            );

            return true;
        }

        if (!game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(
                    ChatColor.DARK_RED
                            + "Error: "
                            + ChatColor.WHITE
                            + "Cannot execute this command now"
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            game.getConfig().setTimerEnabled(true);

            Bukkit.broadcastMessage(
                    Game.PREFIX + "Game timer is now " + ChatColor.GREEN + "enabled"
            );

            return true;
        } else if (args[0].equalsIgnoreCase("disable")) {
            game.getConfig().setTimerEnabled(false);

            Bukkit.broadcastMessage(
                    Game.PREFIX + "Game timer is now " + ChatColor.DARK_RED + "disabled"
            );

            return true;
        }

        int timerLength;
        try {
            timerLength = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Could not parse arguments, please provide max timer length in seconds"
            );

            return true;
        }

        if (timerLength < 1) {
            sender.sendMessage(
                    ChatColor.DARK_RED + "Error: "
                            + ChatColor.WHITE + "Timer length must be "
                            + ChatColor.BOLD + "at least"
                            + ChatColor.RESET + " 1"
            );

            return true;
        }

        game.getConfig().setTimerLength(timerLength);

        Bukkit.broadcastMessage(
                Game.PREFIX + "Max timer length has been set to "
                        + ChatColor.YELLOW + timerLength
                        + ChatColor.WHITE + " seconds"
        );

        return true;
    }
}
