package com.extremelyd1.game;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private State state;

    private final List<Team> teams;

    public Game() {
        state = State.PRE_GAME;

        teams = new ArrayList<>();
    }

    private enum State {
        PRE_GAME,
        IN_GAME,
        POST_GAME
    }

}
