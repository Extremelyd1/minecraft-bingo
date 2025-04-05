package com.extremelyd1.listener;

import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.potion.PotionEffects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

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

        Player player = e.getPlayer();
        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null) {
            Game.getLogger().warning("onPlayerDeath player has no team");
            return;
        }

        Component deathMessageComponent = e.deathMessage();
        if (deathMessageComponent == null) {
            Game.getLogger().warning("onPlayerDeath has no death message");
            return;
        }

        if (!(deathMessageComponent instanceof TranslatableComponent translatableComponent)) {
            Game.getLogger().warning("onPlayerDeath has no translatable component in death message");
            return;
        }

        TranslationArgument translationArgument = translatableComponent.arguments().getFirst();
        if (translationArgument == null) {
            Game.getLogger().warning("onPlayerDeath has no translation argument in death message");
            return;
        }

        TextComponent argumentValueComponent = (TextComponent) translationArgument.value();

        // Style the text component with the player's team color
        Component styledArgumentComponent = argumentValueComponent.style(argumentValueComponent.style().color(team.getColor()));

        // Set the arguments on the translatable component to the argument we just changed
        TranslatableComponent styledTranslatableComponent = translatableComponent.arguments(styledArgumentComponent);

        // Lastly, set the death message to this new translatable component that has our styled component
        e.deathMessage(styledTranslatableComponent);
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
