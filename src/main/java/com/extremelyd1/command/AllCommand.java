package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AllCommand implements TabExecutor {

    /**
     * The game instance.
     */
    private final Game game;

    public AllCommand(Game game) {
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

        Player player = (Player) sender;

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command without a team")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Please specify a message")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        String message = String.join(" ", args);

        Bukkit.broadcast(Component
                .text(player.getName())
                .color(team.getColor())
                .append(Component
                        .text(": " + message)
                        .color(NamedTextColor.WHITE)
                )
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        return Collections.emptyList();
    }
}
