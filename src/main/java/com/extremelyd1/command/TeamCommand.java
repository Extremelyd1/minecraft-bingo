package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.team.TeamManager;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public TeamCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtil.checkCommandSender(sender, false)) {
            return true;
        }

        if (args.length <= 0) {
            sendUsage(sender, command);

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

        if (args[0].equalsIgnoreCase("random")) {
            if (args.length < 2) {
                sender.sendMessage(
                        ChatColor.DARK_RED
                                + "Usage: "
                                + ChatColor.WHITE
                                + "/"
                                + command.getName()
                                + " random <num teams>"
                );

                return true;
            }

            int numTeams;
            try {
                numTeams = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "Could not parse team size argument"
                );

                return true;
            }

            if (numTeams >= TeamManager.MAX_TEAMS) {
                sender.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "Cannot create more than 8 teams"
                );

                return true;
            }

            game.getTeamManager().createRandomizedTeams(
                    Bukkit.getOnlinePlayers(),
                    numTeams,
                    true
            );
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage(
                        ChatColor.DARK_RED
                                + "Usage: "
                                + ChatColor.WHITE
                                + "/"
                                + command.getName()
                                + " add <player> <team-name>"
                );

                return true;
            }

            Player argumentPlayer = getPlayerByName(args[1]);
            if (argumentPlayer == null) {
                sender.sendMessage(
                        ChatColor.DARK_RED
                                + "Error: "
                                + ChatColor.WHITE
                                + "Could not find player with that name"
                );

                return true;
            }

            TeamManager teamManager = game.getTeamManager();

            Team argumentTeam = teamManager.getTeamByName(args[2]);
            if (argumentTeam == null) {
                sender.sendMessage(
                        ChatColor.DARK_RED
                                + "Error: "
                                + ChatColor.WHITE
                                + "Could not find team with that name"
                );

                return true;
            }

            // Remove player from team if he is already on a team
            if (teamManager.removePlayerFromTeam(argumentPlayer)) {
                Game.getLogger().info("Player was already on a team, removing...");
            }

            teamManager.addPlayerToTeam(argumentPlayer, argumentTeam, true);
        } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 2) {
                    sender.sendMessage(
                            ChatColor.DARK_RED
                                    + "Usage: "
                                    + ChatColor.WHITE
                                    + "/"
                                    + command.getName()
                                    + " remove <player>"
                    );

                    return true;
                }

                Player argumentPlayer = getPlayerByName(args[1]);
                if (argumentPlayer == null) {
                    sender.sendMessage(
                            ChatColor.DARK_RED
                                    + "Error: "
                                    + ChatColor.WHITE
                                    + "Could not find player with that name"
                    );

                    return true;
                }

                TeamManager teamManager = game.getTeamManager();

                Team team = teamManager.getTeamByPlayer(argumentPlayer);
                if (team == null) {
                    sender.sendMessage(
                            ChatColor.DARK_RED
                                    + "Error: "
                                    + ChatColor.WHITE
                                    + "Player is not currently on a team"
                    );

                    return true;
                }

                // Remove player from team
                teamManager.removePlayerFromTeam(argumentPlayer);
        } else {
            sendUsage(sender, command);

            return true;
        }

        game.onPregameUpdate();

        return true;
    }

    /**
     * Send the usage of this command to the given sender
     * @param sender The sender to send the command to
     * @param command The command instance
     */
    private void sendUsage(CommandSender sender, Command command) {
        sender.sendMessage(
                ChatColor.DARK_RED
                        + "Usage: "
                        + ChatColor.WHITE
                        + "/"
                        + command.getName()
                        + " <random|add|remove>"
        );
    }

    /**
     * Get a player by its name
     * @param name The name of the player
     * @return The player corresponding to this name, or null if no such player exists
     */
    private Player getPlayerByName(String name) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(name)) {
                return onlinePlayer;
            }
        }

        return null;
    }
}
