package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.team.TeamManager;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class TeamCommand implements BasicCommand {

    /**
     * The game instance.
     */
    private final Game game;

    public TeamCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, true, true)) {
            return;
        }

        CommandSender sender = commandSourceStack.getSender();

        if (args.length == 0) {
            sendUsage(sender);

            return;
        }

        if (!game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command now")
                    .color(NamedTextColor.WHITE)
            ));

            return;
        }

        if (args[0].equalsIgnoreCase("random")) {
            if (args.length < 2) {
                sendUsageRandom(sender);

                return;
            }

            int numTeams;
            try {
                numTeams = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Could not parse team size argument")
                        .color(NamedTextColor.WHITE)
                ));

                return;
            }

            if (numTeams <= 0) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Cannot create less than 1 team")
                        .color(NamedTextColor.WHITE)
                ));

                return;
            }

            if (numTeams >= TeamManager.MAX_TEAMS) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Cannot create more than 8 teams")
                        .color(NamedTextColor.WHITE)
                ));

                return;
            }

            Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            if (args.length > 2) {
                if (args[2].equalsIgnoreCase("-e")) {
                    if (args.length < 4) {
                        sendUsageRandom(sender);
                        return;
                    }

                    Collection<Player> excludedPlayers = parsePlayerArguments(args, 3);
                    if (excludedPlayers.isEmpty()) {
                        sender.sendMessage(ChatUtil.errorPrefix().append(Component
                                .text("Could not find the given players")
                                .color(NamedTextColor.WHITE)
                        ));

                        return;
                    }

                    // Filter the online players to exclude the players given in the command
                    players = players.stream()
                            .filter(player -> !excludedPlayers.contains(player))
                            .collect(Collectors.toList());
                } else {
                    players = parsePlayerArguments(args, 2);
                }
            }

            if (players.isEmpty()) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("There are no players to create teams with")
                        .color(NamedTextColor.WHITE)
                ));

                return;
            }

            game.getTeamManager().createRandomizedTeams(
                    players,
                    numTeams,
                    true
            );

            sender.sendMessage(ChatUtil.successPrefix().append(Component
                    .text("Created random teams")
                    .color(NamedTextColor.WHITE)
            ));
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage(Component
                        .text("Usage: ")
                        .color(NamedTextColor.DARK_RED)
                        .append(Component
                                .text("/team add <player> <team name>")
                                .color(NamedTextColor.WHITE)
                        )
                );

                return;
            }

            Player argumentPlayer = getPlayerByName(args[1]);
            if (argumentPlayer == null) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Could not find player with that name")
                        .color(NamedTextColor.WHITE)
                ));

                return;
            }

            TeamManager teamManager = game.getTeamManager();

            PlayerTeam argumentTeam = teamManager.getTeamByName(args[2]);
            if (argumentTeam == null) {
                sender.sendMessage(ChatUtil.errorPrefix().append(Component
                        .text("Could not find team with that name")
                        .color(NamedTextColor.WHITE)
                ));

                return;
            }

            teamManager.addPlayerToTeam(argumentPlayer, argumentTeam, true);
        } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 2) {
                    sender.sendMessage(Component
                            .text("Usage: ")
                            .color(NamedTextColor.DARK_RED)
                            .append(Component
                                    .text("/team remove <player>")
                                    .color(NamedTextColor.WHITE)
                            )
                    );

                    return;
                }

                Player argumentPlayer = getPlayerByName(args[1]);
                if (argumentPlayer == null) {
                    sender.sendMessage(ChatUtil.errorPrefix().append(Component
                            .text("Could not find player with that name")
                            .color(NamedTextColor.WHITE)
                    ));

                    return;
                }

                TeamManager teamManager = game.getTeamManager();

                Team team = teamManager.getTeamByPlayer(argumentPlayer);
                if (team == null || team.isSpectatorTeam()) {
                    sender.sendMessage(ChatUtil.errorPrefix().append(Component
                            .text("Player is not currently on a team")
                            .color(NamedTextColor.WHITE)
                    ));

                    return;
                }

                // Add player to spectators
                teamManager.addPlayerToTeam(argumentPlayer, teamManager.getSpectatorTeam());
        } else {
            sendUsage(sender);
            return;
        }

        game.onPregameUpdate();
    }

    /**
     * Send the usage of this command to the given sender.
     * @param sender The sender to send the command to.
     */
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component
                .text("Usage: ")
                .color(NamedTextColor.DARK_RED)
                .append(Component
                        .text("/team <random|add|remove>")
                        .color(NamedTextColor.WHITE)
                )
        );
    }

    /**
     * Send the usage of the random sub command.
     */
    private void sendUsageRandom(CommandSender sender) {
        sender.sendMessage(Component
                .text("Usage: ")
                .color(NamedTextColor.DARK_RED)
                .append(Component
                        .text("/team random <num teams> [-e] [players...]")
                        .color(NamedTextColor.WHITE)
                )
        );
    }

    /**
     * Get a player by its name.
     * @param name The name of the player.
     * @return The player corresponding to this name, or null if no such player exists.
     */
    private Player getPlayerByName(String name) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(name)) {
                return onlinePlayer;
            }
        }

        return null;
    }

    /**
     * Parse the given argument list to player instances.
     * @param args The argument list.
     * @param index The index to start parsing at.
     * @return A collection of players that match the names given in the argument list.
     */
    private Collection<Player> parsePlayerArguments(String[] args, int index) {
        Collection<Player> players = new ArrayList<>();

        while (index < args.length) {
            Player player = Bukkit.getPlayer(args[index++]);

            if (player == null) {
                continue;
            }

            players.add(player);
        }

        return players;
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!game.getState().equals(Game.State.PRE_GAME)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("random", "add", "remove");
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("random")) {
                return CommandUtil.GetTabCompletionForNumTeams(args[1]);
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                // Get all online players and return their names
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }

            return Collections.emptyList();
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {
                List<String> teams = new ArrayList<>();

                for (PlayerTeam team : game.getTeamManager().getAvailableTeams()) {
                    if (team.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        teams.add(team.getName());
                    }
                }

                return teams;
            }
        }

        return Collections.emptyList();
    }
}
