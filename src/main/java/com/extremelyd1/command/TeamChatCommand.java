package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class TeamChatCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public TeamChatCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, false, false)) {
            return true;
        }

        Player player = (Player) sender;

        Team team = game.getTeamManager().getTeamByPlayer(player);

        String message = String.join(" ", strings);

        if (team == null) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("You are not on a team")
                    .color(NamedTextColor.WHITE))
            );
        } else {
            for (Player p : team.getPlayers()) {
                p.sendMessage(Component
                        .text("TEAM " + player.getName())
                        .color(team.getColor())
                        .append(Component
                                .text(": " + message)
                                .color(NamedTextColor.WHITE)
                        )
                );
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
