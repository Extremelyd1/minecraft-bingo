package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WinConditionCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public WinConditionCommand(Game game) {
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
                            + ChatColor.WHITE + "Please provide either 'full' or a number of lines"
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("full")) {
            boolean fullCard = !game.getWinConditionChecker().isFullCard();
            game.getWinConditionChecker().setFullCard(fullCard);

            if (fullCard) {
                Bukkit.broadcastMessage(
                        Game.PREFIX + "Full bingo card is now " + ChatColor.GREEN + "enabled"
                );
            } else {
                Bukkit.broadcastMessage(
                        Game.PREFIX + "Full bingo card is now " + ChatColor.DARK_RED + "disabled"
                );
            }
        } else {
            int numLines;
            try {
                numLines = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "Could not parse arguments, please provide an integer"
                );

                return true;
            }

            if (numLines < 1 || numLines > 10) {
                sender.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "Number of lines to complete must be "
                                + ChatColor.BOLD + "between"
                                + ChatColor.RESET + " 0 and 11"
                );

                return true;
            }

            game.getWinConditionChecker().setNumLinesComplete(numLines);

            Bukkit.broadcastMessage(
                    Game.PREFIX + "Number of lines (rows, columns or diagonals) to achieve bingo has been set to "
                            + ChatColor.YELLOW + numLines
            );
        }

        return true;
    }
}
