package com.extremelyd1.game.winCondition;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.config.Config;
import com.extremelyd1.game.team.PlayerTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A class that stores and handles win conditions
 * Such as needing a full card to win the game
 */
public class WinConditionChecker {

    /**
     * How many lines need to be completed in order to win
     */
    private int numLinesComplete;
    /**
     * Whether teams need to complete the full card in order to win
     */
    private boolean fullCard;

    public WinConditionChecker(Config config) {
        this.numLinesComplete = config.getDefaultNumLinesComplete();
        this.fullCard = false;
    }

    /**
     * Checks whether a bingo card is completed according to the win conditions
     * @param card The bingo card to check
     * @return Whether this bingo card has won
     */
    public boolean hasBingo(BingoCard card) {
        if (fullCard) {
            return card.isCardComplete();
        } else {
            if (numLinesComplete == 1) {
                return card.hasLineComplete();
            } else {
                return card.getNumLinesComplete() >= numLinesComplete;
            }
        }
    }

    /**
     * Decide the winner when the timer ends
     * @param teams The list of teams to choose from
     * @return The team that won
     */
    public WinReason decideWinner(Iterable<PlayerTeam> teams) {
        if (!fullCard && numLinesComplete == 1) {
            // We cannot really determine a winner now
            return new WinReason();
        }

        List<PlayerTeam> potentialWinners = new ArrayList<>();
        int maxScore = 0;
        for (PlayerTeam team : teams) {
            int score = team.getBingoCard().getNumberOfCollectedItems();

            if (score > maxScore) {
                potentialWinners.clear();
                maxScore = score;
            }

            if (score >= maxScore) {
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

    /**
     * Sets the number of lines to complete in order to win
     * @param numLinesComplete The number of lines to complete; must be between 1 (inclusive) and 10 (inclusive)
     */
    public void setNumLinesComplete(int numLinesComplete) {
        if (numLinesComplete < 1 || numLinesComplete > 10) {
            throw new IllegalArgumentException("Cannot set number of lines completed to less than 1 or more than 10");
        }

        this.numLinesComplete = numLinesComplete;
    }

    public int getNumLinesComplete() {
        return numLinesComplete;
    }

    public void setFullCard(boolean fullCard) {
        this.fullCard = fullCard;
    }

    public boolean isFullCard() {
        return fullCard;
    }
}
