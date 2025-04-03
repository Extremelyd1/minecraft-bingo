package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class MotdListener implements Listener {
    /**
     * The game instance.
     */
    private final Game game;

    public MotdListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent e) {
        if (game.isMaintenance()) {
            e.motd(Component
                    .text("Maintenance mode")
                    .appendNewline()
                    .append(Component
                            .text("Unable to join")
                            .color(NamedTextColor.DARK_RED)
                    )
            );
            e.setMaxPlayers(0);
        } else if (game.getConfig().isPreGenerateWorlds()) {
            e.motd(Component
                    .text( "Pre-generating worlds")
                    .appendNewline()
                    .append(Component
                            .text("Unable to join")
                            .color(NamedTextColor.DARK_RED)
                    )
            );
            e.setMaxPlayers(0);
        } else {
            e.motd(Component
                    .text("BINGO")
                    .color(NamedTextColor.BLUE)
                    .decorate(TextDecoration.BOLD)
                    .appendSpace()
                    .append(Component
                            .text(game.getState().getName())
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.BOLD, false)
                    ).appendNewline()
                    .append(Component
                            .text("Players: ")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.BOLD, false)
                    ).append(Component
                            .text(Bukkit.getOnlinePlayers().size())
                            .color(NamedTextColor.AQUA)
                            .decoration(TextDecoration.BOLD, false)
                    )
            );
        }
    }

}
