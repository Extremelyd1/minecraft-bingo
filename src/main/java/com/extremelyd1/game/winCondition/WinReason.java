package com.extremelyd1.game.winCondition;

import com.extremelyd1.game.team.Team;

public class WinReason {

    private final Team team;

    private final Reason reason;

    public WinReason(Team team, Reason reason) {
        this.team = team;
        this.reason = reason;
    }

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
