package com.extremelyd1.command;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MaintenanceCommand implements TabExecutor {

    /**
     * The game instance
     */
    private final Game game;

    public MaintenanceCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!CommandUtil.checkCommandSender(sender, true, true)) {
            return true;
        }

        game.toggleMaintenance();

        if (!game.isMaintenance()) {
            Bukkit.broadcastMessage(
                    Game.PREFIX + "Maintenance is now " + ChatColor.DARK_RED + "disabled"
            );
        } else {
            Bukkit.broadcastMessage(
                    Game.PREFIX + "Maintenance is now " + ChatColor.GREEN + "enabled"
            );
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
