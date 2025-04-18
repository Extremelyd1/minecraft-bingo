package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.winCondition.WinReason;
import com.extremelyd1.util.CommandUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class EndCommand implements BasicCommand {

    /**
     * The game instance.
     */
    private final Game game;

    public EndCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (!CommandUtil.checkCommandSender(commandSourceStack, true, true)) {
            return;
        }

        game.end(new WinReason());
    }
}
