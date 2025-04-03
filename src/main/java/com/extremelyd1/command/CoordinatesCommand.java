package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;


public class CoordinatesCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public CoordinatesCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String @NotNull [] args
    ) {
        if (!CommandUtil.checkCommandSender(sender, false, false)) {
            return true;
        }

        if (game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command in pre-game")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        Player player = (Player) sender;

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null || team.isSpectatorTeam()) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command as spectator")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        Location location = player.getLocation();

        // Either empty or the text that the player sends after the command
        String description = "";
        if (args.length > 0) {
            StringBuilder descBuilder = new StringBuilder();
            for (String arg : args) {
                descBuilder.append(" ").append(arg);
            }

            description = descBuilder.toString();
        }

        for (Player teamPlayer : team.getPlayers()) {
            String x = String.valueOf(Math.round(location.getX()));
            String y = String.valueOf(Math.round(location.getY()));
            String z = String.valueOf(Math.round(location.getZ()));

            teamPlayer.sendMessage(Component
                    .text("TEAM " + player.getName())
                    .color(team.getColor())
                    .append(Component
                            .text(": ")
                            .color(NamedTextColor.WHITE)
                    ).append(Component
                            .text("[" + x + ", " + y + ", " + z + "]")
                            .color(NamedTextColor.AQUA)
                    ).append(Component
                            .text(description)
                            .color(NamedTextColor.WHITE)
                    )
            );
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args) {
        return Collections.emptyList();
    }
}
