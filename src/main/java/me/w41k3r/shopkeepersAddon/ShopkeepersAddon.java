package me.w41k3r.shopkeepersAddon;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static me.w41k3r.shopkeepersAddon.Commands.initCommands;
import static me.w41k3r.shopkeepersAddon.Events.initEvents;
import static me.w41k3r.shopkeepersAddon.gui.BaseGUIHandlers.getFillerItem;
import static me.w41k3r.shopkeepersAddon.gui.init.InitAdminPages.initPages;
import static me.w41k3r.shopkeepersAddon.gui.managers.FetchShopkeepersManager.fetchShopkeepers;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.lastUpdateTime;


public final class ShopkeepersAddon extends JavaPlugin {

    public static FileConfiguration config;
    public static ShopkeepersAddon plugin;
    private static Logger logger = null;
    public static boolean debugMode;
    public static ItemStack adminFillerItem;
    public static ItemStack playerFillerItem;
    static String prefix;
    public static Economy Money;


    /**
     * Get the instance of the plugin.
     *
     * @return the instance of the plugin
     */

//    @Override
//    public void onLoad() {
//        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
//        //On Bukkit, calling this here is essential, hence the name "load"
//        PacketEvents.getAPI().load();
//    }

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        config = getConfig();
        debugMode = plugin.getConfig().getBoolean("debug");
        logger = getPlugin(ShopkeepersAddon.class).getLogger();
        if (!getServer().getOnlineMode() && getServer().getPluginManager().getPlugin("SkinsRestorer") == null) {
            getLogger().info("Server is offline mode, skins restorer plugin is required for player skins.");
        }
        if (plugin.getConfig().getBoolean("economy.enabled")) {
            if (!setupVault()) {
                this.getLogger().severe("Disabling due to Vault dependency error!");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        initCommands();
        initEvents();
        logger.info("Fetching shopkeepers...");
        fetchShopkeepers();
        initPages();
        adminFillerItem  = getFillerItem("adminShop.filler");
        playerFillerItem = getFillerItem("playerShop.filler");
        prefix = ChatColor.translateAlternateColorCodes('&',
                config.getString("messages.prefix", "&7[&bShopkeepersAddon&7] "))
                .replace("\\n", "\n");
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
//        PacketEvents.getAPI().terminate();

    }


    public static void debugLog(String message) {
        if (debugMode) logger.info(message);
    }

    public static void sendPlayerMessage(Player player, String message) {
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message.replace('§', '&')).replace("\\n", "\n");
        player.sendMessage(prefix + formattedMessage);
    }

    private boolean setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            debugLog("Vault plugin not found!");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            debugLog("No economy found!");
            return false;
        }
        Money = rsp.getProvider();
        return true;
    }
}



