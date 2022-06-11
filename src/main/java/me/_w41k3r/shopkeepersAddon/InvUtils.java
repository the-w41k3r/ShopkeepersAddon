package me._w41k3r.shopkeepersAddon;

import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class InvUtils {
    public static ItemStack ItemBuilder(Material m, int amount, String name, List<String> lore) {
        if(lore == null)
            lore = new ArrayList<>();
        ItemStack itemStack = new ItemStack(m, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    public static ItemStack customPlayerHead(String skin, List<String> lore, String name) {
        if(lore == null)
            lore = new ArrayList<>();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        //item = Bukkit.getUnsafe().modifyItemStack(head, dev.MrFlyn.shopkeeperNavAddon.Main.plugin.getConfig().getString("backpack." + String.valueOf(lev) + ".skin"));
        head = Bukkit.getUnsafe().modifyItemStack(head, "{display:{Name:\"{\\\"text\\\":\\\"Pumpkin Bowl\\\"}\"},SkullOwner:{Id:[" + "I;1201296705,1414024019,-1385893868,1321399054" + "],Properties:{textures:[{Value:\"" + skin + "\"}]}}}");
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }
    public static ItemStack ItemBuilder(Material m, int amount, String name, List<String> lore, String persistentDataKey, Double persistentData) {
        if(lore == null)
            lore = new ArrayList<>();
        ItemStack itemStack = new ItemStack(m, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        NamespacedKey key = new NamespacedKey(Main.plugin, persistentDataKey);
        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, persistentData);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack ItemBuilder(Material m, int amount, String name, List<String> lore, String persistentDataKey, Double persistentData,
                                        int CustomModelData) {
        if (lore == null)
            lore = new ArrayList<>();
        ItemStack itemStack = new ItemStack(m, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.setCustomModelData(CustomModelData);
        NamespacedKey key = new NamespacedKey(Main.plugin, persistentDataKey);
        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, persistentData);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    public static ItemStack ItemBuilder(Material m, int amount, String name, List<String> lore, String persistentDataKey, String persistentData) {
        if(lore == null)
            lore = new ArrayList<>();
        ItemStack itemStack = new ItemStack(m, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        NamespacedKey key = new NamespacedKey(Main.plugin, persistentDataKey);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, persistentData);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static boolean hasPersistentData(String key, ItemStack i, PersistentDataType type){
        if(i==null)
            return false;
        if(!i.hasItemMeta())
            return false;
        return i.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Main.plugin, key), type);
    }

    public static boolean hasPersistentData(String key, UnmodifiableItemStack i, PersistentDataType type) {
        if (i == null)
            return false;
        if (!i.hasItemMeta())
            return false;
        return i.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Main.plugin, key), type);
    }

    public static double getPersistentDataPrice(ItemStack i) {
        return i.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "ItemPrice"),PersistentDataType.DOUBLE);
    }

    public static double getPersistentDataPrice(UnmodifiableItemStack i) {
        return i.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "ItemPrice"), PersistentDataType.DOUBLE);
    }
    public static void removeItems(Material var1, int var2, Inventory inv) {
        ItemStack[] var3 = inv.getContents();

        for (int var4 = 0; var4 < var3.length; ++var4) {
            ItemStack var5 = var3[var4];
            if (var5 != null && var5.getType() == var1) {
                if (var5.getAmount() <= var2) {
                    var2 -= var5.getAmount();
                    var3[var4] = null;
                } else {
                    var5.setAmount(var5.getAmount() - var2);
                    var2 = 0;
                }

                if (var2 <= 0) {
                    break;
                }
            }
        }

        inv.setContents(var3);
    }

}
