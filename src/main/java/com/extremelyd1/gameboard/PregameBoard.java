package com.extremelyd1.gameboard;

import com.extremelyd1.game.Game;
import com.extremelyd1.gameboard.boardEntry.BlankBoardEntry;
import com.extremelyd1.gameboard.boardEntry.BoardEntry;
import com.extremelyd1.gameboard.boardEntry.DynamicBoardEntry;
import org.bukkit.ChatColor;

/**
 * Represents the scoreboard in the pregame state
 */
public class PregameBoard extends GameBoard {

    /**
     * The entry representing the number of players online
     */
    private final DynamicBoardEntry<Integer> numPlayersEntry;

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
        this.boardEntries.add(new BlankBoardEntry(numberOfSpaces));
    }

    /**
     * Updates this board with the new number of players
     * @param numPlayers The new number of players
     */
    public void update(int numPlayers) {
        numPlayersEntry.setValue(numPlayers);

        super.update();
    }

}
