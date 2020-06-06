package com.extremelyd1.game.winCondition;

import com.extremelyd1.game.team.Team;

/**
 * The reason a certain team has won the game, or that the game has ended
 */
public class WinReason {

    /**
     * The team that has won the game, or null if no team has won
     */
    private final Team team;

    /**
     * The reason that the game has been won, or has ended
     */
    private final Reason reason;

    /**
     * Create a win reason with a team and a reason
     * @param team The team that has won
     * @param reason The reason that the team has won
     */
    public WinReason(Team team, Reason reason) {
        this.team = team;
        this.reason = reason;
    }

    /**
     * Creates a win reason that has no winning team
     */
    public WinReason() {
        this.team = null;
        this.reason = Reason.NO_WINNER;
    }

    public Team getTeam() {
        return team;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        COMPLETE,
        RANDOM_TIE,
        NO_WINNER
    }

}
