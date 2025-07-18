package me.w41k3r.shopkeepersAddon.gui.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.plugin;

public class PersistentGUIDataManager {
    // This class is intended to handle persistent data storage and retrieval.
    // Currently, it does not contain any methods or fields.
    // Future implementations may include:
    // - Saving and loading player data
    // - Managing shopkeeper states
    // - Handling inventory persistence

    public static String getTarget(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "target"), PersistentDataType.STRING);
    }

    public static ItemStack setTarget(ItemStack item, String value) {
        debugLog("Setting target for item: " + item.getItemMeta().getDisplayName() + " to " + value);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "target"), PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack setCurrentPage(ItemStack item, String value) {
        debugLog("Setting current page for item: " + item.getItemMeta().getDisplayName() + " to " + value);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "currentPage"), PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }
    public static String getCurrentPage(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "currentPage"), PersistentDataType.STRING);
    }

    public static Integer getPageNumber(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "pageNumber"), PersistentDataType.INTEGER);
    }
    public static ItemStack setPageNumber(ItemStack item, Integer value) {
        debugLog("Setting page number for item: " + item.getItemMeta().getDisplayName() + " to " + value);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pageNumber"), PersistentDataType.INTEGER, value);
        item.setItemMeta(meta);
        return item;
    }


}
