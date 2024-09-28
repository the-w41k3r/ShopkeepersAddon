package me.w41k3r.shopkeepersaddon.General;

import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.nisovin.shopkeepers.api.events.ShopkeeperEditedEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static me.w41k3r.shopkeepersaddon.General.UIHandler.*;
import static me.w41k3r.shopkeepersaddon.General.Utils.*;
import static me.w41k3r.shopkeepersaddon.Main.*;

public class UpdateListeners implements Listener {





    static void startUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                refreshShops();
            }
        }.runTaskTimerAsynchronously(plugin, setting().getLong("refresh-rate") * 20, setting().getLong("refresh-rate") * 20);
    }


    static void refreshShops() {
        FileConfiguration config;
        try {
            config = YamlConfiguration.loadConfiguration(new File(ShopkeepersInstance.getDataFolder(), "data/save.yml"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        adminItemsList.clear();
        adminShops.clear();
        adminItems.clear();
        adminHeads.clear();
        adminShopItems.clear();

        playerItemsList.clear();
        playerShops.clear();
        playerItems.clear();
        playerHeads.clear();
        playerShopItems.clear();

        for (String key : config.getKeys(false)) {
            if (key.equalsIgnoreCase("data-version")) {
                continue;
            }
            readyItemsUI(config, key);
        }
    }




}
