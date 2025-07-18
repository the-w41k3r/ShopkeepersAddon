package me.w41k3r.shopkeepersAddon.gui.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.listeners.UpdateListeners.updateShops;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.SHOPS_SAVEPATH;

public class PlayerShopsManager {


    /*
        * This class is responsible for managing shop data.
        * It provides methods to save and fetch shop information such as shop name and location.
        * The data is stored in YAML files named after the player's UUID.
        *
        * Methods:
        * - saveShop: Saves the shop name and location for a player.
        * - fetchShopLocation: Retrieves the shop location for a player.
        * - fetchShopName: Retrieves the shop name for a player.
    * */

    public static void saveShop(List<String> shopName, Player player) {
        Path path = Paths.get(SHOPS_SAVEPATH, player.getUniqueId() + ".yml");
        File shopFile = path.toFile();

        try {
            if (!shopFile.exists()) {
                Files.createDirectories(path.getParent());
            }
            YamlConfiguration shop = YamlConfiguration.loadConfiguration(shopFile);
            // Save shopName as a list: first line is player's name, second is shopName
            shop.set("player", player.getName());
            shop.set("uuid", player.getUniqueId().toString());
            shop.set("shopName", shopName);
            shop.set("location", player.getLocation());
            shop.save(shopFile);
            sendPlayerMessage(player, config.getString("messages.playerShops.setShop", "Shop set successfully!"));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                updateShops(null);
            }, 20L); // Schedule update to run after saving the shop

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Failed to save shop for " + player.getName(), e); // Include exception in log
        }
    }

    public static Location fetchShopLocation(UUID uuid) {
        Path path = Paths.get(SHOPS_SAVEPATH, uuid + ".yml");
        File shopFile = path.toFile();
        if (!shopFile.exists()) {
            debugLog("Shop file does not exist for player: " + uuid);
            return null;
        }
        YamlConfiguration shop = YamlConfiguration.loadConfiguration(shopFile);
        Location location = shop.getLocation("location");
        if (location == null) {
            debugLog("No location found in shop file for player: " + uuid);
        }
        return location;
    }

}
