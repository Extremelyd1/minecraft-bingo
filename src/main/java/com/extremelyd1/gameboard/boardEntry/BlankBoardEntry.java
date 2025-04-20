package com.extremelyd1.gameboard.boardEntry;

import net.kyori.adventure.text.Component;

/**
 * Represents a blank entry on the scoreboard.
 * Can be used as a divider between other entries.
 */
public class BlankBoardEntry extends BoardEntry {

    public BlankBoardEntry() {
        super(Component.empty());
    }
}
