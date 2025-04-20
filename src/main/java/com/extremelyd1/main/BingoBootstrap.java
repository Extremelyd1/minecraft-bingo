package com.extremelyd1.main;

import com.extremelyd1.util.FileUtil;
import io.papermc.paper.datapack.DatapackRegistrar;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class BingoBootstrap implements PluginBootstrap {
    /**
     * The name of the directory that stores datapacks either on disk or in our embedded resources.
     */
    private static final String DATAPACK_DIR_NAME = "datapacks";
    /**
     * The name of the directory for the biome size datapack.
     */
    private static final String BIOME_SIZE_DIR_NAME = "biome-size";

    /**
     * The name of the directory that stores item data either on disk or in our embedded resources.
     */
    private static final String ITEM_DATA_DIR_NAME = "item_data";

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        bootstrapDatapack(context);
        bootstrapItemData(context);
    }

    /**
     * Bootstrap the biome size datapack by manually loading the configuration file, checking its values, unpacking
     * the datapack directory from embedded resources, and allowing it to be discovered by the server.
     * @param context The bootstrap context for logging and datapack discovery.
     */
    private void bootstrapDatapack(BootstrapContext context) {
        // First check for the existence of the plugin's configuration file
        File configFile = new File(context.getDataDirectory().toFile(), "config.yml");
        if (!configFile.exists()) {
            // If it doesn't exist yet, it will be unpacked from the jar with default values, meaning that loading
            // the datapack is unnecessary, because the default value for the biome-size config key will be normal
            context.getLogger().info("Config file does not exist yet, not copying resource datapack");
            return;
        }

        // Manually load the configuration file, given that we do not have access to it by other means
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Check whether the border is enabled in our config. If not, we do not make use of the datapack
        boolean worldBorderEnabled = config.getBoolean("border.enable");
        if (!worldBorderEnabled) {
            context.getLogger().info("World border is not enabled, not copying resource datapack");
            return;
        }

        // Get the config value for biome size
        String biomeSize = config.getString("border.generation.biome-size");

        // Either the biome-size key is not defined, or it is not set to 'small', so we do not provide a datapack
        if (biomeSize == null || !biomeSize.equals("small")) {
            context.getLogger().info("Biome size config key does not exist or is not set to 'small', not copying resource datapack");
            return;
        }

        File datapackDir = unpackResourceToDataDir(context, DATAPACK_DIR_NAME, "biome size datapack");
        if (datapackDir == null) {
            return;
        }

        // Finally use the lifecycle event manager to discover our datapack on disk at the specified directory
        final LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, event -> {
            DatapackRegistrar registrar = event.registrar();
            try {
                registrar.discoverPack(new File(datapackDir, BIOME_SIZE_DIR_NAME).toPath(), "world-gen");
            } catch (IOException e) {
                context.getLogger().error("Could not discover biome size resource datapack:\n{}", e.toString());
            }
        });
    }

    /**
     * Bootstrap the item data by unpacking it from embedded resources if it doesn't exist yet.
     * @param context The bootstrap context for logging.
     */
    private void bootstrapItemData(BootstrapContext context) {
        unpackResourceToDataDir(context, ITEM_DATA_DIR_NAME, "item data");
    }

    /**
     * Unpack an embedded resource to the plugin's data directory.
     * @param context The bootstrap context for logging.
     * @param dirName The name of the directory in the embedded resource and the resulting directory name on disk.
     * @param resourceName The name of the resource for logging purposes.
     * @return The resulting directory file if unpacking succeeded, otherwise null.
     */
    private @Nullable File unpackResourceToDataDir(BootstrapContext context, String dirName, String resourceName) {
        // Get the data directory of our plugin
        File dataDir = context.getDataDirectory().toFile();
        if (!dataDir.exists()) {
            context.getLogger().error(
                    "Could not unpack {}, because data directory does not exist", resourceName
            );
            return null;
        }

        // Get the directory that stores the unpacked resource and check if it exists
        File resourceDir = new File(dataDir, dirName);
        if (!resourceDir.exists()) {
            // If it doesn't exist yet, we try to copy the resource directory from our embedded resources
            try {
                FileUtil.copyResourceDirectory(dirName, resourceDir.toPath());
            } catch (URISyntaxException | IOException e) {
                context.getLogger().error("Could not copy resource {} to the data directory:\n{}",
                        resourceName,
                        e.toString()
                );
                return null;
            }

            context.getLogger().info("Successfully unpacked resource {}", resourceName);
        } else {
            context.getLogger().info("Resource {} already exists in data directory", resourceName);
        }

        return resourceDir;
    }
}
