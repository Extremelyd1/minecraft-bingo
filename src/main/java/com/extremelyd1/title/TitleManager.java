package com.extremelyd1.title;

import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.winCondition.WinReason;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Manager class to send titles to players.
 */
public class TitleManager {

    public TitleManager() {
    }

    /**
     * Send the start title to all players.
     */
    public void sendStartTitle() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(Title.title(
                    Component
                            .text("BINGO")
                            .color(NamedTextColor.BLUE)
                            .decorate(TextDecoration.BOLD),
                    Component
                            .text("Game has started!"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(1500), Duration.ofMillis(500))
            ));
        }
    }

    /**
     * Send the end title to all players based on the win reason.
     * @param winReason The win reason.
     */
    public void sendEndTitle(WinReason winReason) {
        Component title;
        Component subtitle = Component.empty();

        switch (winReason.getReason()) {
            case COMPLETE:
                PlayerTeam team = winReason.getTeam();

                title = Component
                        .text("BINGO")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.BOLD);
                subtitle = Component
                        .text(team.getName())
                        .color(team.getColor())
                        .append(Component
                                .text(" team has won the game!")
                                .color(NamedTextColor.WHITE)
                        );
                break;
            case RANDOM_TIE:
                title = Component
                        .text("Game has ended!");
                subtitle = Component
                        .text("It is a tie")
                        .color(NamedTextColor.BLUE);
                break;
            case NO_WINNER:
            default:
                title = Component
                        .text("Game has ended!");
                break;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(Title.title(
                    title,
                    subtitle,
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            ));
        }
    }
}
