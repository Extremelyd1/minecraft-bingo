package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.team.TeamManager;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    private final Game game;

    public TeamCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtil.checkCommandSender(sender)) {
            return true;
        }

        Player player = (Player) sender;

        if (args.length <= 0) {
            player.sendMessage(
                    ChatColor.DARK_RED
                            + "Usage: "
                            + ChatColor.WHITE
                            + "/"
                            + command.getName()
                            + " <random|create|add>"
            );

            return true;
        }

        if (!game.getState().equals(Game.State.PRE_GAME)) {
            player.sendMessage(
                    ChatColor.DARK_RED
                            + "Error: "
                            + ChatColor.WHITE
                            + "Cannot execute this command now"
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("random")) {
            if (args.length < 2) {
                player.sendMessage(
                        ChatColor.DARK_RED
                                + "Usage: "
                                + ChatColor.WHITE
                                + "/"
                                + command.getName()
                                + " random <num teams>"
                );

                return true;
            }

            int numTeams = 0;
            try {
                numTeams = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "Could not parse team size argument"
                );

                return true;
            }

            if (numTeams >= TeamManager.MAX_TEAMS) {
                player.sendMessage(
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
        } else if (args[0].equalsIgnoreCase("create")) {
            if (game.getTeamManager().getTeams().size() == TeamManager.MAX_TEAMS) {
                player.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "Cannot create more than 8 teams"
                );

                return true;
            }

            Team createdTeam = game.getTeamManager().createTeam();

            player.sendMessage(
                    Game.PREFIX +
                    ChatColor.GREEN + "Created "
                            + ChatColor.WHITE + "new team: "
                            + createdTeam.getColor() + createdTeam.getName()
                            + ChatColor.WHITE + " team"
            );
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                player.sendMessage(
                        ChatColor.DARK_RED
                                + "Usage: "
                                + ChatColor.WHITE
                                + "/"
                                + command.getName()
                                + " add <player> <team-name>"
                );

                return true;
            }

            Player argumentPlayer = null;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getName().equalsIgnoreCase(args[1])) {
                    argumentPlayer = onlinePlayer;
                    break;
                }
            }

            if (argumentPlayer == null) {
                player.sendMessage(
                        ChatColor.DARK_RED
                                + "Error: "
                                + ChatColor.WHITE
                                + "Could not find player with that name"
                );

                return true;
            }

            Team argumentTeam = null;
            for (Team team : game.getTeamManager().getTeams()) {
                if (team.getName().equalsIgnoreCase(args[2])) {
                    argumentTeam = team;
                    break;
                }
            }

            if (argumentTeam == null) {
                player.sendMessage(
                        ChatColor.DARK_RED
                                + "Error: "
                                + ChatColor.WHITE
                                + "Could not find team with that name"
                );

                return true;
            }

            Team team = game.getTeamManager().getTeamByPlayer(argumentPlayer);
            // Remove player from team if he is already on a team
            if (team.getPlayers().contains(argumentPlayer)) {
                team.getPlayers().remove(argumentPlayer);

                game.getLogger().info("Player was already on a team, removing...");
            }

            argumentTeam.addPlayer(argumentPlayer);
            argumentPlayer.sendMessage(
                    Game.PREFIX +
                    ChatColor.GREEN + "Joined "
                            + argumentTeam.getColor() + argumentTeam.getName()
                            + ChatColor.WHITE + " team"
            );
        }

        game.onPregameUpdate();

        return true;
    }
}
