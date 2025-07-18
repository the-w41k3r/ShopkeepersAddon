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
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.ItemsListHandlers.openItemOwnersList;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.*;

public class BaseGUIHandlers {

    // In handleBackButton method
    private static void handleBackButton(Player player, ItemStack button) {
        debugLog(player.getName() + " clicked back button");
        openInventory(getTarget(button), player, -1);
    }

    // In handleNavigationButton method
    private static void handleNavigationButton(Player player, InventoryClickEvent event) {
        debugLog(player.getName() + " clicked previous/next button");
        ItemStack backButton = event.getInventory().getItem(BACK_BUTTON_SLOT);
        ItemStack navButton = event.getCurrentItem();
        openInventory(getCurrentPage(backButton), player, getPageNumber(navButton));
    }

    // In OpenGUI method
    public static void OpenGUI(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem().clone();
        if (clickedItem == null) return;

        int slot = event.getSlot();

        if (slot == BACK_BUTTON_SLOT) {
            handleBackButton(player, clickedItem);
        } else if (slot == PREV_BUTTON_SLOT || slot == NEXT_BUTTON_SLOT) {
            handleNavigationButton(player, event);
        } else if( event.getClickedInventory().getSize() == 54
                && getCurrentPage(event.getClickedInventory().getItem(BACK_BUTTON_SLOT)).endsWith("_ITEMS_LIST")
        ){
            openItemOwnersList(clickedItem, player, getCurrentPage(event.getClickedInventory().getItem(BACK_BUTTON_SLOT)));
            debugLog("getCurrentPage: " + getCurrentPage(event.getClickedInventory().getItem(BACK_BUTTON_SLOT)));
        }
        else {
            debugLog("Opening inventory for item: " + clickedItem.getType());
            openInventory(getTarget(clickedItem), player, -1);
        }
    }

    public static void openInventory(String guiName, Player player, int page) {
        if (guiName == null) {
            debugLog("Empty GUI name!");
            return;
        }
        GUITypes gui = GUITypes.valueOf(guiName);

        debugLog("Opening " + gui + " for " + player.getName() + (page >= 0 ? " on page: " + page : ""));

        // For single-page GUIs
        if (page == -1) {
            Inventory singlePageInventory = gui.getInventory();
            debugLog("Single page inventory: " + singlePageInventory);
            if (singlePageInventory != null) {
                player.openInventory(singlePageInventory);
                return;
            }
        }

        // For multipage GUIs
        ArrayList<Inventory> inventoryList = gui.getInventoryList();

        if (inventoryList != null && !inventoryList.isEmpty()) {
            int pageToOpen = page >= 0 ? page : 0;
            if (pageToOpen < inventoryList.size()) {
                player.openInventory(inventoryList.get(pageToOpen));
                return;
            }
        }

        sendPlayerMessage(player, config.getString("messages.playerShopsPage.noShops", "No shops available."));
    }

    public static ItemStack getFillerItem(String configPath) {
        Material material = Material.valueOf(config.getString(configPath + ".material", "BLACK_STAINED_GLASS_PANE"));
        ItemStack fillerItem = new ItemStack(material);
        ItemMeta meta = fillerItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            fillerItem.setItemMeta(meta);
        }
        fillerItem = setTarget(fillerItem, "ignore");
        return fillerItem;
    }

    public static void closeAllUIs() {
        debugLog("Closing all open inventories for online players.");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    Inventory inv = player.getOpenInventory().getTopInventory();
                    if (inv != null && inv.getHolder() instanceof VirtualInventoryOwner) {
                        player.closeInventory();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);

    }
}