package me.w41k3r.shopkeepersAddon.gui.managers;

import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.trade.TradingPlayerShopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.init.InitAdminPages.*;
import static me.w41k3r.shopkeepersAddon.gui.init.InitPlayerPages.*;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;

public class FetchShopkeepersManager {

    // Use thread-safe collections since they're accessed from multiple places
    public static final List<AdminShopkeeper> adminShopkeepers = new ArrayList<>();
    public static final List<PlayerShopkeeper> playerShopkeepers = new ArrayList<>();
    public static final Set<ItemStack> adminShopItems = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static final Set<ItemStack> playerShopItems = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Fetches all shopkeepers from the ShopkeepersAPI and categorizes them into admin and player shops.
     * This must run on the main thread to avoid async access errors.
     */
    public static void fetchShopkeepers() {
        debugLog("Starting shopkeeper fetch process...");

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    long totalStartTime = System.currentTimeMillis();

                    clearPreviousData();
                    gatherShopkeepers();

                    // Process shopkeepers on main thread (required for block access)
                    long processStartTime = System.currentTimeMillis();
                    processShopkeepers();
                    long processTime = System.currentTimeMillis() - processStartTime;

                    debugLog("Shopkeeper processing completed in " + processTime + "ms");

                    // Schedule GUI updates (can be async if needed)
                    scheduleGUIUpdates();

                    long totalTime = System.currentTimeMillis() - totalStartTime;
                    debugLog("Total shopkeeper fetch completed in " + totalTime + "ms");

                } catch (Exception e) {
                    Bukkit.getLogger().severe("Error fetching shopkeepers: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTask(plugin);
    }

    private static void clearPreviousData() {
        adminShopkeepers.clear();
        playerShopkeepers.clear();
        adminShopItems.clear();
        playerShopItems.clear();
        adminItemOwners.clear();
        playerItemOwners.clear();

        debugLog("Cleared previous shopkeeper data");
    }

    private static void gatherShopkeepers() {
        int adminCount = 0;
        int playerCount = 0;

        for (var shopkeeper : registry.getAllShopkeepers()) {
            if (shopkeeper instanceof AdminShopkeeper adminShop) {
                adminShopkeepers.add(adminShop);
                adminCount++;
                debugLog("Admin shop: " + shopkeeper.getName() + " ID: " + shopkeeper.getId());
            } else if (shopkeeper instanceof TradingPlayerShopkeeper playerShop) {
                playerShopkeepers.add(playerShop);
                playerCount++;
                debugLog("Player shop: " + shopkeeper.getName() + " ID: " + shopkeeper.getId());
            } else {
                debugLog("Ignoring shop: " + shopkeeper.getName() + " ID: " + shopkeeper.getId());
            }
        }

        debugLog("Gathered " + adminCount + " admin shops and " + playerCount + " player shops");
    }

    private static void processShopkeepers() {
        // Process admin shops first (usually faster since they don't access containers)
        processAdminShops();

        // Process player shops (this is where the async error was occurring)
        processPlayerShops();
    }

    private static void processAdminShops() {
        long startTime = System.currentTimeMillis();
        int itemsProcessed = 0;
        int shopsProcessed = 0;

        for (AdminShopkeeper adminShopkeeper : adminShopkeepers) {
            try {
                List<TradingRecipe> recipes = (List<TradingRecipe>) adminShopkeeper.getTradingRecipes(null);
                if (recipes.isEmpty()) continue;

                for (TradingRecipe recipe : recipes) {
                    ItemStack resultItem = normalizeItemStack(recipe.getResultItem().copy());
                    if (resultItem != null) {
                        adminShopItems.add(resultItem);
                        addAdminOwner(resultItem, adminShopkeeper.getUniqueId());
                        itemsProcessed++;
                    }
                }
                shopsProcessed++;
            } catch (Exception e) {
                Bukkit.getLogger().warning("Error processing admin shop " + adminShopkeeper.getId() + ": " + e.getMessage());
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        debugLog("Processed " + itemsProcessed + " items from " + shopsProcessed +
                "/" + adminShopkeepers.size() + " admin shops in " + processingTime + "ms");
    }

    private static void processPlayerShops() {
        long startTime = System.currentTimeMillis();
        int itemsProcessed = 0;
        int shopsProcessed = 0;
        int shopsFailed = 0;

        for (PlayerShopkeeper playerShopkeeper : playerShopkeepers) {
            try {
                // This call accesses the container (chest) and must be on main thread
                List<TradingRecipe> recipes = (List<TradingRecipe>) playerShopkeeper.getTradingRecipes(null);
                if (recipes == null || recipes.isEmpty()) {
                    debugLog("No recipes found for player shop " + playerShopkeeper.getId());
                    continue;
                }

                for (TradingRecipe recipe : recipes) {
                    ItemStack resultItem = normalizeItemStack(recipe.getResultItem().copy());
                    if (resultItem != null) {
                        playerShopItems.add(resultItem);
                        addPlayerOwner(resultItem, playerShopkeeper.getUniqueId());
                        itemsProcessed++;
                    }
                }
                shopsProcessed++;

            } catch (IllegalStateException e) {
                // This usually happens when the container is not loaded/accessible
                shopsFailed++;
                debugLog("Could not access container for player shop " + playerShopkeeper.getId() +
                        " (container might be unloaded): " + e.getMessage());
            } catch (Exception e) {
                shopsFailed++;
                Bukkit.getLogger().warning("Error processing player shop " + playerShopkeeper.getId() + ": " + e.getMessage());
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        debugLog("Processed " + itemsProcessed + " items from " + shopsProcessed +
                "/" + playerShopkeepers.size() + " player shops (" + shopsFailed +
                " failed) in " + processingTime + "ms");
    }

    private static ItemStack normalizeItemStack(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        ItemStack normalized = item.clone();
        normalized.setAmount(1);
        return normalized;
    }

    private static void scheduleGUIUpdates() {
        // GUI updates can be async since they only create inventories
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();

                    // Update all GUI pages
                    createAdminShopsList();
                    createAdminItemsList();
                    createPlayerShopsList();
                    createPlayerItemsList();

                    long updateTime = System.currentTimeMillis() - startTime;
                    debugLog("GUI updates completed in " + updateTime + "ms");

                    debugLog("Total items - Admin: " + adminShopItems.size() +
                            ", Player: " + playerShopItems.size());

                } catch (Exception e) {
                    Bukkit.getLogger().severe("Error updating GUI: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTask(plugin);
    }

    public static void addPlayerOwner(ItemStack item, UUID ownerUUID) {
        if (item == null || ownerUUID == null) return;

        playerItemOwners.computeIfAbsent(item, k -> new ArrayList<>()).add(ownerUUID);
    }

    public static void addAdminOwner(ItemStack item, UUID ownerUUID) {
        if (item == null || ownerUUID == null) return;

        adminItemOwners.computeIfAbsent(item, k -> new ArrayList<>()).add(ownerUUID);
    }

    // Utility methods for external access
    public static int getAdminShopCount() {
        return adminShopkeepers.size();
    }

    public static int getPlayerShopCount() {
        return playerShopkeepers.size();
    }

    public static int getAdminItemCount() {
        return adminShopItems.size();
    }

    public static int getPlayerItemCount() {
        return playerShopItems.size();
    }

    // Method to force refresh shopkeepers
    public static void refreshShopkeepers() {
        debugLog("Manual shopkeeper refresh requested");
        fetchShopkeepers();
    }

    // Method to check if processing is safe (for potential async calls)
    public static boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }
}