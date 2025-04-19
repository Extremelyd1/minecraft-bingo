package com.extremelyd1.command;

import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ChatUtil;
import com.extremelyd1.util.CommandUtil;
import com.extremelyd1.util.ItemUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class CardCommand implements BasicCommand {

    /**
     * The game instance.
     */
    private final Game game;

    /**
     * The bingo card item factory instance to check whether a given item stack is a bingo card.
     */
    private final BingoCardItemFactory bingoCardItemFactory;

    public CardCommand(Game game, BingoCardItemFactory bingoCardItemFactory) {
        this.game = game;
        this.bingoCardItemFactory = bingoCardItemFactory;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (CommandUtil.checkCommandSender(commandSourceStack, game, false, false, true, true)) {
            return;
        }

        if (!(commandSourceStack.getExecutor() instanceof Player player)) {
            commandSourceStack.getSender().sendMessage("This command can only be executed on a player");
            return;
        }

        Team team = game.getTeamManager().getTeamByPlayer(player);

        if (ItemUtil.hasBingoCard(player, bingoCardItemFactory)) {
            player.sendMessage(ChatUtil.errorPrefix().append(Component
                    .text("You already have a bingo card in your inventory")
                    .color(NamedTextColor.WHITE)
            ));

            return;
        }

        player.getInventory().addItem(
                game.getBingoCardItemFactory().create(game.getBingoCard(), (PlayerTeam) team)
        );

        player.sendMessage(ChatUtil.successPrefix().append(Component
                .text("You have been given a new bingo card")
                .color(NamedTextColor.WHITE)
        ));
    }
}
