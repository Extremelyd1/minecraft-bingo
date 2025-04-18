package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class JoinCommand implements BasicCommand {

    /**
     * The game instance.
     */
    private final Game game;

    public JoinCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, false, false)) {
            return;
        }

        CommandSender sender = commandSourceStack.getSender();

        // Prevent the command from being used anything apart from pre-game
        if (!game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command now")
                    .color(NamedTextColor.WHITE)
            ));

            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        if (!(commandSourceStack.getExecutor() instanceof Player player)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command for a non-player entity")
                    .color(NamedTextColor.WHITE)
            ));
            return;
        }

        PlayerTeam checkTeam = game.getTeamManager().getTeamByName(args[0]);
        if (checkTeam != null) {
            game.getTeamManager().addPlayerToTeam(player, checkTeam);
            game.onPregameUpdate();
        } else {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Could not find team with that name")
                    .color(NamedTextColor.WHITE)
            ));
        }
    }

    /**
     * Send the usage of this command to the given sender.
     * @param sender The sender to send the command to.
     */
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component
                .text("Usage: ")
                .color(NamedTextColor.DARK_RED)
                .append(Component
                        .text("/join <team name>")
                        .color(NamedTextColor.WHITE)
                )
        );
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        List<String> teams = new ArrayList<>();

        if (!(commandSourceStack.getSender() instanceof Player) || args.length != 1) {
            return teams;
        }

        for (PlayerTeam team : game.getTeamManager().getAvailableTeams()) {
            if (team.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                teams.add(team.getName());
            }
        }

        return teams;
    }
}
