package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoardManager {

    private final Game game;

    private final PregameBoard pregameBoard;
    private Map<Team, IngameBoard> ingameBoards;

    public GameBoardManager(Game game) {
        this.game = game;

        this.pregameBoard = new PregameBoard(game);
    }

    public void createIngameBoards(Iterable<Team> teams) {
        ingameBoards = new HashMap<>();

        for (Team team : teams) {
            ingameBoards.put(team, new IngameBoard(game, team));
        }
    }

    public void onPregameUpdate() {
        if (game.getState().equals(Game.State.PRE_GAME)) {
            pregameBoard.update(Bukkit.getOnlinePlayers().size());
        }

        broadcast();
    }

    public void onItemCollected(Team team) {
        if (game.getState().equals(Game.State.IN_GAME)) {
            ingameBoards.get(team).updateNumItems(team.getBingoCard().getNumberOfCollectedItems());

            if (game.getConfig().showCurrentlyWinningTeam() && game.getWinConditionChecker().isFullCard()) {
                List<Team> winningTeams = new ArrayList<>();
                int highestNumItems = 0;

                for (Team activeTeam : game.getTeamManager().getTeams()) {
                    int numCollectedItems = activeTeam.getBingoCard().getNumberOfCollectedItems();
                    if (numCollectedItems > highestNumItems) {
                        winningTeams.clear();
                        highestNumItems = numCollectedItems;
                    }

                    if (numCollectedItems >= highestNumItems) {
                        winningTeams.add(activeTeam);
                    }
                }

                if (winningTeams.size() != 1) {
                    for (Team activeTeam : game.getTeamManager().getTeams()) {
                        ingameBoards.get(activeTeam).updateWinningTeam(null);
                    }
                } else {
                    for (Team activeTeam : game.getTeamManager().getTeams()) {
                        ingameBoards.get(activeTeam).updateWinningTeam(winningTeams.get(0));
                    }
                }
            }
        }
    }

    public void onTimeUpdate(long timeLeft) {
        if (game.getState().equals(Game.State.IN_GAME)) {
            for (IngameBoard ingameBoard : ingameBoards.values()) {
                ingameBoard.updateTime(timeLeft);
            }
        }
    }

    public void broadcast() {
        if (game.getState().equals(Game.State.PRE_GAME)) {
            pregameBoard.update(Bukkit.getOnlinePlayers().size());
            pregameBoard.broadcast();
        } else if (game.getState().equals(Game.State.IN_GAME)) {
            for (IngameBoard ingameBoard : ingameBoards.values()) {
                ingameBoard.update();
                ingameBoard.broadcast();
            }
        }
    }

}
