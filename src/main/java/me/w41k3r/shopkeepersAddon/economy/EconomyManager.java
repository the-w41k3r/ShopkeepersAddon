package me.w41k3r.shopkeepersAddon.economy;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.setPrice;

public class EconomyManager {



    public static ItemStack getCurrencyItem(double price, boolean isBuyItem) {
        String type = isBuyItem ? "buy-item" : "sell-item";
        Material material = Material.valueOf(config.getString("economy." + type + ".material"));
        ItemStack moneyItem = new ItemStack(material);
        ItemMeta meta = moneyItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(config.getString("economy." + type + ".name").replace("%price%", String.valueOf(price)));
            List<String> lore = config.getStringList("economy." + type + ".lore");
            meta.setLore(lore);
            meta = setPrice(meta, price);

            if (config.getBoolean("economy." + type + ".glow")) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            if (config.getInt("economy." + type + ".custom-model-data") > 0) {
                meta.setCustomModelData(config.getInt("economy." + type + ".custom-model-data"));
            }


            moneyItem.setItemMeta(meta);
        }

        return moneyItem;
    }

    public static boolean hasMoney(Player player, double amount){
        if (Money.getBalance(player) < amount){
            debugLog("Not enough money!" + amount);
            return false;
        }
        return true;
    }

}
