package me.w41k3r.shopkeepersAddon.gui.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.plugin;

public class PersistentGUIDataManager {

    private static final NamespacedKey TARGET_KEY = new NamespacedKey(plugin, "target");
    private static final NamespacedKey CURRENT_PAGE_KEY = new NamespacedKey(plugin, "currentPage");
    private static final NamespacedKey PAGE_NUMBER_KEY = new NamespacedKey(plugin, "pageNumber");

    public static String getTarget(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(TARGET_KEY, PersistentDataType.STRING);
    }

    public static ItemStack setTarget(ItemStack item, String value) {
        debugLog("Setting target for item: " + item.getItemMeta().getDisplayName() + " to " + value);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(TARGET_KEY, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack setCurrentPage(ItemStack item, String value) {
        debugLog("Setting current page for item: " + item.getItemMeta().getDisplayName() + " to " + value);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(CURRENT_PAGE_KEY, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    public static String getCurrentPage(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(CURRENT_PAGE_KEY, PersistentDataType.STRING);
    }

    public static Integer getPageNumber(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(PAGE_NUMBER_KEY, PersistentDataType.INTEGER);
    }

    public static ItemStack setPageNumber(ItemStack item, Integer value) {
        debugLog("Setting page number for item: " + item.getItemMeta().getDisplayName() + " to " + value);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(PAGE_NUMBER_KEY, PersistentDataType.INTEGER, value);
        item.setItemMeta(meta);
        return item;
    }

}
