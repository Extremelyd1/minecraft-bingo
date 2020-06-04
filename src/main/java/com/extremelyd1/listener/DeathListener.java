package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.potion.PotionEffects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DeathListener implements Listener {

    private final Game game;

    public DeathListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        for (int i = 0; i < e.getDrops().size(); i++) {
            ItemStack itemStack = e.getDrops().get(i);
            if (itemStack.getItemMeta() != null
                    && itemStack.getItemMeta().getDisplayName().contains("Bingo Card")) {
                e.getDrops().remove(i);
                i--;
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (game.getState().equals(Game.State.IN_GAME)) {
            Player player = e.getPlayer();
            Team team = game.getTeamManager().getTeamByPlayer(player);
            if (team == null) {
                return;
            }

            player.addPotionEffect(PotionEffects.RESISTANCE);

            player.getInventory().addItem(
                    game.getBingoCardItemFactory().create(team.getBingoCard())
            );
            player.sendMessage(
                    Game.PREFIX + "You have been given a new bingo card"
            );
        }
    }

}
