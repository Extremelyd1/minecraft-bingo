package com.extremelyd1.game;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.BingoItemMaterials;
import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.command.*;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.team.TeamManager;
import com.extremelyd1.gameboard.GameBoardManager;
import com.extremelyd1.listener.*;
import com.extremelyd1.main.Bingo;
import com.extremelyd1.sound.SoundManager;
import com.extremelyd1.util.ItemUtil;
import com.extremelyd1.util.LocationUtil;
import com.extremelyd1.util.StringUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import sun.applet.Main;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class Game {

    // The base value of the spread radius
    private static final float BASE_RADIUS = 200;
    // The increase in spread radius for each team
    private static final float RADIUS_TEAM_INCREASE = 50;

    public static final String PREFIX = ChatColor.BOLD.toString() + ChatColor.BLUE + "BINGO " + ChatColor.RESET;

    private final JavaPlugin plugin;

    private final Logger logger;

    private State state;

    private final World world;

    private final GameBoardManager gameBoardManager;
    private final TeamManager teamManager;

    private final BingoCardItemFactory bingoCardItemFactory;
    private final BingoItemMaterials bingoItemMaterials;

    private final SoundManager soundManager;

    private boolean maintenance = false;
    private boolean pvpEnabled = false;
    private boolean fullCard = false;

    public Game(Bingo bingo) {
        this.plugin = bingo;
        this.logger = bingo.getLogger();
        state = State.PRE_GAME;

        if (Bukkit.getWorlds().size() == 0) {
            throw new IllegalStateException("There are no worlds loaded");
        }
        world = Bukkit.getWorlds().get(0);
        world.setAutoSave(false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        gameBoardManager = new GameBoardManager(this);
        teamManager = new TeamManager(this);

        bingoCardItemFactory = new BingoCardItemFactory(this, world);
        bingoItemMaterials = new BingoItemMaterials();
        bingoItemMaterials.loadMaterials(getDataFolder());

        soundManager = new SoundManager();

        registerListeners(bingo);
        registerCommands(bingo);
    }

    private void registerListeners(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinLeaveListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new ItemListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new MotdListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new InteractListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new FoodListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new MoveListener(this), plugin);
    }

    private void registerCommands(JavaPlugin plugin) {
        plugin.getCommand("team").setExecutor(new TeamCommand(this));
        plugin.getCommand("start").setExecutor(new StartCommand(this));
        plugin.getCommand("end").setExecutor(new EndCommand(this));
        plugin.getCommand("bingo").setExecutor(new BingoCommand(this));
        plugin.getCommand("card").setExecutor(new CardCommand(this));
        plugin.getCommand("pvp").setExecutor(new PvpCommand(this));
        plugin.getCommand("maintenance").setExecutor(new MaintenanceCommand(this));
        plugin.getCommand("fullcard").setExecutor(new FullCardCommand(this));
        plugin.getCommand("reroll").setExecutor(new RerollCommand(this));
    }

    public void start(Player player) {
        // Sanity checks
        if (teamManager.getTeams().size() == 0) {
            getLogger().warning("No teams have been selected, cannot start game");

            if (player != null) {
                player.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "No teams have been selected, cannot start game"
                );
            }

            return;
        }

        // Create random bingo card
        BingoCard bingoCard = new BingoCard(bingoItemMaterials.getMaterials());

        for (Team team : teamManager.getTeams()) {
            team.setBingoCard(bingoCard.copy());
        }

        this.state = State.IN_GAME;

        Bukkit.broadcastMessage(
                PREFIX + "----------------------"
        );
        Bukkit.broadcastMessage(
                PREFIX + "Game has been started!"
        );
        Bukkit.broadcastMessage(
                PREFIX + "----------------------"
        );

        // Spread out players
        List<Location> locations = LocationUtil.getRandomCircleLocations(
                world.getSpawnLocation(),
                teamManager.getTeams().size(),
                BASE_RADIUS + RADIUS_TEAM_INCREASE * teamManager.getTeams().size()
        );

        for (int i = 0; i < teamManager.getTeams().size(); i++) {
            Team team = teamManager.getTeams().get(i);
            Location location = locations.get(i);

            PotionEffect resistanceEffect = new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE,
                    5,
                    5,
                    false,
                    false
            );

            for (Player teamPlayer : team.getPlayers()) {
                // Give player resistance 5 before teleporting to prevent fall damage
                teamPlayer.addPotionEffect(resistanceEffect);

                teamPlayer.teleport(location);
                teamPlayer.setBedSpawnLocation(location, true);

                // Just to be sure, reset player again
                teamPlayer.getInventory().clear();
                teamPlayer.setGameMode(GameMode.SURVIVAL);
                teamPlayer.setHealth(20D);
                teamPlayer.setFoodLevel(20);
                teamPlayer.setSaturation(5);

                // Give all players a bingo card
                teamPlayer.getInventory().addItem(
                        bingoCardItemFactory.create(team.getBingoCard())
                );
            }
        }

        // Enable game rules
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);

        // Enable scoreboards
        gameBoardManager.createIngameBoards(teamManager.getTeams());
        gameBoardManager.broadcast();

        // Send sounds
        soundManager.broadcastStart();
    }

    public void end() {
        this.state = State.POST_GAME;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setGameMode(GameMode.CREATIVE);
        }

        soundManager.broadcastEnd();
    }

    public void rerollCard() {
        // Create random bingo card
        BingoCard bingoCard = new BingoCard(bingoItemMaterials.getMaterials());

        for (Team team : teamManager.getTeams()) {
            team.setBingoCard(bingoCard.copy());

            // Update the bingo card of all players in the team
            ItemUtil.updateBingoCard(team, bingoCardItemFactory);

            gameBoardManager.onItemCollected(team);
        }
    }

    public void onPlayerJoinLeave() {
        gameBoardManager.onPlayerJoinLeave();
    }

    public void onMaterialCollected(Player player, Material material) {
        Team playerTeam = null;
        for (Team team : teamManager.getTeams()) {
            if (team.getPlayers().contains(player)) {
                playerTeam = team;
                break;
            }
        }

        if (playerTeam == null) {
            return;
        }

        if (playerTeam.getBingoCard().containsItem(material)
                && !playerTeam.getBingoCard().getItemByMaterial(material).isCollected()) {
            playerTeam.getBingoCard().addItemCollected(material);

            gameBoardManager.onItemCollected(playerTeam);

            // Update the bingo card of all players in the team
            ItemUtil.updateBingoCard(playerTeam, bingoCardItemFactory);

            Bukkit.broadcastMessage(
                    PREFIX +
                    playerTeam.getColor() + playerTeam.getName()
                            + ChatColor.WHITE + " team has obtained "
                            + ChatColor.AQUA + StringUtil.formatMaterialName(material)
            );

            // If using full card and card is completely filled OR
            // not using full card and card has bingo
            if ((fullCard && playerTeam.getBingoCard().isCardComplete())
                    || (!fullCard && playerTeam.getBingoCard().hasBingo())) {
                Bukkit.broadcastMessage(
                        PREFIX + "-------------------------"
                );
                Bukkit.broadcastMessage(
                        PREFIX
                                + playerTeam.getColor() + playerTeam.getName()
                                + ChatColor.WHITE + " team "
                                + "has gotten bingo!"
                );
                Bukkit.broadcastMessage(
                        PREFIX + "-------------------------"
                );

                end();
            } else {
                soundManager.broadcastItemCollected(playerTeam);
            }
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public State getState() {
        return state;
    }

    public World getWorld() {
        return world;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public BingoCardItemFactory getBingoCardItemFactory() {
        return bingoCardItemFactory;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void toggleMaintenance() {
        maintenance = !maintenance;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void togglePvp() {
        pvpEnabled = !pvpEnabled;
    }

    public boolean isFullCard() {
        return fullCard;
    }

    public void toggleFullCard() {
        fullCard = !fullCard;
    }

    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    public enum State {
        PRE_GAME,
        IN_GAME,
        POST_GAME
    }

}
