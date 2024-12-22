package me.w41k3r.shopkeepersaddon.Economy;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static me.w41k3r.shopkeepersaddon.General.Utils.*;
import static me.w41k3r.shopkeepersaddon.Main.*;

public class EcoUtils {

    static boolean hasMoney(Player player, double amount){
        if (Money.getBalance(player) < amount){
            debugLog("Not enough money!" + amount);
            return false;
        }
        return true;
    }


    static ItemStack getCurrencyItem(double price, boolean isBuyItem) {
        String type = isBuyItem ? "buy-item" : "sell-item";
        Material material = Material.valueOf(plugin.getSettingString("economy." + type + ".material"));
        ItemStack moneyItem = new ItemStack(material);
        ItemMeta meta = moneyItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getSettingString("economy." + type + ".name").replace("%price%", String.valueOf(price)));
            List<String> lore = plugin.setting().getStringList("economy." + type + ".lore");
            meta.setLore(lore);
            meta = setPrice(meta, "itemprice", price);

            if (plugin.setting().getBoolean("economy." + type + ".glow")) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            moneyItem.setItemMeta(meta);
        }

        return moneyItem;
    }


    public static void removeEconomyItem(Player player){
        for (ItemStack item : player.getInventory().getContents()){
            if (item != null && hasData(item, "itemprice", PersistentDataType.DOUBLE)){
                player.getInventory().remove(item);
                return;
            }
        }
    }

}
