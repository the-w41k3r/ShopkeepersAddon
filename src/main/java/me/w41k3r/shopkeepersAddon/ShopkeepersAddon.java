package me.w41k3r.shopkeepersAddon;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static me.w41k3r.shopkeepersAddon.Commands.initCommands;
import static me.w41k3r.shopkeepersAddon.Events.initEvents;
import static me.w41k3r.shopkeepersAddon.gui.BaseGUIHandlers.getFillerItem;
import static me.w41k3r.shopkeepersAddon.gui.init.InitAdminPages.initPages;
import static me.w41k3r.shopkeepersAddon.gui.managers.FetchShopkeepersManager.fetchShopkeepers;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;

public final class ShopkeepersAddon extends JavaPlugin {

    private static final String VAULT_PLUGIN_NAME = "Vault";
    private static final String SKINS_RESTORER_PLUGIN_NAME = "SkinsRestorer";
    private static final String CONFIG_DEBUG_PATH = "debug";
    private static final String CONFIG_ECONOMY_ENABLED_PATH = "economy.enabled";
    private static final String CONFIG_PREFIX_PATH = "messages.prefix";
    private static final String CONFIG_BLACKLIST_PATH = "playerShops.itemBlacklist";
    private static final String BACKUP_FOLDER_NAME = "backups";
    private static final String CONFIG_VERSION_PATH = "config-version";
    private static final double CURRENT_CONFIG_VERSION = 1.0;

    public static FileConfiguration config;
    public static ShopkeepersAddon plugin;
    private static Logger logger;
    public static boolean debugMode;
    public static ItemStack adminFillerItem;
    public static ItemStack playerFillerItem;
    public static Economy Money;

    private static String prefix;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        initializePlugin();

        if (!validateDependencies()) {
            return;
        }

        setupEconomy();
        initializeComponents();
        loadConfiguration();

        logger.info(String.format("Plugin enabled successfully in %dms",
                System.currentTimeMillis() - startTime));
    }

    @Override
    public void onDisable() {
        cleanupResources();
        logger.info("Plugin disabled successfully");
    }

    private void initializePlugin() {
        plugin = this;
        logger = getLogger();
        saveDefaultConfig();
        config = plugin.getConfig();
        debugMode = config.getBoolean(CONFIG_DEBUG_PATH, false);
    }

    private boolean validateDependencies() {
        // Check for SkinsRestorer in offline mode
        if (!Bukkit.getOnlineMode() &&
                Bukkit.getPluginManager().getPlugin(SKINS_RESTORER_PLUGIN_NAME) == null) {
            logger.warning("Server is in offline mode but SkinsRestorer is not installed!");
            logger.warning("Player skins may not work correctly.");
        }

        return true;
    }

    private void setupEconomy() {
        if (!config.getBoolean(CONFIG_ECONOMY_ENABLED_PATH, false)) {
            debugLog("Economy is disabled in config");
            return;
        }

        setupVault();
    }

    private void initializeComponents() {
        try {
            initCommands();
            initEvents();

            logger.info("Fetching shopkeepers...");
            fetchShopkeepers();

            initPages();
        } catch (Exception e) {
            logger.severe("Failed to initialize plugin components: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public static void loadConfiguration() {
        try {
            // First, update the config if needed
            updateConfig();

            // Then load the configuration values
            adminFillerItem = getFillerItem("adminShop.filler");
            playerFillerItem = getFillerItem("playerShop.filler");

            blacklistedItems = config.getStringList(CONFIG_BLACKLIST_PATH);

            prefix = formatPrefix(config.getString(CONFIG_PREFIX_PATH,
                    "&7[&bShopkeepersAddon&7] "));

            lastUpdateTime = System.currentTimeMillis();

            debugLog("Configuration loaded successfully");
        } catch (Exception e) {
            logger.severe("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateConfig() {
        try {
            double currentVersion = config.getDouble(CONFIG_VERSION_PATH, 0.0);

            if (currentVersion < CURRENT_CONFIG_VERSION) {
                createConfigBackup();
                logger.info("Updating config from version " + currentVersion + " to " + CURRENT_CONFIG_VERSION);
                InputStream defaultConfigStream = plugin.getResource("config.yml");
                if (defaultConfigStream == null) {
                    logger.warning("Default config.yml not found inside the plugin jar!");
                    return;
                }
                FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultConfigStream)
                );

                boolean configChanged = addMissingKeysSimple(config, defaultConfig);

                config.set(CONFIG_VERSION_PATH, CURRENT_CONFIG_VERSION);

                if (configChanged) {
                    saveUpdatedConfig(config);   // Write changes to disk
                    plugin.reloadConfig();       // Reload from disk
                    config = plugin.getConfig(); // Refresh reference
                    logger.info("Config updated successfully to version " + CURRENT_CONFIG_VERSION);
                } else {
                    logger.info("Config was already up to date");
                }
            } else {
                debugLog("Config is up to date (version " + currentVersion + ")");
            }

        } catch (Exception e) {
            logger.severe("Error updating config: " + e.getMessage());
            e.printStackTrace();
        }
    }




    private static void createConfigBackup() {
        try {
            File dataFolder = plugin.getDataFolder();
            File backupsFolder = new File(dataFolder, BACKUP_FOLDER_NAME);

            if (!backupsFolder.exists() && !backupsFolder.mkdirs()) {
                logger.warning("Failed to create backups directory");
                return;
            }

            File currentConfig = new File(dataFolder, "config.yml");
            if (!currentConfig.exists()) {
                return; // No config to backup
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String backupFileName = "config_backup_v" +
                    config.getDouble(CONFIG_VERSION_PATH, 0.0) + "_" + timestamp + ".yml";
            File backupFile = new File(backupsFolder, backupFileName);

            Files.copy(currentConfig.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Config backup created: " + backupFileName);

        } catch (Exception e) {
            logger.warning("Failed to create config backup: " + e.getMessage());
        }
    }


    public static void validateConfig() {
        try {
            File defaultConfigFile = new File(plugin.getDataFolder(), "config.yml.default");
            if (!defaultConfigFile.exists()) {
                logger.warning("Cannot validate config: default config file not found");
                return;
            }

            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigFile);

            List<String> missingKeys = new ArrayList<>();
            for (String key : defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    missingKeys.add(key);
                }
            }

            if (!missingKeys.isEmpty()) {
                logger.warning("Config validation found " + missingKeys.size() + " missing keys:");
                for (String key : missingKeys) {
                    logger.warning(" - " + key);
                }
                logger.warning("Run /shopkeepersaddon reload to update the config automatically");
            } else {
                debugLog("Config validation passed - all keys are present");
            }

        } catch (Exception e) {
            logger.warning("Failed to validate config: " + e.getMessage());
        }
    }

    // FIX: Removed the problematic recursive method and only use the simple version
    private static boolean addMissingKeysSimple(FileConfiguration currentConfig, FileConfiguration defaultConfig) {
        boolean changed = false;


        for (String key : defaultConfig.getKeys(true)) {
            // Skip the version key - we handle that separately
            if (CONFIG_VERSION_PATH.equals(key)) {
                continue;
            }
            if (!currentConfig.contains(key)) {
                logger.info("Config key " + key + " not found in config.yml");
                Object defaultValue = defaultConfig.get(key);
                currentConfig.set(key, defaultValue);
                changed = true;
                logger.info("Added missing config key: " + key + " = " + defaultValue);
            }
            // Also check if the value types match (optional but recommended)
            else if (!currentConfig.isSet(key) ||
                    currentConfig.get(key) == null ||
                    !currentConfig.get(key).getClass().equals(defaultConfig.get(key).getClass())) {
                Object defaultValue = defaultConfig.get(key);
                currentConfig.set(key, defaultValue);
                changed = true;
                logger.info("Fixed config key type: " + key + " = " + defaultValue);
            }
        }

        return changed;
    }



    private void cleanupResources() {
        // Add any cleanup logic here if needed
        Money = null;
        plugin = null;
    }

    private void setupVault() {
        if (Bukkit.getPluginManager().getPlugin(VAULT_PLUGIN_NAME) == null) {
            logger.severe("Vault plugin not found! Please install Vault.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer()
                .getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            debugLog("Economy provider not set yet, listening for registration.");
            return;
        }

        Money = rsp.getProvider();
        debugLog("Economy provider set to: " + Money.getName());
    }

    private static String formatPrefix(String rawPrefix) {
        if (rawPrefix == null || rawPrefix.trim().isEmpty()) {
            return "§7[§bShopkeepersAddon§7] ";
        }

        return ChatColor.translateAlternateColorCodes('&', rawPrefix)
                .replace("\\n", "\n");
    }

    public static void debugLog(String message) {
        if (debugMode && logger != null) {
            logger.info("[DEBUG] " + message);
        }
    }

    public static void sendPlayerMessage(Player player, String message) {
        if (player == null || message == null || message.trim().isEmpty()) {
            return;
        }

        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                message.replace('§', '&')).replace("\\n", "\n");

        player.sendMessage(prefix + formattedMessage);
    }

    // Utility method for safe message sending
    public static void sendPlayerMessage(Player player, String message, String defaultValue) {
        if (message == null) {
            message = defaultValue;
        }
        sendPlayerMessage(player, message);
    }

    // Getter for prefix in case other classes need it
    public static String getPrefix() {
        return prefix;
    }

    // Safety check method
    public static boolean isEconomyEnabled() {
        return Money != null && config.getBoolean(CONFIG_ECONOMY_ENABLED_PATH, false);
    }

    private static void saveUpdatedConfig(FileConfiguration updatedConfig) {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            updatedConfig.save(configFile); // Direct YAML dump
            logger.info("Config saved directly to config.yml");
        } catch (Exception e) {
            logger.severe("Failed to save updated config.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
