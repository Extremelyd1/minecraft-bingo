package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.potion.PotionEffects;
import com.extremelyd1.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class DeathListener implements Listener {

    /**
     * The game instance
     */
    private final Game game;

    public DeathListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        final Player p = e.getEntity();
        Location loc = p.getLocation();
        game.getBackloc().put(p, loc);

        for (int i = 0; i < e.getDrops().size(); i++) {
            ItemStack itemStack = e.getDrops().get(i);
            if (itemStack.getItemMeta() != null
                    && itemStack.getItemMeta().getDisplayName().contains("Bingo Card")) {
                e.getDrops().remove(i);
                i--;
            }
        }

        e.setDeathMessage(StringUtil.replaceNamesWithTeamColors(
                e.getDeathMessage(),
                Bukkit.getOnlinePlayers(),
                game.getTeamManager()
        ));
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

            if (player.getBedSpawnLocation() == null) {
                e.setRespawnLocation(playerTeam.getSpawnLocation());
            }

            player.getInventory().addItem(
                    game.getBingoCardItemFactory().create(game.getBingoCard(), playerTeam)
            );
            player.sendMessage(
                    Game.PREFIX + "You have been given a new bingo card"
            );

            player.sendMessage(
                    ChatColor.RED+"Use the " + ChatColor.GOLD + "/back " + ChatColor.RED +" command to return to your death point."
            );
        }
    }

}
