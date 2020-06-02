package com.extremelyd1.game.team;

import com.extremelyd1.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class TeamManager {

    private final Game game;

    public static final int MAX_TEAMS = 8;

    private List<Team> teams;

    private TeamFactory teamFactory;

    public TeamManager(Game game) {
        this.game = game;
        this.teams = new ArrayList<>();
    }

    public void createRandomizedTeams(
            Collection<? extends Player> players,
            int numTeams,
            boolean notify
    ) {
        game.getLogger().info("Clearing teams and creating randomized teams...");

        if (numTeams == MAX_TEAMS) {
            throw new IllegalArgumentException("Can not create more than " + MAX_TEAMS + " teams");
        }

        this.teams.clear();
        this.teamFactory = new TeamFactory();

        int playersPerTeam = players.size() / numTeams;

        List<Player> playersLeft = new ArrayList<>(players);

        for (int i = 0; i < numTeams; i++) {
            Team team = teamFactory.createTeam();

            for (int p = 0; p < playersPerTeam; p++) {
                Player randomPlayer = playersLeft.get(
                        new Random().nextInt(playersLeft.size())
                );

                team.addPlayer(randomPlayer);
                if (notify) {
                    randomPlayer.sendMessage(
                            Game.PREFIX +
                            "Joined "
                                    + team.getColor() + team.getName()
                                    + ChatColor.WHITE + " team"
                    );
                }

                playersLeft.remove(randomPlayer);
            }

            teams.add(team);
        }

        // Loop over the leftover players until they have
        // all randomly been assigned to a team
        List<Team> teamsLeft = new ArrayList<>(teams);
        for (Player player : playersLeft) {
            Team team = teamsLeft.get(
                    new Random().nextInt(teamsLeft.size())
            );

            team.addPlayer(player);
            if (notify) {
                player.sendMessage(
                        Game.PREFIX +
                        "Joined "
                                + team.getColor() + team.getName()
                                + ChatColor.WHITE + " team"
                );
            }

            teamsLeft.remove(team);
        }
    }

    public Team getTeamByPlayer(Player player) {
        for (Team team : teams) {
            if (team.getPlayers().contains(player)) {
                return team;
            }
        }

        return null;
    }

    public Team createTeam() {
        if (teamFactory == null) {
            teamFactory = new TeamFactory();
        }

        Team team = teamFactory.createTeam();

        this.teams.add(team);

        return team;
    }

    public List<Team> getTeams() {
        return teams;
    }

}
