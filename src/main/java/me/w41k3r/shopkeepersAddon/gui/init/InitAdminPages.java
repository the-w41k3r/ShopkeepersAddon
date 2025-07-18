package me.w41k3r.shopkeepersAddon.gui.init;

import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import me.w41k3r.shopkeepersAddon.gui.objects.VirtualInventoryOwner;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;
import static me.w41k3r.shopkeepersAddon.gui.init.InitPlayerPages.createPlayerShopsPage;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.managers.FetchShopkeepersManager.*;
import static me.w41k3r.shopkeepersAddon.gui.managers.SkinsManager.getIcon;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.setCurrentPage;
import static org.bukkit.Bukkit.createInventory;

public class InitAdminPages {

    public static void initPages() {
        debugLog("Initializing Admin GUI Pages");
        createHomePage();
        createAdminShopsPage();
        createPlayerShopsPage();
    }

    /* --------------------------------
     * Initialization of the Home Page
     * Size: 27
     * Slots for admin shop and player shop buttons: 11 and 15
    -------------------------------- */
    private static void createHomePage() {
        new BukkitRunnable() {
            @Override
            public void run() {
                debugLog("Creating Home Page Inventory");
                VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("homePage");
                homePage = createInventory(virtualOwner, 27,
                        config.getString("messages.homePage.title"));
                homePage.setItem(11,
                        getIcon("messages.homePage.buttons.adminShops",
                                config.getString("heads.homePage.adminShops"),
                                "ADMIN_SHOPS_PAGE", null));
                homePage.setItem(15,
                        getIcon("messages.homePage.buttons.playerShops",
                                config.getString("heads.homePage.playerShops"),
                                "PLAYER_SHOPS_PAGE", null));
            }
        }.runTaskAsynchronously(plugin);
    }

    /* --------------------------------
     * Initialization of the Admin Shops Page
     * Size: 27
     * Slots for admin shop buttons: 11 and 15
    -------------------------------- */
    private static void createAdminShopsPage() {
        new BukkitRunnable() {
            @Override
            public void run() {
                debugLog("Creating Admin Shops Page Inventory");
                VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("adminShopsPage");
                adminShopsPage = createInventory(virtualOwner, 27,
                        config.getString("messages.adminShopsPage.title"));
                adminShopsPage.setItem(11,
                        getIcon("messages.adminShopsPage.buttons.adminShops",
                                config.getString("heads.adminShopsPage.adminShops"),
                                "ADMIN_SHOPS_LIST", null));
                adminShopsPage.setItem(15,
                        getIcon("messages.adminShopsPage.buttons.adminItems",
                                config.getString("heads.adminShopsPage.adminItems"),
                                "ADMIN_ITEMS_LIST", null));
            }
        }.runTaskAsynchronously(plugin);
    }



    /* --------------------------------
     * Initialization of the Admin Shops
     * Size: 54
     * Slots for admin shops buttons: 0 to 45
     * Slots for navigation buttons: 46 and 53
     * Slot for back button: 49
     *     * */
    public static void createAdminShopsList() {
        new BukkitRunnable() {
            @Override
            public void run() {
                debugLog("Creating Admin Shops Inventory");
                final int ITEMS_PER_PAGE = 45;
                List<AdminShopkeeper> validShopkeepers = adminShopkeepers.stream()
                        .filter(shop -> !shop.getTradingRecipes(null).isEmpty())
                        .toList();

                int totalPages = (int) Math.ceil((double) validShopkeepers.size() / ITEMS_PER_PAGE);
                adminShopsList = new ArrayList<>(totalPages);

                for (int page = 0; page < totalPages; page++) {
                    VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("adminShopsList_" + page);
                    Inventory pageInventory = createInventory(virtualOwner, 54,
                            config.getString("messages.adminShops.title")
                                    .replace("%page%", String.valueOf(page + 1)));

                    // Add shop items
                    int startIndex = page * ITEMS_PER_PAGE;
                    int endIndex = Math.min((page + 1) * ITEMS_PER_PAGE, validShopkeepers.size());

                    for (int i = startIndex; i < endIndex; i++) {
                        AdminShopkeeper shopkeeper = validShopkeepers.get(i);
                        String displayName = shopkeeper.getDisplayName();

                        ItemStack shopIcon = getIcon("messages.adminShops.buttons",
                                config.getString("heads.adminShops.Shops"),
                                String.valueOf(shopkeeper.getUniqueId()),
                                displayName);
                        int j = 0;

                        pageInventory.setItem(i - startIndex, shopIcon);
                    }

                    // Add filler items in 45-53 slots
                    for (int i = 45; i <= 53; i++) {
                        pageInventory.setItem(i, adminFillerItem);
                    }

                    // Add navigation items
                    ItemStack backButton = getIcon("messages.back",
                            config.getString("heads.back"),
                            "ADMIN_SHOPS_PAGE", null);
                    backButton = setCurrentPage(backButton, "ADMIN_SHOPS_LIST");
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

                    adminShopsList.add(pageInventory);
                }

                debugLog("Created " + totalPages + " pages of admin shops");

                lastUpdateTime = System.currentTimeMillis();
                debugLog(lastUpdateTime + " " + System.currentTimeMillis());
            }
        }.runTaskAsynchronously(plugin);
    }


    /* --------------------------------
     * Initialization of the Admin Items Shops
     * Size: 54
     * Slots for Admin Items buttons: 0 to 45
     * Slots for navigation buttons: 45 and 53
     *     * */
    public static void createAdminItemsList() {
        new BukkitRunnable() {
            @Override
            public void run() {
                debugLog("Creating Admin Items Inventory");
                final int ITEMS_PER_PAGE = 45;

                // Convert HashSet to List for ordered access
                List<ItemStack> orderedItems = new ArrayList<>(adminShopItems);
                int totalPages = (int) Math.ceil((double) orderedItems.size() / ITEMS_PER_PAGE);
                adminItemsList = new ArrayList<>(totalPages);

                for (int page = 0; page < totalPages; page++) {
                    VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("adminListItems_" + page);
                    Inventory pageInventory = createInventory(virtualOwner, 54,
                            config.getString("messages.adminItems.title")
                                    .replace("%page%", String.valueOf(page + 1)));

                    // Add items for this page
                    int startIndex = page * ITEMS_PER_PAGE;
                    int endIndex = Math.min((page + 1) * ITEMS_PER_PAGE, orderedItems.size());

                    for (int i = startIndex; i < endIndex; i++) {
                        pageInventory.setItem(i - startIndex, orderedItems.get(i));
                    }
                    // Add filler items in 45-53 slots
                    for (int i = 45; i <= 53; i++) {
                        pageInventory.setItem(i, adminFillerItem);
                    }

                    // Add back button
                    ItemStack backButton = getIcon("messages.back",
                            config.getString("heads.back"),
                            "ADMIN_SHOPS_PAGE", null);
                    backButton = setCurrentPage(backButton, "ADMIN_ITEMS_LIST");
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

                    adminItemsList.add(pageInventory);
                }

                debugLog("Created " + totalPages + " pages of admin items");

                lastUpdateTime = System.currentTimeMillis();
                debugLog(lastUpdateTime + " " + System.currentTimeMillis());
            }
        }.runTaskAsynchronously(plugin);
    }




}
