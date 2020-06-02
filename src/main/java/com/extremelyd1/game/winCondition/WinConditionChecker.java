package com.extremelyd1.game.winCondition;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.config.Config;
import com.extremelyd1.game.team.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WinConditionChecker {

    private int numLinesComplete;
    private boolean fullCard;

    public WinConditionChecker(Config config) {
        this.numLinesComplete = config.getDefaultNumLinesComplete();
        this.fullCard = false;
    }

    public boolean hasBingo(BingoCard card) {
        if (fullCard) {
            return card.isCardComplete();
        } else {
            if (numLinesComplete == 1) {
                return card.hasLineComplete();
            } else {
                return card.getNumLinesComplete() == numLinesComplete;
            }
        }
    }

    /**
     * Decide the winner when the timer ends
     * @param teams The list of teams to choose from
     * @return The team that won
     */
    public WinReason decideWinner(List<Team> teams) {
        if (!fullCard && numLinesComplete == 1) {
            // We cannot really determine a winner now
            return new WinReason();
        }

        List<Team> potentialWinners = new ArrayList<>();
        int score = 0;
        for (Team team : teams) {
            int newScore;

            newScore = team.getBingoCard().getNumberOfCollectedItems();

            if (newScore > score) {
                potentialWinners.clear();
                score = newScore;
            }

            if (newScore >= score) {
                potentialWinners.add(team);
            }
        }

        WinReason winReason;

        if (potentialWinners.size() > 1) {
            winReason = new WinReason(
                    potentialWinners.get(new Random().nextInt(potentialWinners.size())),
                    WinReason.Reason.RANDOM_TIE
            );
        } else {
            winReason = new WinReason(
                    potentialWinners.get(0),
                    WinReason.Reason.COMPLETE
            );
        }

        return winReason;
    }

    public void setNumLinesComplete(int numLinesComplete) {
        if (numLinesComplete < 1 || numLinesComplete > 10) {
            throw new IllegalArgumentException("Cannot set number of lines completed to less than 1 or more than 10");
        }

        this.numLinesComplete = numLinesComplete;
    }

    public void setFullCard(boolean fullCard) {
        this.fullCard = fullCard;
    }

    public boolean isFullCard() {
        return fullCard;
    }
}
