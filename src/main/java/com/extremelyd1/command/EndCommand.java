package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.winCondition.WinReason;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndCommand implements CommandExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public EndCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        game.end(new WinReason());

        return true;
    }
}
