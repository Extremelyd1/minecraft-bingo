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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    private boolean pvpDisabled = true;

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
        teamManager = new TeamManager();

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
        Bukkit.getPluginManager().registerEvents(this.worldManager, plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinLeaveListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new ItemListener(this, bingoCardItemFactory), plugin);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new MotdListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this, bingoCardItemFactory), plugin);
        Bukkit.getPluginManager().registerEvents(new InteractListener(this, bingoCardItemFactory), plugin);
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
        final Map<String, CommandExecutor> executors = new HashMap<>() {{
            put("team", new TeamCommand(game));
            put("start", new StartCommand(game));
            put("end", new EndCommand(game));
            put("bingo", new BingoCommand(game));
            put("card", new CardCommand(game, bingoCardItemFactory));
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

            if (config.isPreGenerateWorlds()) {
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
                player.sendMessage(ChatUtil.errorPrefix()
                        .append(Component
                                .text("No teams have been selected, cannot start game")
                                .color(NamedTextColor.WHITE)
                        )
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
            player.sendMessage(ChatUtil.waitPrefix()
                    .append(Component
                            .text("Preparing spawn locations for teams...")
                            .color(NamedTextColor.WHITE)
                    )
            );
        }

        // Create the spawn loader to determine and chunk-load spawn locations
        new SpawnLoader(this, worldManager, locations, spawns -> onSpawnsLoaded(player, spawns)).start();
    }

    /**
     * Callback method for when spawns are loaded.
     * @param player The player that issued the game start, which resulted in loading spawns.
     * @param locations The locations of the spawns.
     */
    private void onSpawnsLoaded(Player player, List<Location> locations) {
        if (player != null) {
            player.sendMessage(ChatUtil.successPrefix()
                    .append(Component
                            .text("Spawn locations found, starting game")
                            .color(NamedTextColor.WHITE)
                    )
            );
        }

        this.state = State.IN_GAME;

        // Clear previously created bingo cards
        bingoCardItemFactory.clearCreatedBingoCards();

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

                int freezeTimeOnStart = config.getFreezeTimeOnStart();
                if (freezeTimeOnStart > 0) {
                    teamPlayer.addPotionEffect(PotionEffects.BLINDNESS.withDuration(freezeTimeOnStart * 20));
                    teamPlayer.addPotionEffect(PotionEffects.DARKNESS.withDuration(freezeTimeOnStart * 20));
                    teamPlayer.addPotionEffect(PotionEffects.SLOWNESS.withDuration(freezeTimeOnStart * 20));
                    teamPlayer.addPotionEffect(PotionEffects.JUMP_BOOST.withDuration(freezeTimeOnStart * 20));
                }

                teamPlayer.teleport(location);

                // Just to be sure, reset player again
                teamPlayer.getInventory().clear();
                teamPlayer.setGameMode(GameMode.SURVIVAL);
                teamPlayer.setHealth(20D);
                teamPlayer.setFoodLevel(20);
                teamPlayer.setSaturation(5);
                teamPlayer.setRemainingAir(teamPlayer.getMaximumAir());

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
        gameBoardManager.createInGameBoards(teamManager.getActiveTeams());
        gameBoardManager.createSpectatorBoard(teamManager.getSpectatorTeam());
        gameBoardManager.broadcast();

        // Broadcast start message
        Bukkit.broadcast(
                ChatUtil.divider()
                        .append(Component.newline())
                        .append(Component
                                .text("                                 Game has started!")
                                .color(NamedTextColor.WHITE)
                        )
                        .append(Component.newline())
                        .append(ChatUtil.divider())
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
                            WinReason winReason = winConditionChecker.decideWinner(teamManager.getActiveTeams(), bingoCard);
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

    /**
     * Ends the game with a win reason
     * @param winReason The reason for the game to end
     */
    public void end(WinReason winReason) {
        Component message = ChatUtil.divider().append(Component.newline());

        message = switch (winReason.getReason()) {
            case COMPLETE -> {
                PlayerTeam team = winReason.getTeam();
                yield message.append(Component
                        .text("                     " + team.getName())
                        .color(team.getColor())
                ).append(Component
                        .text(" team has gotten bingo!")
                        .color(NamedTextColor.WHITE)
                );
            }
            case RANDOM_TIE -> message.append(Component
                            .text("                      Game has ended in a tie!")
                            .color(NamedTextColor.WHITE)
                    );
            default -> message.append(Component
                    .text("                            Game has ended!")
                    .color(NamedTextColor.WHITE)
            );
        };

        message = message.append(Component.newline()).append(ChatUtil.divider());

        Bukkit.broadcast(message);

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
                        ColorUtil.textColorToInt(team.getColor())
                );

                // Change the item name to include team name and color
                ItemMeta meta = bingoCardItemStack.getItemMeta();
                if (meta != null) {
                    meta.customName(Component.text(team.getName() + " Team").color(team.getColor()));
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
                    //noinspection SuspiciousMethodCalls
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
                Bukkit.broadcast(Component
                        .text(collectorTeam.getName())
                        .color(collectorTeam.getColor())
                        .append(Component
                                .text(" team has obtained ")
                                .color(NamedTextColor.WHITE))
                        .append(Component
                                .text(ChatUtil.formatMaterialName(material))
                                .color(NamedTextColor.AQUA)
                        )
                );
            }

            if (config.notifyOtherTeamCompletions() ||
                    (winConditionChecker.getCompletionsToLock() > 0 && bingoCard.isItemLocked(material))
            ) {
                // Update the cards of all players in all teams
                for (PlayerTeam playerTeam : teamManager.getActiveTeams()) {
                    ItemUtil.updateBingoCard(bingoCard, playerTeam, bingoCardItemFactory);
                }
            } else {
                // Update only the bingo card of the players in the team that collected the item
                ItemUtil.updateBingoCard(bingoCard, collectorTeam, bingoCardItemFactory);
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
                end(new WinReason(winners.getFirst(), WinReason.Reason.COMPLETE));
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

    public GameBoardManager getGameBoardManager() {
        return gameBoardManager;
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

    public boolean isPvpDisabled() {
        return pvpDisabled;
    }

    public BingoCard getBingoCard() {
        return bingoCard;
    }

    public void togglePvp() {
        pvpDisabled = !pvpDisabled;
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
