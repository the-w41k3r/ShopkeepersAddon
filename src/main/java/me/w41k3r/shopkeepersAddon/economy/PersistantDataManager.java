package me.w41k3r.shopkeepersAddon.economy;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.plugin;

public class PersistantDataManager {

    /*
    * */
    public static boolean isEconomyItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null || item.getItemMeta().getPersistentDataContainer() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "itemprice"), PersistentDataType.DOUBLE);
    }

    public static ItemMeta setPrice(ItemMeta itemMeta, Double value) {
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "itemprice"), PersistentDataType.DOUBLE, value);
        return itemMeta;
    }

    public static Double getPrice(ItemStack item) {
        double price = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "itemprice"), PersistentDataType.DOUBLE);
        debugLog("Price of item: " + price);
        return price;
    }
}
