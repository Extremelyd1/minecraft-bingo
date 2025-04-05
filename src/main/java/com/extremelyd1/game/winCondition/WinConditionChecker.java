package com.extremelyd1.game.winCondition;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.config.Config;
import com.extremelyd1.game.team.PlayerTeam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A class that stores and handles/checks win conditions. Such as needing a full card to win the game or completing
 * a certain number of lines.
 */
public class WinConditionChecker {

    /**
     * How many lines need to be completed in order to win.
     * A value of zero indicates that the 'lines' objective is not enabled.
     */
    private int numLinesToComplete;
    /**
     * Whether teams need to complete the full card in order to win.
     */
    private boolean fullCard;
    /**
     * The number of completions for an item to lock it for the remaining teams.
     * A value of zero will indicate that no locking will occur.
     */
    private int completionsToLock;

    public WinConditionChecker(Config config) {
        this.numLinesToComplete = config.getDefaultNumLinesComplete();
        this.fullCard = false;
        this.completionsToLock = 0;
    }

    /**
     * Checks whether the game is finished and returns a possibly empty list of the current winners.
     * @param card The bingo card to check.
     * @param team The team to check for.
     * @param allTeams All the teams participating.
     * @return An empty list if there are no winners and otherwise a list containing the teams that won.
     */
    public List<PlayerTeam> getCurrentWinners(BingoCard card, PlayerTeam team, Iterable<PlayerTeam> allTeams) {
        if (completionsToLock > 0) {
            return getLockoutWinner(card, allTeams);
        }

        if (hasBingo(card, team)) {
            return Collections.singletonList(team);
        }

        return Collections.emptyList();
    }

    /**
     * Checks whether there is a winner in the 'lockout' game type.
     * @param card The bingo card to check.
     * @param allTeams All the teams participating.
     * @return A list of teams that have won.
     */
    private List<PlayerTeam> getLockoutWinner(BingoCard card, Iterable<PlayerTeam> allTeams) {
        // If this method is called, the iterable of PlayerTeam instances is never empty
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        int maxNumCollected = StreamSupport
                .stream(allTeams.spliterator(), false)
                .mapToInt(PlayerTeam::getNumCollected)
                .max().getAsInt();

        List<PlayerTeam> maxCollectionsTeams = StreamSupport
                .stream(allTeams.spliterator(), false)
                .filter(team -> team.getNumCollected() == maxNumCollected)
                .collect(Collectors.toList());

        for (PlayerTeam maxCollectionTeam : maxCollectionsTeams) {
            for (PlayerTeam otherTeam : allTeams) {
                if (otherTeam.equals(maxCollectionTeam)) {
                    continue;
                }

                // If this other team can collect at least the same number of items as the leading team
                // currently has, then the leading team has not achieved victory yet
                if (getPossibleNumCollections(card, otherTeam) > maxNumCollected) {
                    return new ArrayList<>();
                }
            }
        }

        return maxCollectionsTeams;
    }

    /**
     * Checks whether the given team has achieved bingo in 'full card' or 'lines' game types.
     * @param card The bingo card to check.
     * @param team The team to check for.
     * @return True if the given team has achieved bingo, false otherwise.
     */
    private boolean hasBingo(BingoCard card, PlayerTeam team) {
        if (fullCard) {
            return card.isCardComplete(team);
        } else {
            return card.getNumLinesComplete(team) >= numLinesToComplete;
        }
    }

    /**
     * Finds the teams that have the maximum score based on a score function.
     * @param teams The teams to iterate over.
     * @param scoreFunc The score functions that takes a team and returns an integer score.
     * @return A list containing the teams with the maximum score.
     */
    private List<PlayerTeam> findMax(Iterable<PlayerTeam> teams, Function<PlayerTeam, Integer> scoreFunc) {
        List<PlayerTeam> maxTeams = new ArrayList<>();
        int maxScore = 0;
        for (PlayerTeam team : teams) {
            int score = scoreFunc.apply(team);

            if (score > maxScore) {
                maxTeams.clear();
                maxScore = score;
            }

            if (score >= maxScore) {
                maxTeams.add(team);
            }
        }

        return maxTeams;
    }

    /**
     * Decide the winner when the timer ends.
     * @param teams The list of teams to choose from.
     * @param bingoCard The bingo card that is used.
     * @return The team that won.
     */
    public WinReason decideWinner(Iterable<PlayerTeam> teams, BingoCard bingoCard) {
        List<PlayerTeam> potentialWinners = findMax(teams, t -> {
            if (fullCard || completionsToLock > 0) {
                return t.getNumCollected();
            }

            return bingoCard.getNumLinesComplete(t);
        });

        // If we have multiple potential winners, but we are playing with the "lines" objective, we can (potentially)
        // break the tie by checking the total number of collected items
        if (potentialWinners.size() > 1 && numLinesToComplete > 0) {
            potentialWinners = findMax(potentialWinners, PlayerTeam::getNumCollected);
        }

        WinReason winReason;

        if (potentialWinners.size() > 1) {
            winReason = new WinReason(
                    potentialWinners.get(new Random().nextInt(potentialWinners.size())),
                    WinReason.Reason.RANDOM_TIE
            );
        } else {
            winReason = new WinReason(
                    potentialWinners.getFirst(),
                    WinReason.Reason.COMPLETE
            );
        }

        return winReason;
    }

    /**
     * The number of items that the given team can potentially collect on the given bingo card.
     * @param card The bingo card with items.
     * @param team The team to calculate this for.
     * @return An integer representing the number of items that can be collected.
     */
    private int getPossibleNumCollections(BingoCard card, PlayerTeam team) {
        // Keep track of how many items can be collected in total by this team
        int possibleNumCollected = 0;

        for (BingoItem[] bingoItems : card.getBingoItems()) {
            for (BingoItem bingoItem : bingoItems) {
                if (bingoItem.hasCollected(team)) {
                    // If this item is locked, but has already been collected by the team, it still counts
                    possibleNumCollected++;
                } else if (!card.isItemLocked(bingoItem)) {
                    // If the item is not yet locked, it is possible for this team to still collect it
                    possibleNumCollected++;
                }
            }
        }

        return possibleNumCollected;
    }

    /**
     * Sets the number of lines to complete in order to win.
     * @param numLinesToComplete The number of lines to complete; must be between 1 (inclusive) and 10 (inclusive).
     */
    public void setNumLinesToComplete(int numLinesToComplete) {
        if (numLinesToComplete < 1 || numLinesToComplete > 10) {
            throw new IllegalArgumentException("Cannot set number of lines completed to less than 1 or more than 10");
        }

        this.fullCard = false;
        this.completionsToLock = 0;
        this.numLinesToComplete = numLinesToComplete;
    }

    public int getNumLinesToComplete() {
        return numLinesToComplete;
    }

    public void setFullCard() {
        this.completionsToLock = 0;
        this.numLinesToComplete = 0;
        this.fullCard = true;
    }

    public boolean isFullCard() {
        return fullCard;
    }

    public int getCompletionsToLock() {
        return completionsToLock;
    }

    public void setCompletionsToLock(int completionsToLock) {
        this.fullCard = false;
        this.numLinesToComplete = 0;
        this.completionsToLock = completionsToLock;
    }
}
