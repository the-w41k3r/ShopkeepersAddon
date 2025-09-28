package me.w41k3r.shopkeepersAddon.gui;

import me.w41k3r.shopkeepersAddon.gui.models.GUITypes;
import me.w41k3r.shopkeepersAddon.gui.objects.VirtualInventoryOwner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.ItemsListHandlers.openItemOwnersList;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.*;

public class BaseGUIHandlers {

    private static final String IGNORE_TARGET = "ignore";
    private static final int INVALID_PAGE = -1;
    private static final int LARGE_INVENTORY_SIZE = 54;

    public static void handleGUIEvent(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        int slot = event.getSlot();
        Inventory clickedInventory = event.getClickedInventory();

        switch (slot) {
            case BACK_BUTTON_SLOT:
                handleBackButton(player, clickedItem);
                break;
            case PREV_BUTTON_SLOT:
            case NEXT_BUTTON_SLOT:
                handleNavigationButton(player, event);
                break;
            default:
                handleDefaultCase(player, clickedItem, clickedInventory, slot);
                break;
        }
    }

    private static void handleBackButton(Player player, ItemStack button) {
        debugLog(player.getName() + " clicked back button");
        openInventory(getTarget(button), player, INVALID_PAGE);
    }

    private static void handleNavigationButton(Player player, InventoryClickEvent event) {
        debugLog(player.getName() + " clicked navigation button");
        ItemStack backButton = event.getInventory().getItem(BACK_BUTTON_SLOT);
        ItemStack navButton = event.getCurrentItem();
        openInventory(getCurrentPage(backButton), player, getPageNumber(navButton));
    }

    private static void handleDefaultCase(Player player, ItemStack clickedItem, Inventory clickedInventory, int slot) {
        if (isItemsListInventory(clickedInventory)) {
            handleItemsListClick(player, clickedItem, clickedInventory);
        } else {
            debugLog("Opening inventory for item: " + clickedItem.getType());
            openInventory(getTarget(clickedItem), player, INVALID_PAGE);
        }
    }

    private static boolean isItemsListInventory(Inventory inventory) {
        if (inventory == null || inventory.getSize() != LARGE_INVENTORY_SIZE) {
            return false;
        }

        ItemStack backButton = inventory.getItem(BACK_BUTTON_SLOT);
        return backButton != null && getCurrentPage(backButton).endsWith("_ITEMS_LIST");
    }

    private static void handleItemsListClick(Player player, ItemStack clickedItem, Inventory clickedInventory) {
        ItemStack backButton = clickedInventory.getItem(BACK_BUTTON_SLOT);
        String currentPage = getCurrentPage(backButton);
        openItemOwnersList(clickedItem, player, currentPage);
        debugLog("Items list current page: " + currentPage);
    }

    public static void openInventory(String guiName, Player player, int page) {
        if (guiName == null || guiName.trim().isEmpty()) {
            debugLog("Invalid GUI name provided!");
            return;
        }

        try {
            GUITypes gui = GUITypes.valueOf(guiName.toUpperCase());
            openGUIType(gui, player, page);
        } catch (IllegalArgumentException e) {
            debugLog("Invalid GUI type: " + guiName);
            sendPlayerMessage(player, config.getString("messages.invalidGUI", "Invalid menu."));
        }
    }

    private static void openGUIType(GUITypes gui, Player player, int page) {
        debugLog("Opening " + gui + " for " + player.getName() +
                (page >= 0 ? " on page: " + page : ""));

        // Try single-page inventory first
        if (page == INVALID_PAGE) {
            Inventory singlePageInventory = gui.getInventory();
            if (singlePageInventory != null) {
                player.openInventory(singlePageInventory);
                return;
            }
        }

        // Fall back to multi-page inventory
        openMultiPageInventory(gui, player, page);
    }

    private static void openMultiPageInventory(GUITypes gui, Player player, int page) {
        List<Inventory> inventoryList = gui.getInventoryList();

        if (inventoryList == null || inventoryList.isEmpty()) {
            sendNoShopsMessage(player);
            return;
        }

        int pageToOpen = Math.max(0, page);
        if (pageToOpen < inventoryList.size()) {
            player.openInventory(inventoryList.get(pageToOpen));
        } else {
            sendNoShopsMessage(player);
        }
    }

    private static void sendNoShopsMessage(Player player) {
        sendPlayerMessage(player,
                config.getString("messages.playerShopsPage.noShops", "No shops available."));
    }

    public static ItemStack getFillerItem(String configPath) {
        String materialName = config.getString(configPath + ".material", "BLACK_STAINED_GLASS_PANE");
        Material material = getMaterialSafe(materialName);

        ItemStack fillerItem = new ItemStack(material);
        ItemMeta meta = fillerItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(" ");
            fillerItem.setItemMeta(meta);
        }

        return setTarget(fillerItem, IGNORE_TARGET);
    }

    private static Material getMaterialSafe(String materialName) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            debugLog("Invalid material: " + materialName + ", using default: BLACK_STAINED_GLASS_PANE");
            return Material.BLACK_STAINED_GLASS_PANE;
        }
    }

    public static void closeAllUIs() {
        debugLog("Closing all open inventories for online players.");

        new BukkitRunnable() {
            @Override
            public void run() {
                closeVirtualInventories();
            }
        }.runTaskAsynchronously(plugin);
    }

    private static void closeVirtualInventories() {
        List<Player> playersToClose = new ArrayList<>();

        // First collect players to avoid concurrent modification
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (topInventory != null && topInventory.getHolder() instanceof VirtualInventoryOwner) {
                playersToClose.add(player);
            }
        }

        // Close inventories on main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : playersToClose) {
                    try {
                        player.closeInventory();
                        debugLog("Closed inventory for player: " + player.getName());
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.WARNING, "Failed to close inventory for player: " + player.getName(), e);
                    }
                }
            }
        }.runTask(plugin);
    }

    // Utility method for safe GUI opening
    public static void safeOpenInventory(Player player, String guiName) {
        if (player == null || !player.isOnline()) {
            debugLog("Attempted to open GUI for offline or null player");
            return;
        }
        openInventory(guiName, player, INVALID_PAGE);
    }
}