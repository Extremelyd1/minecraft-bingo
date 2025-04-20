package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.gameboard.boardEntry.BlankBoardEntry;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import com.extremelyd1.gameboard.boardEntry.DynamicBoardEntry;
import com.extremelyd1.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * Represents the in game scoreboard.
 */
public class IngameBoard extends GameBoard {

    /**
     * The team this scoreboard belongs to.
     */
    private final Team team;

    /**
     * The entry representing the number of items collected.
     */
    private final DynamicBoardEntry<Integer> numItemsEntry;
    /**
     * The entry representing the timer.
     */
    private final DynamicBoardEntry<String> timeLeftEntry;
    /**
     * The entry representing the currently winning team.
     */
    private final DynamicBoardEntry<Component> winningTeamEntry;

    public IngameBoard(Game game, Team team) {
        super(game);

        this.team = team;

        BoardEntry.reset(objective);

        new BlankBoardEntry();
        new BoardEntry(Component
                .text("Status: ")
                .append(Component
                        .text("In-Game")
                        .color(NamedTextColor.YELLOW)
                )
        );

        new BoardEntry(Component
                .text("Game type: ")
                .append(Component
                        .text(formatWinCondition(game.getWinConditionChecker()))
                        .color(NamedTextColor.YELLOW)
                )
        );

        if (game.getConfig().isTimerEnabled()) {
            timeLeftEntry = new DynamicBoardEntry<>(Component
                    .text("Time left: ")
                    .append(Component
                            .text(DynamicBoardEntry.replacePlaceholder)
                            .color(NamedTextColor.YELLOW)
                    ),
                    "0:00"
            );
        } else {
            timeLeftEntry = null;
        }

        new BlankBoardEntry();

        new BoardEntry(Component
                .text("Team: ")
                .append(Component
                        .text(team.getName())
                        .color(team.getColor())
                )
        );
        new BlankBoardEntry();

        if (!team.isSpectatorTeam()) {
            new BoardEntry(Component
                    .text("Number of items collected:")
            );
            numItemsEntry = new DynamicBoardEntry<>(Component
                    .text("  " + DynamicBoardEntry.replacePlaceholder)
                    .color(NamedTextColor.AQUA),
                    0
            );
            new BlankBoardEntry();
        } else {
            numItemsEntry = null;
        }

        if (game.getConfig().showCurrentlyWinningTeam()) {
            new BoardEntry(Component
                    .text("Leading team:")
            );
            winningTeamEntry = new DynamicBoardEntry<>(Component
                    .text("  "),
                    Component
                            .text("Tie")
                            .color(NamedTextColor.GRAY)
            );
            new BlankBoardEntry();
        } else {
            winningTeamEntry = null;
        }
    }

    /**
     * Updates this board with a new number of items.
     * @param numItems The new number of items.
     */
    public void updateNumItems(int numItems) {
        numItemsEntry.setValue(numItems);
    }

    /**
     * Updates this board with the new time left.
     * @param timeLeft The new time left.
     */
    public void updateTime(long timeLeft) {
        timeLeftEntry.setValue(TimeUtil.formatTimeLeft(timeLeft));
    }

    /**
     * Updates this board with the currently winning team.
     * @param team The currently winning team.
     */
    public void updateWinningTeam(PlayerTeam team) {
        if (winningTeamEntry == null) {
            return;
        }

        if (team == null) {
            winningTeamEntry.setValue(Component
                    .text("Tie")
                    .color(NamedTextColor.GRAY)
            );
        } else {
            winningTeamEntry.setValue(Component
                    .text(team.getName())
                    .color(team.getColor())
            );
        }
    }

    /**
     * Broadcasts this board to all the team's members.
     */
    public void broadcast() {
        for (Player player : team.getPlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

}
