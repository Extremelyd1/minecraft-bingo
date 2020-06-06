package com.extremelyd1.gameboard.boardEntry;

/**
 * Represents a blank entry on the scoreboard
 * Can be used as a divider between other entries
 */
public class BlankBoardEntry extends BoardEntry {

    public BlankBoardEntry(int numberOfSpaces) {
        super(spacedString(numberOfSpaces));
    }

    private static String spacedString(int numberOfSpaces) {
        StringBuilder sb = new StringBuilder(numberOfSpaces);
        for (int i = 0; i < numberOfSpaces; i++) {
            sb.append(" ");
        }

        return sb.toString();
    }

}
