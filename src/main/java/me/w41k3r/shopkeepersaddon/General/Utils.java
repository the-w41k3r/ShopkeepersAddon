package me.w41k3r.shopkeepersaddon.General;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.properties.Property;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperRegistry;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import me.w41k3r.shopkeepersaddon.Main;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.w41k3r.shopkeepersaddon.General.UIHandler.*;
import static me.w41k3r.shopkeepersaddon.General.UpdateListeners.startUpdates;
import static me.w41k3r.shopkeepersaddon.Main.*;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class Utils {
    public static ShopkeeperRegistry shopkeepersAPI = ShopkeepersAPI.getShopkeeperRegistry();
    public static HashMap<UUID, String> shopTitles = new HashMap<>();
    public static HashMap<String, ItemStack> heads = new HashMap<>();
    static File onlineCacheFile = new File(plugin.getDataFolder(), "OnlineCache.yml");
    static File offlineCacheFile = new File(plugin.getDataFolder(), "OfflineCache.yml");
    static FileConfiguration onlineCache = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "OnlineCache.yml"));
    static FileConfiguration offlineCache = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "OfflineCache.yml"));
    public static void loadShops() {
        new BukkitRunnable() {
            @Override
            public void run() {

                FileConfiguration config;
                try {
                    config = YamlConfiguration.loadConfiguration(new File(ShopkeepersInstance.getDataFolder(), "data/save.yml"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                loadShopTitles();
                createShopsUI();
                selectionUIs();

                for (String key : config.getKeys(false)) {
                    if (key.equalsIgnoreCase("data-version")) {
                        continue;
                    }
                    readyItemsUI(config, key);
                }

                log("Shops loaded successfully!");
            }
        }.runTaskAsynchronously(plugin);

        startUpdates();
    }


    public static void debugLog(String s) {
        if (setting().getBoolean("debug")) {
            Bukkit.getLogger().warning("ShopkeepersAddon Debug » " + s);
        }
    }
    public static void errorLog(String s) {
        Bukkit.getLogger().severe(s);
    }

    public static void log(String s) {
        Bukkit.getLogger().info("§eShopkeepersAddon » §a" + s);
    }

    public static String configData(String key) {
        return getSettingString(key);
    }

    public static ItemMeta setData(ItemMeta itemMeta, String key, String value) {
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
        return itemMeta;
    }

    public static ItemMeta setPrice(ItemMeta itemMeta, String key, Double value) {
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.DOUBLE, value);
        return itemMeta;
    }

    public static boolean hasData(ItemStack item, String key, PersistentDataType type) {
        if (item == null || item.getItemMeta() == null || item.getItemMeta().getPersistentDataContainer() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, key), type);
    }

    public static String getData(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }

    public static Double getPrice(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "itemprice"), PersistentDataType.DOUBLE);
    }

    public static ItemStack getCustomHead(String name, String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        try {
            if (isPaperAvailable()) {
                PlayerProfile profile = Bukkit.createProfile(name);
                profile.getProperties().add(new ProfileProperty("textures", texture));
                headMeta.setPlayerProfile(profile);
            } else {
                GameProfile profile = new GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), name);
                profile.getProperties().put("textures", new Property("textures", texture));
                Field profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
            }
            head.setItemMeta(headMeta);
        } catch (Exception e) {
            debugLog("Error getting head for " + name);
            return null;
        }

        return head;
    }

    private static PlayerProfile createProfileWithRetry(UUID uuid, String name) throws InterruptedException {
        int retries = 5;
        long backoff = 1000; // Initial backoff time in milliseconds

        for (int i = 0; i < retries; i++) {
            try {
                debugLog("Creating profile for " + name + " with UUID " + uuid);
                PlayerProfile profile = Bukkit.createProfile(uuid, name);
                return profile;
            } catch (MinecraftClientHttpException e) {
                if (e.getStatus() == 429) {
                    Thread.sleep(backoff);
                    backoff *= 2;
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Failed to create profile after " + retries + " retries" + "for " + name);
    }


    public static ItemStack getHead(UUID playerUUID, String playerName) {
        if (heads.containsKey(sanitizedName(playerName))) {
            return heads.get(sanitizedName(playerName));
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        debugLog("Getting head" + playerUUID + " " + headMeta);

        if (headMeta != null) {
            try {
                if (isPaperAvailable()) {

                    if(onlineCache.contains(playerName)) {
                        debugLog("Getting online cache head for " + playerName);
                        PlayerProfile profile = createProfileWithRetry(UUID.fromString((String) onlineCache.get(playerName)), sanitizedName(playerName));
                        headMeta.setPlayerProfile(profile);
                    } else {
                        PlayerProfile profile = Bukkit.createProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"),playerName);
                        headMeta.setPlayerProfile(profile);
                    }
                } else {
                    GameProfile profile = new GameProfile(playerUUID, sanitizedName(playerName));
                    Field profileField = headMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(headMeta, profile);
                }
                head.setItemMeta(headMeta);
            } catch (NoSuchFieldException | IllegalAccessException | InterruptedException e) {
                debugLog("Error getting head for " + playerName);
                return null;
            }
        }
        heads.put(sanitizedName(playerName), head);
        return head;
    }

    static String getShopName(ItemStack head) {
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        String playerName = "null";

        if (headMeta != null) {
            try {
                if (isPaperAvailable()) {
                    PlayerProfile profile = headMeta.getPlayerProfile();
                    playerName = profile != null ? profile.getName() : playerName;
                } else {
                    Field profileField = headMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    GameProfile profile = (GameProfile) profileField.get(headMeta);
                    playerName = profile != null ? profile.getName() : playerName;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                debugLog("Error getting head name!");
            }
        }
        return playerName;
    }

    public static UUID convertToJavaUUID(String mojangUUID) {
        if (mojangUUID.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string length" + mojangUUID);
        }
        // Insert hyphens to match the UUID format
        String formattedUUID = mojangUUID.substring(0, 8) + "-" +
                mojangUUID.substring(8, 12) + "-" +
                mojangUUID.substring(12, 16) + "-" +
                mojangUUID.substring(16, 20) + "-" +
                mojangUUID.substring(20);
        return UUID.fromString(formattedUUID);
    }
    public static boolean isPaperAvailable() {
        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static UUID getUUIDFromName(String playerName, boolean online) {
        if (online) {
            if (onlineCache.contains(playerName)) {
                return UUID.fromString(onlineCache.getString(playerName));
            } else if (offlineCache.contains(playerName)) {
                return UUID.fromString(offlineCache.getString(playerName));
            }

            try {
                String urlString = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
                HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }

                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

                    if (jsonResponse.has("id")) {
                        String uuidString = jsonResponse.get("id").getAsString();
                        UUID uuid = convertToJavaUUID(uuidString);
                        onlineCache.set(playerName, uuid.toString());
                        try {
                            onlineCache.save(onlineCacheFile); // Save after setting the value
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        debugLog("UUID for " + playerName + " is " + uuid);
                        return uuid;
                    } else {
                        return getUUIDFromName(playerName, false);
                    }
                }
            } catch (Exception e) {
                return getUUIDFromName(playerName, false);
            }
        } else {
            if (offlineCache.contains(playerName)) {
                debugLog("Getting offline cache UUID for " + playerName + " " + offlineCache.getString(playerName));
                return UUID.fromString(offlineCache.getString(playerName));
            }

            OfflinePlayer player = getOfflinePlayer(playerName);
            if (player != null) {
                String uuidString = player.getUniqueId().toString();
                offlineCache.set(playerName, uuidString);
                try {
                    offlineCache.save(offlineCacheFile); // Save after setting the value
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return player.getUniqueId();
            }
        }
        return null;
    }



    static String sanitizedName(String name) {
        String sanitizedName = name.replaceAll("[^a-zA-Z0-9_]", "");
        if (sanitizedName.length() > 16) {
            sanitizedName = sanitizedName.substring(0, 15);
        }
        return sanitizedName;
    }

    static ItemStack getIcon(String uniqueID, String key, String type) {
        try {
            ItemStack headItem = null;
            ItemMeta headMeta;
            switch (key) {
                case "adminshop":
                    AdminShopkeeper shopkeeper = (AdminShopkeeper) shopkeepersAPI.getShopkeeperByUniqueId(UUID.fromString(uniqueID));
                    debugLog("Admin Shopkeeper: " + type);
                    headItem = getHead(getUUIDFromName(sanitizedName(type), true) , type);
                    headMeta = headItem.getItemMeta();
                    headMeta.setDisplayName(shopkeeper.getName().isEmpty() ? "Admin Shop" : shopkeeper.getName());
                    headMeta = setData(headMeta, "shopkeeperID", uniqueID);
                    headItem.setItemMeta(headMeta);
                    return headItem;
                case "playershop":
                    PlayerShopkeeper playerShopkeeper = (PlayerShopkeeper) shopkeepersAPI.getShopkeeperByUniqueId(UUID.fromString(uniqueID));
                    headItem = getHead(getUUIDFromName(playerShopkeeper.getOwnerName(), true) , playerShopkeeper.getOwnerName());
                    headMeta = headItem.getItemMeta();
                    headMeta.setDisplayName(playerShopkeeper.getOwnerName() + "'s Shop");
                    headMeta = setData(headMeta, "ownerID", getUUIDFromName(playerShopkeeper.getOwnerName(), false).toString());
                    headItem.setItemMeta(headMeta);
                    return headItem;
            }
            return headItem;
        }
        catch (Exception e) {
            debugLog("Error getting icon for " + uniqueID);
            return null;
        }
    }

    public static void teleportToShop(Player player, String shopkeeperID, boolean isAdminShop) {
        // Get configuration values
        int warmupTimeInSeconds = isAdminShop
                ? plugin.getConfig().getInt("admin-shop.teleport.warmup")
                : plugin.getConfig().getInt("player-shop.teleport.warmup");
        boolean allowMovement = isAdminShop
                ? plugin.getConfig().getBoolean("admin-shop.teleport.allow-movement")
                : plugin.getConfig().getBoolean("player-shop.teleport.allow-movement");

        String cancelMessage = getSettingString("messages.teleport-cancelled");
        String successMessage = getSettingString("messages.teleport-success");
        String errorMessage = getSettingString("messages.no-shop");

        // Bypass warmup permission check
        if (isAdminShop && player.hasPermission("shopkeeperaddon.adminshop.warmup.bypass")) {
            warmupTimeInSeconds = 0;
        } else if (!isAdminShop && player.hasPermission("shopkeeperaddon.playershop.warmup.bypass")) {
            warmupTimeInSeconds = 0;
        }

        // Retrieve shop location
        Location shopLocation;
        if (isAdminShop) {
            debugLog("AdminShop ID: " + shopkeeperID);
            Shopkeeper shopkeeper = shopkeepersAPI.getShopkeeperByUniqueId(UUID.fromString(shopkeeperID));
            shopLocation = shopkeeper.getLocation();
        } else {
            shopLocation = getPlayerShopLocation(shopkeeperID);
            debugLog("ID: " + shopkeeperID);
        }

        // Check if the location is valid
        if (shopLocation == null) {
            sendPlayerMessage(player,errorMessage);
            debugLog("Shop location not found for id: " + shopkeeperID);
            return;
        }

        // Set up the teleport warmup task or teleport instantly if warmupTimeInSeconds == 0
        if (warmupTimeInSeconds > 0) {
            TeleportWarmupTask teleportTask = new TeleportWarmupTask(player, shopLocation, warmupTimeInSeconds, allowMovement, cancelMessage, successMessage, plugin, isAdminShop);
            teleportTask.startWarmup();
        } else {
            // Instant teleport if warmup is bypassed or set to 0
            shopLocation.setDirection(shopLocation.toVector().subtract(player.getLocation().toVector()));
            player.teleport(shopLocation);
            sendPlayerMessage(player,successMessage);
        }
    }

    // Method to get player shop location from the database
    private static Location getPlayerShopLocation(String ownerUUID) {
        String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/shops.db";
        Location location = null;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String selectSQL = "SELECT x, y, z, world, yaw, pitch FROM shops WHERE owner_uuid = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
                    pstmt.setString(1, ownerUUID);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double x = rs.getDouble("x");
                            double y = rs.getDouble("y");
                            double z = rs.getDouble("z");
                            String worldName = rs.getString("world");
                            float yaw = rs.getFloat("yaw");
                            float pitch = rs.getFloat("pitch");

                            World world = Bukkit.getWorld(worldName);
                            if (world != null) {
                                location = new Location(world, x, y, z);
                                location.setYaw(yaw);
                                location.setPitch(pitch);
                                debugLog("Yaw and Pitch: " + yaw + " " + pitch);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            return null;
        }

        return location;
    }


    public static List<String> getShopTitle(String playerName) {
        UUID playerUUID = UUID.fromString(getUUIDFromName(playerName, false).toString());

        if (shopTitles.containsKey(playerUUID)) {
            return Arrays.stream(shopTitles.get(playerUUID).split("\\\\n"))
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .map(line -> line.length() > 1 ? line.substring(1, line.length() - 1) : "") // Remove first and last character
                    .collect(Collectors.toList());
        }

        // Return the no-shop-lore from config, with color formatting
        return setting().getStringList("messages.no-shop-lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }




    public static void loadShopTitles() {
        String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/shops.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String querySQL = "SELECT owner_uuid, shop_title FROM shops";

                try (PreparedStatement pstmt = conn.prepareStatement(querySQL);
                     ResultSet rs = pstmt.executeQuery()) {

                    while (rs.next()) {
                        UUID ownerUUID = UUID.fromString(rs.getString("owner_uuid"));
                        String shopTitle = "§e" + rs.getString("shop_title").substring(1, rs.getString("shop_title").length() - 1);
                        shopTitles.put(ownerUUID, shopTitle);
                        debugLog("Loaded shop title for UUID: " + ownerUUID + ", Title: " + shopTitle);
                    }
                }
            }
        } catch (SQLException e) {
            debugLog("Error loading shop titles: " + e.getMessage());
        }
    }


    public static void setShop(String playerName, String description, Player player) {
        String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/shops.db";
        String playerUUID = getUUIDFromName(playerName, false).toString();

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    String createTableSQL = "CREATE TABLE IF NOT EXISTS shops (" +
                            "owner TEXT NOT NULL," +
                            "owner_uuid TEXT NOT NULL PRIMARY KEY," +
                            "shop_title TEXT NOT NULL," +
                            "x REAL NOT NULL," +
                            "y REAL NOT NULL," +
                            "z REAL NOT NULL," +
                            "world TEXT NOT NULL," +
                            "yaw REAL NOT NULL," +
                            "pitch REAL NOT NULL" +
                            ")";
                    stmt.execute(createTableSQL);
                }

                debugLog("Player UUID: " + playerUUID);
                String insertOrUpdateSQL = "INSERT INTO shops (owner, owner_uuid, shop_title, x, y, z, world, yaw, pitch) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT(owner_uuid) DO UPDATE SET " +
                        "shop_title = excluded.shop_title, " +
                        "x = excluded.x, " +
                        "y = excluded.y, " +
                        "z = excluded.z, " +
                        "world = excluded.world, " +
                        "yaw = excluded.yaw, " +
                        "pitch = excluded.pitch";

                try (PreparedStatement pstmt = conn.prepareStatement(insertOrUpdateSQL)) {
                    pstmt.setString(1, playerName);
                    pstmt.setString(2, playerUUID);
                    pstmt.setString(3, description);
                    pstmt.setDouble(4, player.getLocation().getX());
                    pstmt.setDouble(5, player.getLocation().getY());
                    pstmt.setDouble(6, player.getLocation().getZ());
                    pstmt.setString(7, player.getLocation().getWorld().getName());
                    pstmt.setFloat(8, player.getLocation().getYaw()); // Store yaw
                    pstmt.setFloat(9, player.getLocation().getPitch()); // Store pitch
                    debugLog("Yaw and Pitch: " + player.getLocation().getYaw() + " " + player.getLocation().getPitch());

                    pstmt.executeUpdate();
                    sendPlayerMessage(player,getSettingString("messages.shop-set"));
                }
            }

            UUID playerID = UUID.fromString(playerUUID);

            if (shopTitles.containsKey(playerID)){
                shopTitles.replace(playerID, description);
            } else {
                shopTitles.put(playerID, description);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}