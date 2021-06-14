package com.extremelyd1.game;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.item.BingoItemMaterials;
import com.extremelyd1.bingo.map.BingoCardItemFactory;
import com.extremelyd1.command.*;
import com.extremelyd1.config.Config;
import com.extremelyd1.game.chat.ChatChannelController;
import com.extremelyd1.game.team.PlayerTeam;
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
import com.extremelyd1.util.*;
import com.extremelyd1.world.WorldManager;
import com.extremelyd1.world.spawn.SpawnLoader;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class Game {

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
    private final GameBoardManager gameBoardManager;
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
     * Recipe util class instance
     */
    private final RecipeUtil recipeUtil;

    /**
     * Whether maintenance mode is enabled
     */
    private boolean maintenance = false;
    /**
     * Whether PvP is enabled
     */
    private boolean pvpEnabled = false;

    /**
     * The bingo card that is currently used
     */
    private BingoCard bingoCard;

    /**
     * The current game timer
     */
    private GameTimer gameTimer;

    /**
     * Chat Channel controller
     */
    private final ChatChannelController chatChannelController;

    public Game(Bingo bingo) throws IllegalArgumentException {
        Game.logger = bingo.getLogger();

        this.plugin = bingo;
        state = State.PRE_GAME;

        config = new Config(bingo);

        worldManager = new WorldManager(this);

        gameBoardManager = new GameBoardManager(this);
        teamManager = new TeamManager(this);

        bingoCardItemFactory = new BingoCardItemFactory(this);
        bingoItemMaterials = new BingoItemMaterials(this);
        bingoItemMaterials.loadMaterials(getDataFolder());

        winConditionChecker = new WinConditionChecker(config);

        chatChannelController = new ChatChannelController();

        soundManager = new SoundManager();
        titleManager = new TitleManager();

        recipeUtil = new RecipeUtil();

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
            put("channel", new ChannelCommand(game));
            put("teamchat", new TeamChatCommand(game));
            put("join", new JoinCommand(game));
            put("tpt", new TeamTeleportCommand(game));

            if (config.isPregenerateWorlds()) {
                put("generate", new GenerateCommand(game));
            } else {
                put("generate", new DisabledCommand());
            }
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
     * Starts the game
     */
    public void start() {
        start(null);
    }

    /**
     * Starts the game
     * @param player The player that started the game, or null if no player started the game
     */
    public void start(Player player) {
        // Sanity checks
        if (teamManager.getNumActiveTeams() == 0) {
            getLogger().warning("No teams have been selected, cannot start game");

            if (player != null) {
                player.sendMessage(
                        ChatColor.DARK_RED + "Error: "
                                + ChatColor.WHITE + "No teams have been selected, cannot start game"
                );
            }

            return;
        }

        // Calculate radius of spawn circle based on whether a border is enabled
        int radius;
        if (config.isBorderEnabled()) {
            // A point on the circle is at most as far away from the border as from the center
            radius = Math.round(config.getOverworldBorderSize() / 4f);
        } else {
            // Base radius of 200, with an increase of 100 per team
            radius = 200 + 100 * teamManager.getNumActiveTeams();
        }

        // Gather locations to spread teams
        List<Location> locations = LocationUtil.getRandomCircleLocations(
                worldManager.getSpawnLocation(),
                teamManager.getNumActiveTeams(),
                radius
        );

        if (player != null) {
            player.sendMessage(
                    Game.PREFIX + "Preparing spawn locations for teams..."
            );
        }

        // Create chunk loader,
        // and do rest of start logic once chunks are loaded
        new SpawnLoader(
                this,
                worldManager,
                locations,
                () -> {
                    if (player != null) {
                        player.sendMessage(
                                Game.PREFIX + "Spawn locations found, starting game"
                        );
                    }

                    this.state = State.IN_GAME;

                    // Create random bingo card
                    bingoCard = new BingoCard(bingoItemMaterials.pickMaterials(), winConditionChecker.getCompletionsToLock());

                    int index = 0;
                    for (PlayerTeam team : teamManager.getActiveTeams()) {
                        // Get location from list and convert from block position to spawn position
                        Location location = locations.get(index++).add(0.5, 1, 0.5);

                        team.setSpawnLocation(location);

                        for (Player teamPlayer : team.getPlayers()) {
                            // Give player resistance 5 before teleporting to prevent fall damage
                            teamPlayer.addPotionEffect(PotionEffects.RESISTANCE);

                            teamPlayer.teleport(location);

                            // Just to be sure, reset player again
                            teamPlayer.getInventory().clear();
                            teamPlayer.setGameMode(GameMode.SURVIVAL);
                            teamPlayer.setHealth(20D);
                            teamPlayer.setFoodLevel(20);
                            teamPlayer.setSaturation(5);

                            // Give all players a bingo card
                            teamPlayer.getInventory().addItem(bingoCardItemFactory.create(
                                            bingoCard,
                                            team
                            ));

                            if (config.isGiveAllRecipes()) {
                                recipeUtil.discoverAllRecipes(teamPlayer);
                            }
                        }
                    }

                    for (Player spectatorPlayer : teamManager.getSpectatorTeam().getPlayers()) {
                        spectatorPlayer.setGameMode(GameMode.SPECTATOR);
                    }

                    titleManager.sendStartTitle();

                    // Prepare world for game start
                    worldManager.onGameStart();

                    // Enable scoreboards
                    gameBoardManager.createIngameBoards(teamManager.getActiveTeams());
                    gameBoardManager.createSpectatorBoard(teamManager.getSpectatorTeam());
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
                                        WinReason winReason = winConditionChecker.decideWinner(teamManager.getActiveTeams());
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
                PlayerTeam team = winReason.getTeam();
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

        // Check whether we need to show all maps to all players
        if (config.isShowAllMapsPostGame()) {
            // Create all bingo cards for all teams
            Map<PlayerTeam, ItemStack> bingoCardItemStacks = new HashMap<>();

            for (PlayerTeam team : teamManager.getActiveTeams()) {
                // Put the item stack with appropriate border color in map
                ItemStack bingoCardItemStack = bingoCardItemFactory.create(
                        bingoCard,
                        team,
                        ColorUtil.chatColorToInt(team.getColor())
                );

                // Change the item name to include team name and color
                ItemMeta meta = bingoCardItemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(team.getColor() + team.getName() + " Team");
                }
                bingoCardItemStack.setItemMeta(meta);

                bingoCardItemStacks.put(
                        team,
                        bingoCardItemStack
                );
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                // Set own bingo card in offhand if player has a team
                Team team = teamManager.getTeamByPlayer(onlinePlayer);
                if (team != null && !team.isSpectatorTeam()) {
                    onlinePlayer.getInventory().setItemInOffHand(
                            bingoCardItemStacks.get(team)
                    );
                }

                int inventoryIndex = 0;

                for (PlayerTeam otherTeam : bingoCardItemStacks.keySet()) {
                    // Skip own team
                    if (otherTeam.equals(team)) {
                        continue;
                    }

                    // Set bingo card to slot and increase inventory index
                    onlinePlayer.getInventory().setItem(
                            inventoryIndex++,
                            bingoCardItemStacks.get(otherTeam)
                    );
                }
            }
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
        bingoCard = new BingoCard(bingoItemMaterials.pickMaterials(), winConditionChecker.getCompletionsToLock());

        for (PlayerTeam team : teamManager.getActiveTeams()) {
            // Reset the number of collected items for this team
            team.resetNumCollected();

            // Update the bingo card of all players in the team
            ItemUtil.updateBingoCard(bingoCard, team, bingoCardItemFactory);

            gameBoardManager.onItemCollected(team);
        }
    }

    /**
     * Called if the pregame state is updated
     * Updates the game board scoreboard
     */
    public void onPregameUpdate() {
        gameBoardManager.onPregameUpdate(Bukkit.getOnlinePlayers().size());
    }

    /**
     * Called if the pregame state is updated
     * Updates the game board scoreboard
     * @param numOnlinePlayers The number of online players used to update
     */
    public void onPregameUpdate(int numOnlinePlayers) {
        gameBoardManager.onPregameUpdate(numOnlinePlayers);
    }

    /**
     * When a material is collected by a player
     * Updates the bingo card of the player's team and ends the game if a card is completed
     * @param player The player that has collected the material
     * @param material The material that is collected
     */
    public void onMaterialCollected(Player player, Material material) {
        Team team = teamManager.getTeamByPlayer(player);
        if (team == null || team.isSpectatorTeam()) {
            getLogger().warning("Material collected by player without team");
            return;
        }

        PlayerTeam collectorTeam = (PlayerTeam) team;

        int linesCompletedBefore = bingoCard.getNumLinesComplete(collectorTeam);

        if (bingoCard.checkMaterialCollection(material, collectorTeam)) {
            gameBoardManager.onItemCollected(collectorTeam);

            if (config.notifyOtherTeamCompletions()) {
                // Broadcast a message of this collection
                Bukkit.broadcastMessage(
                        PREFIX +
                                collectorTeam.getColor() + collectorTeam.getName()
                                + ChatColor.WHITE + " team has obtained "
                                + ChatColor.AQUA + StringUtil.formatMaterialName(material)
                );

                // Update the cards of all players in all teams
                for (PlayerTeam playerTeam : teamManager.getActiveTeams()) {
                    ItemUtil.updateBingoCard(bingoCard, playerTeam, bingoCardItemFactory);
                }
            } else {
                // Update only the bingo card of the players in the team that collected the item
                ItemUtil.updateBingoCard(bingoCard, collectorTeam, bingoCardItemFactory);
                // TODO: also might need to update other cards if lockout is enabled and this item is now locked
                // for other teams
            }

            config.getProgressController().onCollection(this, collectorTeam, linesCompletedBefore);

            // Get a list of current winners from the checker
            List<PlayerTeam> winners = winConditionChecker.getCurrentWinners(
                    bingoCard,
                    collectorTeam,
                    teamManager.getActiveTeams()
            );

            if (winners.isEmpty()) {
                // If the list is empty, the game is not finished yet
                soundManager.broadcastItemCollected(collectorTeam);
            } else if (winners.size() == 1) {
                // If there is a single winner, we can announce it
                end(new WinReason(winners.get(0), WinReason.Reason.COMPLETE));
            } else {
                // Otherwise, end the game with a random tie
                end(new WinReason(
                        winners.get(new Random().nextInt(winners.size())),
                        WinReason.Reason.RANDOM_TIE)
                );
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

    public ChatChannelController getChatChannelController() {
        return chatChannelController;
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

    public BingoCard getBingoCard() {
        return bingoCard;
    }

    public void togglePvp() {
        pvpEnabled = !pvpEnabled;
    }

    public JavaPlugin getPlugin() {
        return plugin;
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
