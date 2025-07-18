package me.w41k3r.shopkeepersAddon.gui.managers;

import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.trade.TradingPlayerShopkeeper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.plugin;
import static me.w41k3r.shopkeepersAddon.gui.init.InitAdminPages.createAdminItemsList;
import static me.w41k3r.shopkeepersAddon.gui.init.InitAdminPages.createAdminShopsList;
import static me.w41k3r.shopkeepersAddon.gui.init.InitPlayerPages.createPlayerItemsList;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.gui.init.InitPlayerPages.createPlayerShopsList;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;

public class FetchShopkeepersManager {

    public static ArrayList<AdminShopkeeper> adminShopkeepers = new ArrayList<>();
    public static ArrayList<PlayerShopkeeper> playerShopkeepers = new ArrayList<>();
    public static HashSet<ItemStack> adminShopItems = new HashSet<>();
    public static HashSet<ItemStack> playerShopItems = new HashSet<>();


    /**
     * Fetches all shopkeepers from the ShopkeepersAPI and categorizes them into admin and player shops.
     * It also collects items from each shopkeeper's trading recipes into respective sets.
     */
    public static void fetchShopkeepers() {
        // First, gather all shopkeepers on the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                // Clear previous data
                adminShopkeepers.clear();
                playerShopkeepers.clear();
                adminShopItems.clear();
                playerShopItems.clear();

                // Gather shopkeepers
                registry.getAllShopkeepers().forEach(shopkeeper -> {
                    if (shopkeeper instanceof AdminShopkeeper) {
                        debugLog("Admin shop: " + shopkeeper.getName() + " ID: " + shopkeeper.getId());
                        adminShopkeepers.add((AdminShopkeeper) shopkeeper);
                    } else if (shopkeeper instanceof TradingPlayerShopkeeper) {
                        debugLog("Player shop: " + shopkeeper.getName() + " ID: " + shopkeeper.getId());
                        playerShopkeepers.add((PlayerShopkeeper) shopkeeper);
                    } else {
                        debugLog("Ignoring shop: " + shopkeeper.getName() + " ID: " + shopkeeper.getId());
                    }
                });

                // Now process admin shops on the main thread
                processAdminShops();

                // And process player shops on the main thread
                processPlayerShops();
            }
        }.runTask(plugin); // Run on the main thread
    }

    private static void processAdminShops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (AdminShopkeeper adminShopkeeper : adminShopkeepers) {
                    for (TradingRecipe recipe : adminShopkeeper.getTradingRecipes(null)) {
                        ItemStack resultItem = recipe.getResultItem().copy();
                        resultItem.setAmount(1);
                        adminShopItems.add(resultItem);
                        addAdminOwner(resultItem, adminShopkeeper.getUniqueId());
                    }
                }
                debugLog("Fetched " + adminShopItems.size() + " items from admin shops.");

                // Run GUI updates on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        createAdminShopsList();
                        createAdminItemsList();
                    }
                }.runTask(plugin);
            }
        }.runTask(plugin); // Run on the main thread
    }

    private static void processPlayerShops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (PlayerShopkeeper playerShopkeeper : playerShopkeepers) {
                    for (TradingRecipe recipe : playerShopkeeper.getTradingRecipes(null)) {
                        ItemStack resultItem = recipe.getResultItem().copy();
                        resultItem.setAmount(1);
                        playerShopItems.add(resultItem);
                        addPlayerOwner(resultItem, playerShopkeeper.getUniqueId());
                    }
                }
                debugLog("Fetched " + playerShopItems.size() + " items from player shops.");

                // Run GUI updates on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        createPlayerShopsList();
                        createPlayerItemsList();
                    }
                }.runTask(plugin);
            }
        }.runTask(plugin); // Run on the main thread
    }

    public static void addPlayerOwner(ItemStack item, UUID ownerUUID) {
        if(!playerItemOwners.containsKey(item)) {
            playerItemOwners.put(item, new ArrayList<>());
        }
        playerItemOwners.get(item).add(ownerUUID);
    }

    public static void addAdminOwner(ItemStack item, UUID ownerUUID) {
        if(!adminItemOwners.containsKey(item)) {
            adminItemOwners.put(item, new ArrayList<>());
        }
        adminItemOwners.get(item).add(ownerUUID);
    }





}
