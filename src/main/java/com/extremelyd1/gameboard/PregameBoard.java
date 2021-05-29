package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.gameboard.boardEntry.BlankBoardEntry;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import com.extremelyd1.gameboard.boardEntry.DynamicBoardEntry;
import com.extremelyd1.util.TimeUtil;
import org.bukkit.ChatColor;

/**
 * Represents the scoreboard in the pregame state
 */
public class PregameBoard extends GameBoard {

    /**
     * The entry representing the number of players online
     */
    private final DynamicBoardEntry<Integer> numPlayersEntry;

    private final DynamicBoardEntry<String> currentItemDistribution;

    private final DynamicBoardEntry<String> timerStatus;

    private final DynamicBoardEntry<String> gameTypeEntry;

    public PregameBoard(Game game) {
        super(game);

        this.objective.setDisplayName(
                ChatColor.BOLD.toString()
                        + ChatColor.YELLOW.toString()
                        + "Minecraft Bingo"
        );

        int numberOfSpaces = 1;
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));
        this.boardEntries.add(new BoardEntry(
                "Status: " + ChatColor.YELLOW + "Pre-Game"
        ));
        this.boardEntries.add(new BoardEntry("Waiting for players..."));
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));
        numPlayersEntry = new DynamicBoardEntry<>(
                "Players: " + ChatColor.YELLOW + "%d",
                0
        );
        this.boardEntries.add(numPlayersEntry);
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));
        this.boardEntries.add(new BoardEntry("Item Distribution:"));

        currentItemDistribution = new DynamicBoardEntry<>(
                "%s",
                String.format(ChatColor.DARK_RED + "%d "
                        + ChatColor.RED + "%d "
                        + ChatColor.GOLD + "%d "
                        + ChatColor.YELLOW + "%d "
                        + ChatColor.GREEN + "%d",
                        game.getConfig().getNumSTier(),
                        game.getConfig().getNumATier(),
                        game.getConfig().getNumBTier(),
                        game.getConfig().getNumCTier(),
                        game.getConfig().getNumDTier()
                )
        );
        this.boardEntries.add(currentItemDistribution);
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));

        timerStatus = new DynamicBoardEntry<>(
                "Timer: %s",
                "00:00"
        );
        this.boardEntries.add(timerStatus);
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces++));

        gameTypeEntry = new DynamicBoardEntry<>(
                "Game Type: %s",
                "-"
        );
        this.boardEntries.add(gameTypeEntry);
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces));
    }

    private String getItemDistributionString(Game game) {
        return String.format(ChatColor.DARK_RED + "%d "
                        + ChatColor.RED + "%d "
                        + ChatColor.GOLD + "%d "
                        + ChatColor.YELLOW + "%d "
                        + ChatColor.GREEN + "%d",
                game.getConfig().getNumSTier(),
                game.getConfig().getNumATier(),
                game.getConfig().getNumBTier(),
                game.getConfig().getNumCTier(),
                game.getConfig().getNumDTier()
        );
    }

    private String getTimerStatus(Game game) {
        if (game.getConfig().isTimerEnabled()) {
            return ChatColor.GREEN + TimeUtil.formatTimeLeft(game.getConfig().getTimerLength());
        } else {
            return ChatColor.RED + "Disabled";
        }
    }

    /**
     * Updates this board with the new number of players
     * @param numPlayers The new number of players
     */
    public void update(Game game, int numPlayers) {
        numPlayersEntry.setValue(numPlayers);
        currentItemDistribution.setValue(getItemDistributionString(game));
        timerStatus.setValue(getTimerStatus(game));
        gameTypeEntry.setValue(
                ChatColor.GOLD + formatWinCondition(game.getWinConditionChecker())
        );

        super.update();
    }

}
