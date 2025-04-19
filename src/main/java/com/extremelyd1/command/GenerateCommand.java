package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class GenerateCommand implements BasicCommand {

    /**
     * The game instance.
     */
    private final Game game;

    public GenerateCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, true, true)) {
            return;
        }

        if (args.length < 1) {
            sendUsage(commandSourceStack);

            return;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            game.getWorldManager().stopPreGeneration();

            return;
        } else if (args.length < 2) {
            sendUsage(commandSourceStack);

            return;
        }

        int start;
        try {
            start = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sendUsage(commandSourceStack);

            return;
        }

        int numWorlds;
        try {
            numWorlds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendUsage(commandSourceStack);

            return;
        }

        commandSourceStack.getSender().sendMessage(
                "Pre-generating " + numWorlds + " worlds..."
        );

        game.getWorldManager().createWorlds(start, numWorlds);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String[] args) {
        if (args.length == 1) {
            return List.of("stop");
        }

        return Collections.emptyList();
    }

    /**
     * Send the usage of this command to the given command sender.
     * @param commandSourceStack The command source to send the usage to.
     */
    private void sendUsage(@NotNull CommandSourceStack commandSourceStack) {
        commandSourceStack.getSender().sendMessage(Component
                .text("Usage: ")
                .color(NamedTextColor.DARK_RED)
                .append(Component
                        .text("/generate stop | <start> <number of worlds>")
                        .color(NamedTextColor.WHITE)
                )
        );
    }
}
