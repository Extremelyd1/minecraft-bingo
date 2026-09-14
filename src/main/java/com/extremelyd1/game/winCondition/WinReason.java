package com.extremelyd1.game.winCondition;

import com.extremelyd1.game.team.PlayerTeam;

/**
 * The reason a certain team has won the game, or that the game has ended
 *
 * @param team   The team that has won the game, or null if no team has won
 * @param reason The reason that the game has been won, or has ended
 */
public record WinReason(PlayerTeam team, Reason reason) {
    /**
     * Creates a win reason that has no winning team
     */
    public WinReason() {
        this(null, Reason.NO_WINNER);
    }

    public enum Reason {
        COMPLETE,
        RANDOM_TIE,
        NO_WINNER
    }
}
