package me.w41k3r.shopkeepersAddon.gui.init;

import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import me.w41k3r.shopkeepersAddon.gui.objects.VirtualInventoryOwner;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.init.InitPlayerPages.createPlayerShopsPage;
import static me.w41k3r.shopkeepersAddon.gui.managers.FetchShopkeepersManager.*;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.setCurrentPage;
import static me.w41k3r.shopkeepersAddon.gui.managers.SkinsManager.getIcon;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;
import static org.bukkit.Bukkit.createInventory;

public class InitAdminPages {

    private static final int HOME_PAGE_SIZE = 27;
    private static final int LARGE_PAGE_SIZE = 54;
    private static final int ITEMS_PER_PAGE = 45;
    private static final int BACK_BUTTON_SLOT = 49;
    private static final int PREV_BUTTON_SLOT = 45;
    private static final int NEXT_BUTTON_SLOT = 53;

    public static void initPages() {
        debugLog("Initializing Admin GUI Pages");
        createHomePage();
        createAdminShopsPage();
        createPlayerShopsPage();
    }

    private static void createHomePage() {
        runAsyncTask(() -> {
            debugLog("Creating Home Page Inventory");
            VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("homePage");
            homePage = createInventory(virtualOwner, HOME_PAGE_SIZE,
                    getConfigString("messages.homePage.title", "Shopkeepers Addon"));

            setupHomePageButtons();
        });
    }

    private static void setupHomePageButtons() {
        homePage.setItem(11, createHomePageButton(
                "messages.homePage.buttons.adminShops",
                "heads.homePage.adminShops",
                "ADMIN_SHOPS_PAGE"
        ));

        homePage.setItem(15, createHomePageButton(
                "messages.homePage.buttons.playerShops",
                "heads.homePage.playerShops",
                "PLAYER_SHOPS_PAGE"
        ));
    }

    private static ItemStack createHomePageButton(String messagePath, String headPath, String target) {
        return getIcon(
                messagePath,
                getConfigString(headPath, ""),
                target,
                null
        );
    }

    private static void createAdminShopsPage() {
        runAsyncTask(() -> {
            debugLog("Creating Admin Shops Page Inventory");
            VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner("adminShopsPage");
            adminShopsPage = createInventory(virtualOwner, HOME_PAGE_SIZE,
                    getConfigString("messages.adminShopsPage.title", "Admin Shops"));

            setupAdminShopsPageButtons();
        });
    }

    private static void setupAdminShopsPageButtons() {
        adminShopsPage.setItem(11, createAdminShopsButton(
                "messages.adminShopsPage.buttons.adminShops",
                "heads.adminShopsPage.adminShops",
                "ADMIN_SHOPS_LIST"
        ));

        adminShopsPage.setItem(15, createAdminShopsButton(
                "messages.adminShopsPage.buttons.adminItems",
                "heads.adminShopsPage.adminItems",
                "ADMIN_ITEMS_LIST"
        ));
    }

    private static ItemStack createAdminShopsButton(String messagePath, String headPath, String target) {
        return getIcon(
                messagePath,
                getConfigString(headPath, ""),
                target,
                null
        );
    }

    public static void createAdminShopsList() {
        runAsyncTask(() -> {
            debugLog("Creating Admin Shops Inventory");
            List<AdminShopkeeper> validShopkeepers = getValidAdminShopkeepers();
            List<Inventory> pages = createPaginatedInventory(
                    validShopkeepers,
                    "messages.adminShops.title",
                    "ADMIN_SHOPS_LIST",
                    "ADMIN_SHOPS_PAGE"
            );

            adminShopsList = (ArrayList<Inventory>) pages;
            updateLastUpdateTime();
            debugLog("Created " + pages.size() + " pages of admin shops");
        });
    }

    public static void createAdminItemsList() {
        runAsyncTask(() -> {
            debugLog("Creating Admin Items Inventory");
            List<ItemStack> orderedItems = new ArrayList<>(adminShopItems);
            List<Inventory> pages = createPaginatedInventory(
                    orderedItems,
                    "messages.adminItems.title",
                    "ADMIN_ITEMS_LIST",
                    "ADMIN_SHOPS_PAGE"
            );

            adminItemsList = (ArrayList<Inventory>) pages;
            updateLastUpdateTime();
            debugLog("Created " + pages.size() + " pages of admin items");
        });
    }

    private static List<AdminShopkeeper> getValidAdminShopkeepers() {
        return adminShopkeepers.stream()
                .filter(shop -> !shop.getTradingRecipes(null).isEmpty())
                .toList();
    }

    private static <T> List<Inventory> createPaginatedInventory(
            List<T> items, String titlePath, String currentPage, String backTarget) {

        int totalPages = calculateTotalPages(items.size());
        List<Inventory> pages = new ArrayList<>(totalPages);

        for (int page = 0; page < totalPages; page++) {
            Inventory pageInventory = createPageInventory(titlePath, page, currentPage);
            populatePageItems(pageInventory, items, page);
            setupNavigationButtons(pageInventory, page, totalPages, backTarget, currentPage);
            pages.add(pageInventory);
        }

        return pages;
    }

    private static Inventory createPageInventory(String titlePath, int page, String ownerSuffix) {
        String title = getConfigString(titlePath, "Admin Page")
                .replace("%page%", String.valueOf(page + 1));

        VirtualInventoryOwner virtualOwner = new VirtualInventoryOwner(ownerSuffix + "_" + page);
        return createInventory(virtualOwner, LARGE_PAGE_SIZE, title);
    }

    private static <T> void populatePageItems(Inventory inventory, List<T> items, int page) {
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min((page + 1) * ITEMS_PER_PAGE, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            T item = items.get(i);
            ItemStack icon = createItemIcon(item);
            inventory.setItem(i - startIndex, icon);
        }

        // Add filler items
        for (int i = ITEMS_PER_PAGE; i < LARGE_PAGE_SIZE; i++) {
            inventory.setItem(i, adminFillerItem);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ItemStack createItemIcon(T item) {
        if (item instanceof AdminShopkeeper shopkeeper) {
            String displayName = shopkeeper.getDisplayName();
            return getIcon(
                    "messages.adminShops.buttons",
                    getConfigString("heads.adminShops.Shops", ""),
                    String.valueOf(shopkeeper.getUniqueId()),
                    displayName
            );
        } else if (item instanceof ItemStack) {
            return (ItemStack) item;
        }
        return new ItemStack(org.bukkit.Material.STONE); // Fallback item
    }

    private static void setupNavigationButtons(Inventory inventory, int currentPage,
                                               int totalPages, String backTarget, String currentPageName) {
        // Back button
        ItemStack backButton = createNavigationButton(
                "messages.back",
                "heads.back",
                backTarget,
                currentPageName
        );
        inventory.setItem(BACK_BUTTON_SLOT, backButton);

        // Previous page button
        if (currentPage > 0) {
            ItemStack prevButton = createNavigationButton(
                    "messages.previousPage",
                    "heads.previousPage",
                    currentPage - 1,
                    null
            );
            inventory.setItem(PREV_BUTTON_SLOT, prevButton);
        }

        // Next page button
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = createNavigationButton(
                    "messages.nextPage",
                    "heads.nextPage",
                    currentPage + 1,
                    null
            );
            inventory.setItem(NEXT_BUTTON_SLOT, nextButton);
        }
    }

    private static ItemStack createNavigationButton(String messagePath, String headPath,
                                                    Object target, String currentPage) {
        ItemStack button = getIcon(
                messagePath,
                getConfigString(headPath, ""),
                String.valueOf(target),
                null
        );

        if (currentPage != null) {
            button = setCurrentPage(button, currentPage);
        }

        return button;
    }

    private static int calculateTotalPages(int itemCount) {
        return (int) Math.ceil((double) itemCount / ITEMS_PER_PAGE);
    }

    private static void updateLastUpdateTime() {
        lastUpdateTime = System.currentTimeMillis();
        debugLog("Last update time: " + lastUpdateTime);
    }

    private static void runAsyncTask(Runnable task) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Error in async GUI initialization task", e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private static String getConfigString(String path, String defaultValue) {
        String value = config.getString(path);
        return value != null ? value : defaultValue;
    }
}