package me.w41k3r.shopkeepersAddon.gui.init;

import me.w41k3r.shopkeepersAddon.gui.objects.VirtualInventoryOwner;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.gui.managers.SkinsManager.*;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.managers.FetchShopkeepersManager.playerShopItems;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.setCurrentPage;
import static org.bukkit.Bukkit.createInventory;

public class InitPlayerPages {


    private static final Logger log = LoggerFactory.getLogger(InitPlayerPages.class);

    /* --------------------------------
         * Initialization of the Player Shops Page
         * Size: 27
         * Slots for player shop buttons: 11 and 15
        -------------------------------- */
    public static void createPlayerShopsPage() {
        new BukkitRunnable() {
            @Override
            public void run() {
                debugLog("Creating Player Shops Page Inventory");
                VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("playerShopsPage");
                playerShopsPage = createInventory(virtualOwner, 27,
                        config.getString("messages.playerShopsPage.title"));
                playerShopsPage.setItem(11,
                        getIcon("messages.playerShopsPage.buttons.playerShops",
                                config.getString("heads.playerShopsPage.playerShops"),
                                "PLAYER_SHOPS_LIST", null));
                playerShopsPage.setItem(15,
                        getIcon("messages.playerShopsPage.buttons.playerItems",
                                config.getString("heads.playerShopsPage.playerItems"),
                                "PLAYER_ITEMS_LIST", null));
            }
        }.runTaskAsynchronously(plugin);
    }


    /* --------------------------------
     * Initialization of the Player Shops
     * Size: 54
     * Slots for admin shops buttons: 0 to 45
     * Slots for navigation buttons: 46 and 53
     * Slot for back button: 49
     *     * */
    public static void createPlayerShopsList() {
        new BukkitRunnable() {
            @Override
            public void run() {
                debugLog("Creating Player Shops Inventory");
                final int ITEMS_PER_PAGE = 45;
                List<YamlConfiguration> validShops = getAllPlayerShops();

                int totalPages = (int) Math.ceil((double) validShops.size() / ITEMS_PER_PAGE);
                playerShopsList.clear();
                playerShopsList = new ArrayList<>(totalPages);

                for (int page = 0; page < totalPages; page++) {
                    VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("playerShopsList_" + page);
                    Inventory pageInventory = createInventory(virtualOwner, 54,
                            config.getString("messages.playerShops.title")
                                    .replace("%page%", String.valueOf(page + 1)));

                    // Add shop items
                    int startIndex = page * ITEMS_PER_PAGE;
                    int endIndex = Math.min((page + 1) * ITEMS_PER_PAGE, validShops.size());

                    for (int i = startIndex; i < endIndex; i++) {
                        YamlConfiguration shopConfig = validShops.get(i);


                        ItemStack shopIcon = createPlayerShopIcon( shopConfig.getString("player"),
                                UUID.fromString(shopConfig.getString("uuid")),
                                shopConfig.getString("player"),
                                shopConfig.getStringList("shopName")
                        );

                        pageInventory.setItem(i - startIndex, shopIcon);
                    }


                    // Add filler items in 45-53 slots
                    for (int i = 45; i <= 53; i++) {
                        pageInventory.setItem(i, playerFillerItem);
                    }

                    // Add navigation items
                    ItemStack backButton = getIcon("messages.back",
                            config.getString("heads.back"),
                            "PLAYER_SHOPS_PAGE", null);
                    backButton = setCurrentPage(backButton, "PLAYER_SHOPS_LIST");
                    pageInventory.setItem(49, backButton);

                    // Previous page button
                    if (page > 0) {
                        pageInventory.setItem(45, getIcon("messages.previousPage",
                                config.getString("heads.previousPage"),
                                page - 1,
                                null));
                    }

                    // Next page button
                    if (page < totalPages - 1) {
                        pageInventory.setItem(53, getIcon("messages.nextPage",
                                config.getString("heads.nextPage"),
                                page + 1,
                                null));
                    }


                    playerShopsList.add(pageInventory);
                }

                debugLog("Created " + totalPages + " pages of Player shops");

                lastUpdateTime = System.currentTimeMillis();
                debugLog(lastUpdateTime + " " + System.currentTimeMillis());
            }
        }.runTaskAsynchronously(plugin);
    }

    private static List<YamlConfiguration> getAllPlayerShops() {
        List<YamlConfiguration> shopFiles = new ArrayList<>();
        File dir = new File(SHOPS_SAVEPATH);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    shopFiles.add(YamlConfiguration.loadConfiguration(file));
                }
            }
        }
        return shopFiles;
    }

    public static YamlConfiguration getPlayerShop(String uuid) {
        YamlConfiguration shopFiles = new YamlConfiguration();
        File dir = new File(SHOPS_SAVEPATH);
        if (dir.exists() && dir.isDirectory()) {
            File shopFile = new File(dir, uuid + ".yml");
            if (shopFile.exists()) {
                return YamlConfiguration.loadConfiguration(shopFile);
            } else {
                debugLog("Shop file does not exist for player: " + uuid);
            }
        } else {
            debugLog("Shops directory does not exist: " + SHOPS_SAVEPATH);
        }
        return null;
    }


    /* --------------------------------
     * Initialization of the Player Items Shops
     * Size: 54
     * Slots for Player Items buttons: 0 to 45
     * Slots for navigation buttons: 45 and 53
     *     * */
    public static void createPlayerItemsList() {
        new BukkitRunnable() {
            @Override
            public void run() {
                debugLog("Creating Player Items Inventory");
                final int ITEMS_PER_PAGE = 45;

                // Convert HashSet to List for ordered access
                List<ItemStack> orderedItems = new ArrayList<>(playerShopItems);
                int totalPages = (int) Math.ceil((double) orderedItems.size() / ITEMS_PER_PAGE);
                playerItemsList.clear();
                playerItemsList = new ArrayList<>(totalPages);

                for (int page = 0; page < totalPages; page++) {
                    VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("playerItems_" + page);
                    Inventory pageInventory = createInventory(virtualOwner, 54,
                            config.getString("messages.playerItems.title")
                                    .replace("%page%", String.valueOf(page + 1)));

                    // Add items for this page
                    int startIndex = page * ITEMS_PER_PAGE;
                    int endIndex = Math.min((page + 1) * ITEMS_PER_PAGE, orderedItems.size());

                    for (int i = startIndex; i < endIndex; i++) {
                        pageInventory.setItem(i - startIndex, orderedItems.get(i));
                    }
                    // Add filler items in 45-53 slots
                    for (int i = 45; i <= 53; i++) {
                        pageInventory.setItem(i, playerFillerItem);
                    }

                    // Add back button
                    ItemStack backButton = getIcon("messages.back",
                            config.getString("heads.back"),
                            "PLAYER_SHOPS_PAGE", null);
                    backButton = setCurrentPage(backButton, "PLAYER_ITEMS_LIST");
                    pageInventory.setItem(49, backButton);

                    // Add previous page button
                    if (page > 0) {
                        pageInventory.setItem(45, getIcon("messages.previousPage",
                                config.getString("heads.previousPage"),
                                page - 1,
                                null));
                    }

                    // Add next page button
                    if (page < totalPages - 1) {
                        pageInventory.setItem(53, getIcon("messages.nextPage",
                                config.getString("heads.nextPage"),
                                page + 1,
                                null));
                    }

                    playerItemsList.add(pageInventory);
                }

                debugLog("Created " + totalPages + " pages of player items");

                lastUpdateTime = System.currentTimeMillis();
                debugLog(lastUpdateTime + " " + System.currentTimeMillis());
            }
        }.runTaskAsynchronously(plugin);
    }



}
