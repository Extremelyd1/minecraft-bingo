package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.gameboard.boardEntry.BlankBoardEntry;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import com.extremelyd1.gameboard.boardEntry.DynamicBoardEntry;
import com.extremelyd1.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Represents the scoreboard in the pregame state.
 */
public class PregameBoard extends GameBoard {

    /**
     * The entry representing the number of players online.
     */
    private final DynamicBoardEntry<Integer> numPlayersEntry;

    /**
     * The entry representing the current item distribution.
     */
    private final DynamicBoardEntry<Component> currentItemDistribution;

    /**
     * The entry representing the timer status.
     */
    private final DynamicBoardEntry<Component> timerStatus;

    /**
     * The entry representing the game type.
     */
    private final DynamicBoardEntry<String> gameTypeEntry;

    public PregameBoard(Game game) {
        super(game);

        BoardEntry.reset(objective);

        new BlankBoardEntry();
        new BoardEntry(Component
                .text("Status: ")
                .append(Component
                        .text("Pre-Game")
                        .color(NamedTextColor.YELLOW)
                )
        );
        new BoardEntry(Component
                .text("Waiting for players...")
        );
        new BlankBoardEntry();
        numPlayersEntry = new DynamicBoardEntry<>(Component
                .text("Players: ")
                .append(Component
                        .text("%s")
                        .color(NamedTextColor.YELLOW)
                ),
                0
        );
        new BlankBoardEntry();
        new BoardEntry(Component
                .text("Item Distribution:")
        );

        currentItemDistribution = new DynamicBoardEntry<>(
                Component.empty(),
                getItemDistributionComponent(game)
        );
        new BlankBoardEntry();

        timerStatus = new DynamicBoardEntry<>(Component
                .text("Timer: "),
                Component
                        .text("00:00")
        );
        new BlankBoardEntry();

        gameTypeEntry = new DynamicBoardEntry<>(Component
                .text("Game Type: ")
                .append(Component
                        .text("%s")
                        .color(NamedTextColor.GOLD)
                ),
                "-"
        );
        new BlankBoardEntry();
    }

    /**
     * Get the item distribution component for the board.
     * @param game The game instance for getting the item distribution configuration.
     * @return A component that represents the item distribution.
     */
    private static Component getItemDistributionComponent(Game game) {
        return Component
                .text(game.getConfig().getNumSTier())
                .color(NamedTextColor.DARK_RED)
                .appendSpace()
                .append(Component
                        .text(game.getConfig().getNumATier())
                        .color(NamedTextColor.RED)
                ).appendSpace()
                .append(Component
                        .text(game.getConfig().getNumBTier())
                        .color(NamedTextColor.GOLD)
                ).appendSpace()
                .append(Component
                        .text(game.getConfig().getNumCTier())
                        .color(NamedTextColor.YELLOW)
                ).appendSpace()
                .append(Component
                        .text(game.getConfig().getNumDTier())
                        .color(NamedTextColor.GREEN)
                );
    }

    /**
     * Get the status of the timer.
     * @param game The game instance for getting the timer configuration.
     * @return A component that represents the timer status.
     */
    private static Component getTimerStatus(Game game) {
        if (game.getConfig().isTimerEnabled()) {
            return Component
                    .text(TimeUtil.formatTimeLeft(game.getConfig().getTimerLength()))
                    .color(NamedTextColor.GREEN);
        } else {
            return Component
                    .text("Disabled")
                    .color(NamedTextColor.RED);
        }
    }

    /**
     * Updates this board with the new number of players.
     * @param numPlayers The new number of players.
     */
    public void update(Game game, int numPlayers) {
        numPlayersEntry.setValue(numPlayers);
        currentItemDistribution.setValue(getItemDistributionComponent(game));
        timerStatus.setValue(getTimerStatus(game));
        gameTypeEntry.setValue(formatWinCondition(game.getWinConditionChecker()));

        updateTeamColors();
    }
}
