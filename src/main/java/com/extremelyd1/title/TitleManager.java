package com.extremelyd1.title;

import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.winCondition.WinReason;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Manager class to send titles to players
 */
public class TitleManager {

    public TitleManager() {
    }

    /**
     * Send the start title to all players
     */
    public void sendStartTitle() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(
                    ChatColor.BOLD.toString() + ChatColor.BLUE + "BINGO",
                    "Game has started!",
                    10,
                    30,
                    10
            );
        }
    }

    /**
     * Send the end title to all players based on the win reason
     * @param winReason The win reason
     */
    public void sendEndTitle(WinReason winReason) {
        String title = "";
        String subtitle = "";

        switch (winReason.getReason()) {
            case COMPLETE:
                PlayerTeam team = winReason.getTeam();

                title = ChatColor.BOLD.toString() + ChatColor.BLUE + "BINGO";
                subtitle = team.getColor() + team.getName()
                        + ChatColor.WHITE + " team "
                        + "has won the game!";
                break;
            case RANDOM_TIE:
                title = "Game has ended!";
                subtitle = ChatColor.BLUE + "It is a tie";
                break;
            case NO_WINNER:
            default:
                title = "Game has ended!";
                break;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(
                    title,
                    subtitle,
                    10,
                    60,
                    10
            );
        }
    }
}
