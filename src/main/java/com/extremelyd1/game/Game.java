package com.extremelyd1.game;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.item.BingoItemMaterials;
import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.command.*;
import com.extremelyd1.config.Config;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.game.team.TeamManager;
import com.extremelyd1.game.timer.GameTimer;
import com.extremelyd1.game.winCondition.WinConditionChecker;
import com.extremelyd1.game.winCondition.WinReason;
import com.extremelyd1.gameboard.GameBoardManager;
import com.extremelyd1.listener.*;
import com.extremelyd1.main.Bingo;
import com.extremelyd1.potion.PotionEffects;
import com.extremelyd1.sound.SoundManager;
import com.extremelyd1.title.TitleManager;
import com.extremelyd1.util.TimeUtil;
import com.extremelyd1.util.ItemUtil;
import com.extremelyd1.util.LocationUtil;
import com.extremelyd1.util.StringUtil;
import com.extremelyd1.world.ChunkLoader;
import com.extremelyd1.world.WorldManager;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Game {

    /**
     * The base value of the spread radius
     */
    private static final float BASE_RADIUS = 200;
    /**
     * The increase in spread radius for each team
     */
    private static final float RADIUS_TEAM_INCREASE = 100;

    /**
     * The prefix string
     */
    public static final String PREFIX = ChatColor.BOLD.toString() + ChatColor.BLUE + "BINGO " + ChatColor.RESET;
    /**
     * The divider string
     */
    private static final String DIVIDER = PREFIX + ChatColor.STRIKETHROUGH
            + "                                                                        ";

    /**
     * The plugin instance
     */
    private final JavaPlugin plugin;

    /**
     * The logger instance to log messages to
     */
    private static Logger logger;

    /**
     * The current state of the game
     */
    private State state;

    /**
     * The config instance
     */
    private final Config config;

    /**
     * The game board manager instance
     */
    private GameBoardManager gameBoardManager;
    /**
     * The team manager instance
     */
    private final TeamManager teamManager;

    /**
     * The bingo card item factory instance
     */
    private final BingoCardItemFactory bingoCardItemFactory;
    /**
     * The bingo item materials store instance
     */
    private final BingoItemMaterials bingoItemMaterials;
    /**
     * The win condition checker instance
     */
    private final WinConditionChecker winConditionChecker;

    /**
     * The sound manager instance
     */
    private final SoundManager soundManager;
    /**
     * The title manager instance
     */
    private final TitleManager titleManager;
    /**
     * The world manager instance
     */
    private final WorldManager worldManager;

    /**
     * Whether maintenance mode is enabled
     */
    private boolean maintenance = false;
    /**
     * Whether PvP is enabled
     */
    private boolean pvpEnabled = false;

    /**
     * The current game timer
     */
    private GameTimer gameTimer;

    public Game(Bingo bingo) {
        Game.logger = bingo.getLogger();

        this.plugin = bingo;
        state = State.PRE_GAME;

        config = new Config(bingo);

        worldManager = new WorldManager(this);

        teamManager = new TeamManager(this);

        bingoCardItemFactory = new BingoCardItemFactory(this);
        bingoItemMaterials = new BingoItemMaterials(this);
        bingoItemMaterials.loadMaterials(getDataFolder());

        winConditionChecker = new WinConditionChecker(config);

        soundManager = new SoundManager();
        titleManager = new TitleManager();

        registerListeners(bingo);
        registerCommands(bingo);
    }

    /**
     * Register all event listeners
     * @param plugin The plugin instance to register the listeners to
     */
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

        // Register events for correctly handling nether portal with offset borders
        if (config.isBorderEnabled()) {
            Bukkit.getPluginManager().registerEvents(new WorldListener(this), plugin);
        }
    }

    /**
     * Register all commands
     * @param plugin The plugin instance to register the commands to
     */
    private void registerCommands(JavaPlugin plugin) {
        final Game game = this;
        final Map<String, CommandExecutor> executors = new HashMap<String, CommandExecutor>() {{
            put("team", new TeamCommand(game));
            put("start", new StartCommand(game));
            put("end", new EndCommand(game));
            put("bingo", new BingoCommand(game));
            put("card", new CardCommand(game));
            put("pvp", new PvpCommand(game));
            put("maintenance", new MaintenanceCommand(game));
            put("wincondition", new WinConditionCommand(game));
            put("reroll", new RerollCommand(game));
            put("itemdistribution", new ItemDistributionCommand(game));
            put("timer", new TimerCommand(game));
            put("coordinates", new CoordinatesCommand(game));
            put("all", new AllCommand(game));
        }};

        for (String cmdName : executors.keySet()) {
            PluginCommand command = plugin.getCommand(cmdName);
            if (command != null) {
                command.setExecutor(executors.get(cmdName));
            } else {
                throw new IllegalStateException("Command " + cmdName + " could not be registered");
            }
        }
    }

    /**
     * Called when the world manager has successfully loaded all worlds
     */
    public void onWorldsLoaded() {
        gameBoardManager = new GameBoardManager(this);
    }

    /**
     * Starts the game
     * @param player The player that started the game, or null if no player started the game
     */
    public void start(Player player) {
        // Sanity checks
        if (teamManager.getNumTeams() == 0) {
            getLogger().warning("No teams have been selected, cannot start game");

            if (player != null) {
                player.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "No teams have been selected, cannot start game"
                );
            }

            return;
        }

        // Gather locations to spread teams
        List<Location> locations = LocationUtil.getRandomCircleLocations(
                worldManager.getSpawnLocation(),
                teamManager.getNumTeams(),
                BASE_RADIUS + RADIUS_TEAM_INCREASE * teamManager.getNumTeams()
        );

        // Create chunk loader,
        // and do rest of start logic once chunks are loaded
        new ChunkLoader(
                plugin,
                worldManager,
                locations,
                () -> {
                    this.state = State.IN_GAME;

                    // Create random bingo card
                    BingoCard bingoCard = new BingoCard(bingoItemMaterials.pickMaterials());

                    for (Team team : teamManager.getTeams()) {
                        team.setBingoCard(bingoCard.copy());
                    }

                    int index = 0;
                    for (Team team : teamManager.getTeams()) {
                        Location location = locations.get(index++);

                        for (Player teamPlayer : team.getPlayers()) {
                            // Give player resistance 5 before teleporting to prevent fall damage
                            teamPlayer.addPotionEffect(PotionEffects.RESISTANCE);

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

                            titleManager.sendStartTitle();
                        }
                    }

                    // Prepare world for game start
                    worldManager.onGameStart();

                    // Enable scoreboards
                    gameBoardManager.createIngameBoards(teamManager.getTeams());
                    gameBoardManager.broadcast();

                    // Broadcast start message
                    Bukkit.broadcastMessage(
                            DIVIDER + "\n"
                                    + PREFIX + "                           Game has started!\n"
                                    + DIVIDER
                    );

                    // Send sounds
                    soundManager.broadcastStart();

                    if (config.isTimerEnabled()) {
                        // Start timer
                        gameTimer = new GameTimer(
                                plugin,
                                1,
                                config.getTimerLength(),
                                timeLeft -> {
                                    gameBoardManager.onTimeUpdate(timeLeft);

                                    if (timeLeft <= 0) {
                                        WinReason winReason = winConditionChecker.decideWinner(teamManager.getTeams());
                                        end(winReason);

                                        return true;
                                    } else {
                                        TimeUtil.broadcastTimeLeft(timeLeft);
                                    }

                                    return false;
                                }
                        );
                        gameTimer.start();
                    }
                }
        ).start();
    }

    /**
     * Ends the game with a win reason
     * @param winReason The reason for the game to end
     */
    public void end(WinReason winReason) {
        String message = DIVIDER + "\n" + PREFIX;

        switch (winReason.getReason()) {
            case COMPLETE:
                Team team = winReason.getTeam();
                message += "                     " + team.getColor() + team.getName()
                        + ChatColor.WHITE + " team "
                        + "has gotten bingo!";
                break;
            case RANDOM_TIE:
                // Don't do anything with ties yet
                message += "                      Game has ended in a tie!";
                break;
            case NO_WINNER:
            default:
                message += "                            Game has ended!";
                break;
        }

        message += "\n" + DIVIDER;

        Bukkit.broadcastMessage(message);

        titleManager.sendEndTitle(winReason);

        this.state = State.POST_GAME;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setGameMode(GameMode.CREATIVE);
        }

        soundManager.broadcastEnd();

        if (config.isTimerEnabled()) {
            gameTimer.cancel();
        }
    }

    /**
     * Reroll the bingo card
     */
    public void rerollCard() {
        // Create random bingo card
        BingoCard bingoCard = new BingoCard(bingoItemMaterials.pickMaterials());

        for (Team team : teamManager.getTeams()) {
            team.setBingoCard(bingoCard.copy());

            // Update the bingo card of all players in the team
            ItemUtil.updateBingoCard(team, bingoCardItemFactory);

            gameBoardManager.onItemCollected(team);
        }
    }

    /**
     * Called if the pregame state is updated
     * Updates the game board scoreboard
     */
    public void onPregameUpdate() {
        gameBoardManager.onPregameUpdate();
    }

    /**
     * When a material is collected by a player
     * Updates the bingo card of the player's team and ends the game if a card is completed
     * @param player The player that has collected the material
     * @param material The material that is collected
     */
    public void onMaterialCollected(Player player, Material material) {
        Team playerTeam = teamManager.getTeamByPlayer(player);
        if (playerTeam == null) {
            getLogger().warning("Material collected by player without team");
            return;
        }

        BingoCard bingoCard = playerTeam.getBingoCard();

        if (bingoCard.containsItem(material)
                && !bingoCard.getItemByMaterial(material).isCollected()) {
            bingoCard.addItemCollected(material);

            gameBoardManager.onItemCollected(playerTeam);

            // Update the bingo card of all players in the team
            ItemUtil.updateBingoCard(playerTeam, bingoCardItemFactory);

            Bukkit.broadcastMessage(
                    PREFIX +
                    playerTeam.getColor() + playerTeam.getName()
                            + ChatColor.WHITE + " team has obtained "
                            + ChatColor.AQUA + StringUtil.formatMaterialName(material)
            );

            // Check whether win condition has been met
            if (winConditionChecker.hasBingo(bingoCard)) {
                end(new WinReason(playerTeam, WinReason.Reason.COMPLETE));
            } else {
                soundManager.broadcastItemCollected(playerTeam);
            }
        }
    }

    public State getState() {
        return state;
    }

    public Config getConfig() {
        return config;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public BingoCardItemFactory getBingoCardItemFactory() {
        return bingoCardItemFactory;
    }

    public WinConditionChecker getWinConditionChecker() {
        return winConditionChecker;
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

    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public static Logger getLogger() {
        return logger;
    }

    public enum State {
        PRE_GAME("Pre-game"),
        IN_GAME("In-game"),
        POST_GAME("Post-game");

        private final String name;

        State(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
