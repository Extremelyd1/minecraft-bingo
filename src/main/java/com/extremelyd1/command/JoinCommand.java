package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JoinCommand implements TabExecutor {

    /**
     * The game instance.
     */
    private final Game game;

    public JoinCommand(Game game) {
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

        // Prevent the command from being used anything apart from pre-game
        if (!game.getState().equals(Game.State.PRE_GAME)) {
            sender.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("Cannot execute this command now")
                    .color(NamedTextColor.WHITE)
            ));

            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(sender, command);
            return true;
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

        return true;
    }

    /**
     * Send the usage of this command to the given sender.
     * @param sender The sender to send the command to.
     * @param command The command instance.
     */
    private void sendUsage(CommandSender sender, Command command) {
        sender.sendMessage(Component
                .text("Usage: ")
                .color(NamedTextColor.DARK_RED)
                .append(Component
                        .text("/" + command.getName() + " <team name>")
                        .color(NamedTextColor.WHITE)
                )
        );
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String @NotNull [] args
    ) {
        List<String> teams = new ArrayList<>();

        if (!(sender instanceof Player) || args.length != 1) {
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
