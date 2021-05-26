package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.winCondition.WinReason;
import org.bukkit.Bukkit;

import java.util.HashMap;
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
    public void createIngameBoards(Iterable<PlayerTeam> teams) {
        if (ingameBoards == null) {
            ingameBoards = new HashMap<>();
        }

        for (PlayerTeam team : teams) {
            ingameBoards.put(team, new IngameBoard(game, team));
        }
    }

    public void createSpectatorBoard(Team spectatorTeam) {
        if (ingameBoards == null) {
            ingameBoards = new HashMap<>();
        }

        ingameBoards.put(spectatorTeam, new IngameBoard(game, spectatorTeam));
    }

    /**
     * Updates scoreboards in pregame
     * @param numOnlinePlayers The number of online players
     */
    public void onPregameUpdate(int numOnlinePlayers) {
        if (game.getState().equals(Game.State.PRE_GAME)) {
            pregameBoard.update(game, numOnlinePlayers);
        }

        broadcast();
    }

    /**
     * When an item is collected by a certain team
     * @param team The team that collects the item
     */
    public void onItemCollected(PlayerTeam team) {
        if (game.getState().equals(Game.State.IN_GAME)) {
            ingameBoards.get(team).updateNumItems(team.getNumCollected());

            if (game.getConfig().showCurrentlyWinningTeam()) {
                // Obtain a preliminary win reason
                WinReason winReason = game.getWinConditionChecker().decideWinner(
                        game.getTeamManager().getActiveTeams()
                );

                // Based on this win reason update the in-game boards
                PlayerTeam leadingTeam = null;
                if (!winReason.getReason().equals(WinReason.Reason.RANDOM_TIE)) {
                    leadingTeam = winReason.getTeam();
                }

                for (IngameBoard ingameBoard : ingameBoards.values()) {
                    ingameBoard.updateWinningTeam(leadingTeam);
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
            pregameBoard.update(game, Bukkit.getOnlinePlayers().size());
            pregameBoard.broadcast();
        } else if (game.getState().equals(Game.State.IN_GAME)) {
            for (IngameBoard ingameBoard : ingameBoards.values()) {
                ingameBoard.update();
                ingameBoard.broadcast();
            }
        }
    }

}
