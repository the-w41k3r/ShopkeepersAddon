package me.w41k3r.shopkeepersaddon.Economy;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static me.w41k3r.shopkeepersaddon.General.Utils.hasData;
import static me.w41k3r.shopkeepersaddon.General.Utils.setPrice;
import static me.w41k3r.shopkeepersaddon.Main.*;

public class EcoUtils {

    static boolean hasMoney(Player player, double amount){
        if (Money.getBalance(player) < amount){
            sendPlayerMessage(player,setting().getString("messages.no-money"));
            return false;
        }
        return true;
    }


    static ItemStack getCurrencyItem(double price){
        ItemStack moneyItem = new ItemStack(Material.valueOf(plugin.setting().getString("economy.item.material")));
        ItemMeta meta = moneyItem.getItemMeta();
        meta.setDisplayName(plugin.setting().getString("economy.item.name").replace("%price%", String.valueOf(price)));
        List<String> lore = plugin.setting().getStringList("economy.item.lore");
        meta.setLore(lore);
        meta = setPrice(meta, "itemprice", price);
        if (plugin.setting().getBoolean("economy.item.glow")){
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        moneyItem.setItemMeta(meta);
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
