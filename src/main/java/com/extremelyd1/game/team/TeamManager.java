package com.extremelyd1.game.team;

import com.extremelyd1.game.Game;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Manager class that handles team related activities
 */
public class TeamManager {

    /**
     * The maximum number of teams
     */
    public static final int MAX_TEAMS = 8;

    /**
     * The list of possible teams
     */
    private final List<PlayerTeam> teams;
    /**
     * The list of teams that have at least a single player on it
     */
    private final List<PlayerTeam> activeTeams;
    /**
     * The team containing spectators
     */
    private final Team spectatorTeam;

    public TeamManager() {
        this.teams = new ArrayList<>();
        this.activeTeams = new ArrayList<>();

        this.spectatorTeam = new Team();

        TeamFactory teamFactory = new TeamFactory();
        for (int i = 0; i < MAX_TEAMS; i++) {
            teams.add(teamFactory.createTeam());
        }
    }

    /**
     * Create randomized teams for the given collection of players
     * @param players The collection of players to divide over teams
     * @param numTeams The number of teams to create
     * @param notify Whether to notify the players of their new team
     */
    public void createRandomizedTeams(
            Collection<? extends Player> players,
            int numTeams,
            boolean notify
    ) {
        Game.getLogger().info("Clearing teams and creating randomized teams...");

        if (numTeams > MAX_TEAMS) {
            throw new IllegalArgumentException("Can not create more than " + MAX_TEAMS + " teams");
        }

        clearTeams();

        int playersPerTeam = players.size() / numTeams;

        List<Player> playersLeft = new ArrayList<>(players);

        int teamIndex = 0;

        for (int i = 0; i < numTeams; i++) {
            PlayerTeam team = teams.get(teamIndex++);

            for (int p = 0; p < playersPerTeam; p++) {
                Player randomPlayer = playersLeft.get(
                        new Random().nextInt(playersLeft.size())
                );

                addPlayerToTeam(randomPlayer, team, notify);

                playersLeft.remove(randomPlayer);
            }

            if (!activeTeams.contains(team)) {
                activeTeams.add(team);
            }
        }

        // Loop over the leftover players until they have
        // all randomly been assigned to a team
        List<PlayerTeam> teamsLeft = new ArrayList<>(activeTeams);
        for (Player player : playersLeft) {
            PlayerTeam team = teamsLeft.get(
                    new Random().nextInt(teamsLeft.size())
            );

            addPlayerToTeam(player, team, notify);

            teamsLeft.remove(team);
        }
    }

    /**
     * Add a given player to a given team
     * @param player The player to add to the team
     * @param team The team to add the player to
     */
    public void addPlayerToTeam(Player player, Team team) {
        this.addPlayerToTeam(player, team, true);
    }

    /**
     * Add a given player to a given team
     * @param player The player to add to the team
     * @param team The team to add the player to
     * @param notify Whether to notify the player of their new team
     */
    public void addPlayerToTeam(Player player, Team team, boolean notify) {
        removePlayerFromTeam(player);
        team.addPlayer(player, notify);

        if (!team.isSpectatorTeam()) {
            PlayerTeam playerTeam = (PlayerTeam) team;

            if (!activeTeams.contains(playerTeam)) {
                activeTeams.add(playerTeam);
            }
        }
    }

    /**
     * Remove a given player from a team if they are on a team
     *
     * @param player The player to remove from the team
     */
    private void removePlayerFromTeam(Player player) {
        Team team = getTeamByPlayer(player);

        if (team != null) {
            removePlayerFromTeam(player, team);

        }
    }

    /**
     * Remove a given player from a given team
     * @param player The player to remove from the team
     * @param team The team to remove the player from
     */
    private void removePlayerFromTeam(Player player, Team team) {
        team.removePlayer(player);

        if (!team.isSpectatorTeam() && team.getNumPlayers() == 0) {
            //noinspection SuspiciousMethodCalls
            activeTeams.remove(team);
        }
    }

    /**
     * Get a team by their name, ignoring case
     * @param name The name to search for
     * @return The team that matches the name or null if none match
     */
    public PlayerTeam getTeamByName(String name) {
        for (PlayerTeam team : teams) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }

        return null;
    }

    /**
     * Get the spectator team
     * @return The spectator team
     */
    public Team getSpectatorTeam() {
        return spectatorTeam;
    }

    /**
     * Removes all players from all active teams
     */
    public void clearTeams() {
        while (!activeTeams.isEmpty()) {
            PlayerTeam team = activeTeams.getFirst();

            team.clear();

            activeTeams.remove(team);
        }
    }

    /**
     * Get the team a given player is on
     * @param player The player to check
     * @return The team the player is on or null if the player has no team
     */
    public Team getTeamByPlayer(Player player) {
        for (PlayerTeam team : activeTeams) {
            if (team.contains(player)) {
                return team;
            }
        }

        if (spectatorTeam.contains(player)) {
            return spectatorTeam;
        }

        return null;
    }

    /**
     * Get the number of teams with players on them
     * @return The number of teams with players on them
     */
    public int getNumActiveTeams() {
        return activeTeams.size();
    }

    /**
     * Get an iterable for the teams with players on them that can be looped over
     * @return An iterable for the teams with players on them that can be looped over
     */
    public Iterable<PlayerTeam> getActiveTeams() {
        return activeTeams;
    }

    /**
     * Get an iterable for all the available teams that can be looped over
     * @return An iterable for the available teams
     */
    public Iterable<PlayerTeam> getAvailableTeams() {
        return teams;
    }

}
