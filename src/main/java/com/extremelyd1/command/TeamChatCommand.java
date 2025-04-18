package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class TeamChatCommand implements BasicCommand {

    /**
     * The game instance
     */
    private final Game game;

    public TeamChatCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, false, false)) {
            return;
        }

        Player player = (Player) commandSourceStack.getSender();

        Team team = game.getTeamManager().getTeamByPlayer(player);

        String message = String.join(" ", args);

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
    }
}
