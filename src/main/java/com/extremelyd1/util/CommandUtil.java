package com.extremelyd1.util;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.team.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandUtil {
    /**
     * Checks whether the command sender has permission to execute a command.
     * @param sender The sender to check.
     * @return True if the sender is a player and has OP rights; False otherwise.
     */
    public static boolean checkCommandSender(CommandSender sender) {
        return checkCommandSender(sender, false, true);
    }

    /**
     * Checks whether the command sender has permission to execute a command.
     * @param sender The sender to check.
     * @param allowConsole Whether to allow console to execute.
     * @param needsOp Whether the player needs to be OP to execute.
     * @return True if the sender is allowed to execute the given command.
     */
    public static boolean checkCommandSender(CommandSender sender, boolean allowConsole, boolean needsOp) {
        if (!(sender instanceof Player player)) {
            if (!allowConsole) {
                sender.sendMessage("This command can only be executed as a player");

                return false;
            } else {
                return true;
            }
        }

        if (!needsOp) {
            return true;
        }

        // TODO: implement better permissions system
        if (!player.isOp()) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("No permission to execute command")
                    .color(NamedTextColor.WHITE))
            );

            return false;
        }

        return true;
    }

    /**
     * Checks whether the command sender has permission to execute a command.
     * @param sender The sender to check.
     * @param allowConsole Whether to allow console to execute.
     * @param needsOp Whether the player needs to be OP to execute.
     * @param requiresInGame Whether the game state needs to be in-game.
     * @param disallowSpectator Whether to disallow spectators from executing this command.
     * @return True if the sender is allowed to execute the given command.
     */
    public static boolean checkCommandSender(
            CommandSender sender,
            Game game,
            boolean allowConsole,
            boolean needsOp,
            boolean requiresInGame,
            boolean disallowSpectator
    ) {
        if (!checkCommandSender(sender, allowConsole, needsOp)) {
            return true;
        }

        Player player = (Player) sender;

        if (requiresInGame && !game.getState().equals(Game.State.IN_GAME)) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command now")
                    .color(NamedTextColor.WHITE))
            );

            return true;
        }

        if (disallowSpectator) {
            Team team = game.getTeamManager().getTeamByPlayer(player);
            if (team == null || team.isSpectatorTeam()) {
                player.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Cannot execute this command as spectator")
                        .color(NamedTextColor.WHITE))
                );

                return true;
            }
        }

        return false;
    }

    /**
     * Get the list of strings for tab completion of a command that lists the possible numbers of teams.
     * @param startsWith The string the candidates should start with.
     * @return A list of strings for tab completion.
     */
    public static List<String> GetTabCompletionForNumTeams(String startsWith) {
        List<String> numCompletions = new ArrayList<>();
        for (int i = 1; i <= TeamManager.MAX_TEAMS; i++) {
            String s = String.valueOf(i);
            if (s.startsWith(startsWith)) {
                numCompletions.add(s);
            }
        }

        return numCompletions;
    }
}
