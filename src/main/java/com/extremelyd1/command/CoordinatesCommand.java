package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class CoordinatesCommand implements BasicCommand {

    /**
     * The game instance.
     */
    private final Game game;

    public CoordinatesCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, false, false)) {
            return;
        }

        CommandSender sender = commandSourceStack.getSender();
        if (game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command in pre-game")
                    .color(NamedTextColor.WHITE)
            ));

            return;
        }

        Player player = (Player) sender;

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null || team.isSpectatorTeam()) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command as spectator")
                    .color(NamedTextColor.WHITE)
            ));

            return;
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
    }
}
