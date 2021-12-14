package dev.MrFlyn.shopkeeperNavAddon.globalshopgui;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
}
