package me.w41k3r.shopkeepersaddon.General;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperRegistry;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import me.w41k3r.shopkeepersaddon.Main;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.*;

import static me.w41k3r.shopkeepersaddon.General.UIHandler.*;
import static me.w41k3r.shopkeepersaddon.General.UpdateListeners.refreshShops;
import static me.w41k3r.shopkeepersaddon.Main.*;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class Utils {
    static HashMap<String, UUID> onlineUUIDs = new HashMap<>();
    static HashMap<String, UUID> offlineUUIDs = new HashMap<>();
    public static ShopkeeperRegistry shopkeepersAPI = ShopkeepersAPI.getShopkeeperRegistry();
    public static void loadShops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                File dataFolder = ShopkeepersInstance.getDataFolder();
                File saveFolder = new File(dataFolder, "data/save.yml");
                debugLog("Loading shops from " + saveFolder);

                if (!saveFolder.exists()) {
                    errorLog("Save file does not exist: " + saveFolder);
                    return;
                }

                Map<String, Object> obj = null;
                try (FileInputStream fis = new FileInputStream(saveFolder)) {
                    Yaml yaml = new Yaml();
                    obj = (Map<String, Object>) yaml.load(fis);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }


                createShopsUI();
                initializeUIs();
                selectionUIs();

                for (String key : obj.keySet()) {
                    if (key.equalsIgnoreCase("data-version")) {
                        continue;
                    }
                    Map<String, Object> shopkeeper = (Map<String, Object>) obj.get(key);
                    addShopstoUI(shopkeeper);  // This interacts with the Bukkit API
                    addItemsToUI(shopkeeper);  // This interacts with the Bukkit API
                }

                log("Shops loaded successfully!");

            }
        }.runTaskAsynchronously(plugin);  // Run the file loading asynchronously
    }

    public static ItemStack createItemStackFromYaml(Map<String, Object> yamlData) {
        String typeString = (String) yamlData.get("type");
        int amount = ((Number) (yamlData.get("amount") == null ? 1 : yamlData.get("amount"))).intValue(); // Ensure correct type conversion

        Material material = Material.getMaterial(typeString);
        if (material == null) {
            debugLog("Material type not found: " + typeString);
            return null;
        }

        ItemStack itemStack = new ItemStack(material, amount);

        if (yamlData.containsKey("meta")) {
            Map<String, Object> metaData = (Map<String, Object>) yamlData.get("meta");
            ItemMeta meta = itemStack.getItemMeta();

            // Handle display name
            String displayNameJson = (String) metaData.get("display-name");
            if (displayNameJson != null) {
                meta.setDisplayName(parseJsonToText(displayNameJson)); // Parse JSON display name
            }

            // Handle lore
            List<String> loreJsonList = (List<String>) metaData.get("lore");
            if (loreJsonList != null) {
                List<String> lore = new ArrayList<>();
                for (String loreJson : loreJsonList) {
                    lore.add(parseJsonToText(loreJson)); // Parse JSON lore entries
                }
                meta.setLore(lore);
            }

            // Handle enchantments
            Map<String, Object> enchants = (Map<String, Object>) metaData.get("enchants");
            if (enchants != null) {
                enchants.forEach((key, value) -> {
                    String[] parts = key.split(":");
                    if (parts.length == 2) {
                        Enchantment enchantment = Enchantment.getByName(parts[0]);
                        if (enchantment != null) {
                            int level = ((Number) value).intValue();
                            meta.addEnchant(enchantment, level, true);
                        }
                    }
                });
            }

            // Handle ItemFlags
            List<String> itemFlags = (List<String>) metaData.get("ItemFlags");
            if (itemFlags != null) {
                itemFlags.forEach(flag -> meta.addItemFlags(ItemFlag.valueOf(flag)));
            }

            // Handle custom model data
            Integer customModelData = (Integer) metaData.get("custom-model-data");
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    // Helper method to parse JSON-formatted strings to plain text
    private static String parseJsonToText(String jsonString) {
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            StringBuilder builder = new StringBuilder();

            // Handle if the root is an object with 'text' and 'extra' fields
            if (jsonElement.isJsonObject()) {
                // Parse the 'text' field
                if (jsonElement.getAsJsonObject().has("text")) {
                    builder.append(jsonElement.getAsJsonObject().get("text").getAsString());
                }

                // Parse the 'extra' array
                if (jsonElement.getAsJsonObject().has("extra")) {
                    for (JsonElement extraElement : jsonElement.getAsJsonObject().get("extra").getAsJsonArray()) {
                        if (extraElement.isJsonObject() && extraElement.getAsJsonObject().has("text")) {
                            builder.append(extraElement.getAsJsonObject().get("text").getAsString());
                        }
                    }
                }
            }

            return builder.toString();
        } catch (Exception e) {
            debugLog("Failed to parse JSON: " + jsonString);
            return jsonString; // Fallback to raw string if parsing fails
        }
    }







    public static void debugLog(String s) {
        if (plugin.setting().getBoolean("debug")) {
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
        return plugin.setting().getString(key);
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
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);  // Set the profile field to your custom GameProfile

        } catch (NoSuchFieldException | IllegalAccessException e1) {
            e1.printStackTrace();
            return null;
        }
        head.setItemMeta(headMeta);
        
        return head;
    }

    public static ItemStack getHead(UUID playerUUID, String playerName) {
        if (heads.containsKey(sanitizedName(playerName))) {
            return heads.get(sanitizedName(playerName));
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        if (headMeta != null) {
            GameProfile profile = new GameProfile(playerUUID, sanitizedName(playerName)); // UUID only, name is not used
            // Use default skin data; replace with actual base64 skin data if available
            profile.getProperties().put("textures", new Property("textures", "<base64-skin-data>"));

            try {
                Field profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);


                head.setItemMeta(headMeta);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        heads.put(sanitizedName(playerName), head);
        return head;
    }
    public static UUID convertToJavaUUID(String mojangUUID) {
        if (mojangUUID.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string length");
        }
        // Insert hyphens to match the UUID format
        String formattedUUID = mojangUUID.substring(0, 8) + "-" +
                mojangUUID.substring(8, 12) + "-" +
                mojangUUID.substring(12, 16) + "-" +
                mojangUUID.substring(16, 20) + "-" +
                mojangUUID.substring(20);
        return UUID.fromString(formattedUUID);
    }

    static String getShopName(ItemStack head) {
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = null;
        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profile = (GameProfile) profileField.get(headMeta);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return profile == null ? "null" : profile.getName();
    }

    public static UUID getUUIDFromName(String playerName, boolean online) {
        debugLog("Getting UUID for " + playerName);

        if (online) {
            debugLog(onlineUUIDs.toString());
            if (onlineUUIDs.containsKey(playerName)) {
                return onlineUUIDs.get(playerName);
            }
            final String STEVE_UUID = "8667ba71b85a4004af54457a9734eed7";
            try {
                // Construct the URL
                String urlString = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set up the connection
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                // Parse the JSON response using Gson
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

                // Check if the response contains an "id" field
                if (jsonResponse.has("id")) {
                    onlineUUIDs.put(playerName, convertToJavaUUID(jsonResponse.get("id").getAsString()));
                    return convertToJavaUUID(jsonResponse.get("id").getAsString());  // Return the UUID if found
                } else {
                    // Handle the error response (e.g., username not found)
                    return convertToJavaUUID(STEVE_UUID);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return convertToJavaUUID(STEVE_UUID);
            }
        } else {
            debugLog(offlineUUIDs.toString());
            if (offlineUUIDs.containsKey(playerName)) {
                return offlineUUIDs.get(playerName);
            }
            OfflinePlayer player = getOfflinePlayer(playerName);
            if (player != null) {
                offlineUUIDs.put(playerName, player.getUniqueId());
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
        ItemStack headItem = null;
        ItemMeta headMeta;
        switch (key) {
            case "adminshop":
                AdminShopkeeper shopkeeper = (AdminShopkeeper) shopkeepersAPI.getShopkeeperByUniqueId(UUID.fromString(uniqueID));
                headItem = getHead(getUUIDFromName(sanitizedName(type), true) , shopkeeper.getName().isEmpty() ? "Admin" : sanitizedName(shopkeeper.getName()));
                headMeta = headItem.getItemMeta();
                headMeta.setDisplayName(shopkeeper.getName().isEmpty() ? "Admin Shop" : shopkeeper.getName());
                headMeta = setData(headMeta, "shopkeeperID", uniqueID);
                String sells = "Sells: ";
                List<? extends TradingRecipe> recipes = shopkeeper.getTradingRecipes(null);
                if (recipes.isEmpty()) {
                    sells += "Nothing!";
                } else {
                    for (TradingRecipe recipe : recipes) {
                        String name;
                        if (hasData(recipe.getResultItem().copy(), "ItemPrice", PersistentDataType.DOUBLE)) {
                            name = recipe.getResultItem().getItemMeta().getLocalizedName();
                        } else {
                            name = recipe.getResultItem().getItemMeta().getLocalizedName().isEmpty() ? recipe.getResultItem().getType().toString() : recipe.getResultItem().getItemMeta().getLocalizedName() + ":" + recipe.getResultItem().getType();
                        }


                        sells += name + ", ";
                    }
                }
                headMeta = setLore(headMeta, sells);
                headItem.setItemMeta(headMeta);
                return headItem;
            case "playershop":
                PlayerShopkeeper playerShopkeeper = (PlayerShopkeeper) shopkeepersAPI.getShopkeeperByUniqueId(UUID.fromString(uniqueID));
                headItem = getHead(getUUIDFromName(playerShopkeeper.getOwnerName(), true) , playerShopkeeper.getOwnerName());
                headMeta = headItem.getItemMeta();
                headMeta.setDisplayName(playerShopkeeper.getOwnerName() + "'s Shop");
                headMeta = setData(headMeta, "ownerID", getUUIDFromName(playerShopkeeper.getOwnerName(), false).toString());
                headItem.setItemMeta(headMeta);
                debugLog("Setting Playershop meta for " + playerShopkeeper.getOwnerName());
                headMeta = setLore(headMeta, getShopTitle(playerShopkeeper.getOwnerName()));
                headItem.setItemMeta(headMeta);
                return headItem;
        }
        return headItem;
    }

    static ItemMeta setLore(ItemMeta itemMeta, String description) {
        // Split description into words
        String[] words = description.split(", ");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        // Check if description is empty
        if (words.length == 0) {
            lines.add("Nothing to see here!");
        } else {
            for (String word : words) {
                // If adding the word would exceed 16 characters, start a new line
                if (currentLine.length() + word.length() + 1 > 16) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Add the word to the current line
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }
            // Add the last line (if any)
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        // Add custom footer lines
        lines.add(" §c-----");

        // Check if teleport to admin shop is enabled and add the line to the lore
        if (itemMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "shopkeeperID"), PersistentDataType.STRING) && plugin.setting().getBoolean("admin-shop.teleport.enabled")) {
            lines.add(" §6Left Click to teleport!");
        }

        // Check if teleport to player shop is enabled and add the line to the lore
        if (itemMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "ownerID"), PersistentDataType.STRING) && plugin.setting().getBoolean("player-shop.teleport.enabled")) {
            lines.add(" §6Left Click to teleport!");
        }

        // Check if remote shop is enabled and add the line to the lore
        if (itemMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "shopkeeperID"), PersistentDataType.STRING) && plugin.setting().getBoolean("admin-shop.remote")) {
            lines.add(" §6Right Click to open remotely!");
        }

        // Set the lore with the dynamic list of lines
        itemMeta.setLore(lines);
        return itemMeta;
    }

    public static void teleportToShop(Player player, String shopkeeperID, boolean isAdminShop) {
        // Get configuration values
        int warmupTimeInSeconds = isAdminShop
                ? plugin.getConfig().getInt("admin-shop.teleport.warmup")
                : plugin.getConfig().getInt("player-shop.teleport.warmup");
        boolean allowMovement = isAdminShop
                ? plugin.getConfig().getBoolean("admin-shop.teleport.allow-movement")
                : plugin.getConfig().getBoolean("player-shop.teleport.allow-movement");

        String titleMessage = isAdminShop
                ? setting().getString("messages.admin-teleport-title")
                : setting().getString("messages.player-teleport-title");
        String cancelMessage = setting().getString("messages.teleport-cancelled");
        String successMessage = setting().getString("messages.teleport-success");
        String errorMessage = setting().getString("messages.no-shop");

        // Bypass warmup permission check
        if (isAdminShop && player.hasPermission("shopkeeperaddon.adminshop.warmup.bypass")) {
            warmupTimeInSeconds = 0;
        } else if (!isAdminShop && player.hasPermission("shopkeeperaddon.playershop.warmup.bypass")) {
            warmupTimeInSeconds = 0;
        }

        // Retrieve shop location
        Location shopLocation;
        if (isAdminShop) {
            // Find the shopkeeper by their unique ID for admin shop
            Shopkeeper shopkeeper = shopkeepersAPI.getShopkeeperByUniqueId(UUID.fromString(shopkeeperID));
            shopLocation = shopkeeper.getLocation();
        } else {
            // Retrieve shop data from the database for player shop
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

    public static String getShopTitle(String playerName) {
        String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/shops.db";
        String shopTitle = null;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // Fetch the shop title for the player using their UUID
                String querySQL = "SELECT shop_title FROM shops WHERE owner_uuid = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
                    String playerUUID = getUUIDFromName(playerName, false).toString();
                    pstmt.setString(1, playerUUID);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            shopTitle = rs.getString("shop_title");
                            shopTitle = "§e" + shopTitle.substring(1, shopTitle.length() - 1);
                            debugLog("Shop title found: " + shopTitle);
                        } else {
                            debugLog("No shop found for player UUID: " + playerUUID);
                        }
                    }
                }
            }
            if (shopTitle == null) {
                shopTitle = setting().getString("messages.no-shop");
            }
        } catch (SQLException e) {
            shopTitle = setting().getString("messages.no-shop");
            return shopTitle;
        }

        return shopTitle;
    }




    public static void setShop(String playerName, String description, Player player) {
        String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/shops.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // Create the shops table with columns for direction
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

                // Insert or update shop data
                String playerUUID = getUUIDFromName(playerName, false).toString();
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
                    sendPlayerMessage(player,setting().getString("messages.shop-set"));
                }
            }
            refreshShops();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}