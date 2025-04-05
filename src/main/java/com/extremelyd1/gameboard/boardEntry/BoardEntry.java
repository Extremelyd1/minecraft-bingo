package com.extremelyd1.gameboard.boardEntry;

import net.kyori.adventure.text.Component;

/**
 * Represents an entry on the scoreboard.
 */
public class BoardEntry {

    /**
     * The component for this entry.
     */
    protected final Component component;

    public BoardEntry(Component component) {
        this.component = component;
    }

    /**
     * Get the component for this entry.
     * @return The component.
     */
    public Component getComponent() {
        return component;
    }

}
