package me.w41k3r.shopkeepersaddon;

import me.w41k3r.shopkeepersaddon.Economy.EcoListeners;
import me.w41k3r.shopkeepersaddon.General.Listeners;
import me.w41k3r.shopkeepersaddon.General.UpdateListeners;
import me.w41k3r.shopkeepersaddon.General.VirtualOwner;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

import static me.w41k3r.shopkeepersaddon.General.Utils.debugLog;
import static me.w41k3r.shopkeepersaddon.General.Utils.loadShops;

public final class Main extends JavaPlugin {

    public static Main plugin;
    public static Plugin ShopkeepersInstance;

    public static Economy Money;
     public static VirtualOwner virtualOwner;
     public static ItemStack moneyItem;
     static String prefix;

     public static Set<String> editorItems = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = this;
        ShopkeepersInstance = getServer().getPluginManager().getPlugin("Shopkeepers");
        prefix = setting().getString("messages.prefix");
        if (plugin.getConfig().getBoolean("economy.enabled")) {
            if (!setupVault()) {
                this.getLogger().severe("Disabling due to Vault dependency error!");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        debugLog("Shopkeepers plugin found, loading shops...");
        loadShops();
        debugLog("Starting plugin!");
        getCommand("shopkeepersaddon").setExecutor(new Commands());
        getCommand("shops").setExecutor(new Commands());
        getCommand("setshop").setExecutor(new Commands());
        getCommand("visitshop").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        getServer().getPluginManager().registerEvents(new EcoListeners(), this);
        getServer().getPluginManager().registerEvents(new UpdateListeners(), this);
        virtualOwner = new VirtualOwner("ShopeepersAddon");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
    
    public static void sendPlayerMessage(Player player, String message){
        player.sendMessage(prefix + message);
    }
    
    // get config
    public static FileConfiguration setting() {
        return plugin.getConfig();
    }


    
}
