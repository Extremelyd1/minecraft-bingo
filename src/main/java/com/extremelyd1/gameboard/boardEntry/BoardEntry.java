package com.extremelyd1.gameboard.boardEntry;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

/**
 * Represents an entry on the scoreboard.
 */
public class BoardEntry {
    /**
     * The last ID used by a board entry.
     */
    private static int lastId;
    /**
     * The last score set for a board entry.
     */
    private static int lastScore = Integer.MAX_VALUE;
    /**
     * The current objective for which to register scores.
     */
    private static Objective currentObjective;

    /**
     * The score instance to modify the custom name for.
     */
    protected final Score score;

    public BoardEntry(Component component) {
        if (currentObjective == null) {
            throw new IllegalStateException("Could not instantiate BoardEntry because current objective is null");
        }

        this.score = currentObjective.getScore(String.valueOf(lastId++));

        this.score.customName(component);

        this.score.numberFormat(NumberFormat.blank());
        this.score.setScore(lastScore--);
    }

    /**
     * Resets the last score and current objective for board entries.
     * Should be called before creating new board entries to ensure their scores are correct and they are registered
     * to the correct objective.
     * @param objective The objective to register scores for.
     */
    public static void reset(Objective objective) {
        lastScore = Integer.MAX_VALUE;
        currentObjective = objective;
    }
}
