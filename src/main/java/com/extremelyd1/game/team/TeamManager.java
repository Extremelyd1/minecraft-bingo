package com.extremelyd1.game.team;

import com.extremelyd1.game.Game;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manager class that handles team related activities
 */
public class TeamManager {

    /**
     * The maximum number of teams
     */
    public static final int MAX_TEAMS = 8;

    /**
     * The game instance
     */
    private final Game game;

    /**
     * The list of possible teams
     */
    private final List<Team> teams;
    /**
     * The list of teams that have at least a single player on it
     */
    private final List<Team> activeTeams;

    public TeamManager(Game game) {
        this.game = game;
        this.teams = new ArrayList<>();
        this.activeTeams = new ArrayList<>();

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
            Team team = teams.get(teamIndex++);

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
        List<Team> teamsLeft = new ArrayList<>(activeTeams);
        for (Player player : playersLeft) {
            Team team = teamsLeft.get(
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
        team.addPlayer(player, notify);

        if (!activeTeams.contains(team)) {
            activeTeams.add(team);
        }
    }

    /**
     * Remove a given player from a team if they are on a team
     * @param player The player to remove from the team
     * @return Whether the player was remove from a team
     */
    public boolean removePlayerFromTeam(Player player) {
        Team team = getTeamByPlayer(player);

        if (team != null) {
            removePlayerFromTeam(player, team);

            return true;
        }

        return false;
    }

    /**
     * Remove a given player from a given team
     * @param player The player to remove from the team
     * @param team The team to remove the player from
     */
    public void removePlayerFromTeam(Player player, Team team) {
        team.removePlayer(player);

        if (team.getNumPlayers() == 0) {
            activeTeams.remove(team);
        }
    }

    /**
     * Get a team by their name, ignoring case
     * @param name The name to search for
     * @return The team that matches the name or null if none match
     */
    public Team getTeamByName(String name) {
        for (Team team : teams) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }

        return null;
    }

    /**
     * Removes all players from all active teams
     */
    public void clearTeams() {
        while (!activeTeams.isEmpty()) {
            Team team = activeTeams.get(0);

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
        for (Team team : activeTeams) {
            if (team.contains(player)) {
                return team;
            }
        }

        return null;
    }

    /**
     * Get the number of teams with players on them
     * @return The number of teams with players on them
     */
    public int getNumTeams() {
        return activeTeams.size();
    }

    /**
     * Get an iterable for the teams with players on them that can be looped over
     * @return An iterable for the teams with players on them that can be looped over
     */
    public Iterable<Team> getTeams() {
        return activeTeams;
    }

}
