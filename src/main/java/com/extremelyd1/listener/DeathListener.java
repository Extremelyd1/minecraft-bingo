package com.extremelyd1.listener;

import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.potion.PotionEffects;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DeathListener implements Listener {

    /**
     * The game instance.
     */
    private final Game game;

    /**
     * The bingo card item factory instance to check whether an item is a bingo card.
     */
    private final BingoCardItemFactory bingoCardItemFactory;

    public DeathListener(Game game, BingoCardItemFactory bingoCardItemFactory) {
        this.game = game;
        this.bingoCardItemFactory = bingoCardItemFactory;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        for (int i = 0; i < e.getDrops().size(); i++) {
            ItemStack itemStack = e.getDrops().get(i);
            if (bingoCardItemFactory.isBingoCard(itemStack)) {
                e.getDrops().remove(i);
                i--;
            }
        }

        Component deathMessageComponent = e.deathMessage();
        if (deathMessageComponent == null) {
            Game.getLogger().warning("Player death event has no death message");
            return;
        }

        if (!(deathMessageComponent instanceof TranslatableComponent translatableComponent)) {
            Game.getLogger().warning("Player death event has no translatable component in death message");
            return;
        }

        // Get the list of original translation arguments in the translatable component
        List<TranslationArgument> translationArguments = translatableComponent.arguments();

        // Create a new list of ComponentLike by mapping each translation argument to a ComponentLike
        // This list is the new list of arguments that will be put into the translatable component
        List<ComponentLike> newTranslationArguments = new ArrayList<>(translationArguments.stream().map(
                arg -> (ComponentLike) arg
        ).toList());

        // Loop over all online players and try to find their name in the arguments
        // If a name is found, replace the translation argument with an argument styled by the team's color
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Team team = game.getTeamManager().getTeamByPlayer(onlinePlayer);
            if (team != null) {
                TranslationArgument playerTranslationArgument = getTranslationArgumentForPlayer(
                        translationArguments,
                        onlinePlayer
                );
                if (playerTranslationArgument != null) {
                    replaceTranslationArgumentWithColored(
                            newTranslationArguments,
                            playerTranslationArgument,
                            team.getColor()
                    );
                }
            }
        }

        // Set the arguments on the translatable component to the new arguments list
        TranslatableComponent styledTranslatableComponent = translatableComponent.arguments(newTranslationArguments);

        // Lastly, set the death message to this new translatable component that has our styled components
        e.deathMessage(styledTranslatableComponent);
    }

    /**
     * Get the translation argument that matches the given player in the given list of arguments.
     * @param arguments The list of translation arguments.
     * @param player The player whose name should match the name in the translation argument.
     * @return The translation argument that matches the player's name or null, if no such argument can be found.
     */
    private @Nullable TranslationArgument getTranslationArgumentForPlayer(
            List<TranslationArgument> arguments,
            Player player
    ) {
        for (TranslationArgument translationArgument : arguments) {
            if (translationArgument.value() instanceof TextComponent textComponent) {
                if (textComponent.content().equals(player.getName())) {
                    return translationArgument;
                }
            }
        }

        return null;
    }

    /**
     * Replace the given translation argument present in the given list with a variant that is colored with the given
     * color. The given list of ComponentLike will be updated such that the given argument is replaced with the styled
     * argument.
     * @param arguments The list of ComponentLike arguments in which the argument should be updated with the
     *                  replacement.
     * @param argument The argument that should be replaced with a colored variant. This argument should be present in
     *                 the list, or it will not be updated.
     * @param color The color that the argument should be styled with.
     */
    private void replaceTranslationArgumentWithColored(
            List<ComponentLike> arguments,
            TranslationArgument argument,
            TextColor color
    ) {
        // First get the text component from the translation argument and color it
        TextComponent argumentValueComponent = (TextComponent) argument.value();
        Component styledArgumentValue = argumentValueComponent.style(argumentValueComponent.style().color(color));

        for (int i = 0; i < arguments.size(); i++) {
            if (arguments.get(i).equals(argument)) {
                arguments.set(i, styledArgumentValue);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (game.getState().equals(Game.State.IN_GAME)) {
            Player player = e.getPlayer();
            Team team = game.getTeamManager().getTeamByPlayer(player);
            if (team == null || team.isSpectatorTeam()) {
                return;
            }

            PlayerTeam playerTeam = (PlayerTeam) team;

            player.addPotionEffect(PotionEffects.RESISTANCE);

            if (player.getRespawnLocation() == null) {
                Game.getLogger().info("Player does not have a respawn location, spawning at team spawn");
                e.setRespawnLocation(playerTeam.getSpawnLocation());
            }

            player.getInventory().addItem(
                    game.getBingoCardItemFactory().create(game.getBingoCard(), playerTeam)
            );
            player.sendMessage(Component
                    .text("You have been given a new bingo card")
            );
        }
    }

}
