package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.winCondition.WinReason;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoardManager {

    /**
     * The game instance
     */
    private final Game game;

    /**
     * The pregame board instance
     */
    private final PregameBoard pregameBoard;
    /**
     * A mapping from team to the respective in-game board
     */
    private Map<Team, IngameBoard> ingameBoards;

    public GameBoardManager(Game game) {
        this.game = game;

        this.pregameBoard = new PregameBoard(game);
    }

    /**
     * Creates the boards used while in the in-game phase
     * @param teams An iterable of teams for which to create the boards
     */
    public void createIngameBoards(Iterable<Team> teams) {
        ingameBoards = new HashMap<>();

        for (Team team : teams) {
            ingameBoards.put(team, new IngameBoard(game, team));
        }
    }

    /**
     * Updates scoreboards in pregame
     * @param numOnlinePlayers The number of online players
     */
    public void onPregameUpdate(int numOnlinePlayers) {
        if (game.getState().equals(Game.State.PRE_GAME)) {
            pregameBoard.update(numOnlinePlayers);
        }

        broadcast();
    }

    /**
     * When an item is collected by a certain team
     * @param team The team that collects the item
     */
    public void onItemCollected(Team team) {
        if (game.getState().equals(Game.State.IN_GAME)) {
            ingameBoards.get(team).updateNumItems(team.getBingoCard().getNumberOfCollectedItems());

            if (game.getConfig().showCurrentlyWinningTeam()) {
                // Obtain a preliminary win reason
                WinReason winReason = game.getWinConditionChecker().decideWinner(
                        game.getTeamManager().getTeams()
                );

                // Based on this win reason update the in-game boards
                if (winReason.getReason().equals(WinReason.Reason.RANDOM_TIE)) {
                    for (Team activeTeam : game.getTeamManager().getTeams()) {
                        ingameBoards.get(activeTeam).updateWinningTeam(null);
                    }
                } else {
                    for (Team activeTeam : game.getTeamManager().getTeams()) {
                        ingameBoards.get(activeTeam).updateWinningTeam(winReason.getTeam());
                    }
                }
            }
        }
    }

    /**
     * Called when scoreboard need to update their time
     * @param timeLeft The current time left
     */
    public void onTimeUpdate(long timeLeft) {
        if (game.getState().equals(Game.State.IN_GAME)) {
            for (IngameBoard ingameBoard : ingameBoards.values()) {
                ingameBoard.updateTime(timeLeft);
            }
        }
    }

    /**
     * Broadcasts the appropriate scoreboard to all players
     */
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
