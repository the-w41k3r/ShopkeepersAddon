package me.w41k3r.shopkeepersAddon.gui.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.plugin;
import static me.w41k3r.shopkeepersAddon.economy.EconomyManager.hasMoney;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.getPrice;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.isEconomyItem;

public class Utils {

    public static void removeEconomyItem(Player player){
        for (ItemStack item : player.getInventory().getContents()){
            if (item != null && isEconomyItem(item)){
                player.getInventory().remove(item);
                return;
            }
        }
    }

    public static void setItemsOnTradeSlots(TradeSelectEvent event, int slot){
        ItemStack toAdd = slot == 0 ? event.getMerchant().getRecipe(event.getIndex()).getIngredients().getFirst() : event.getMerchant().getRecipe(event.getIndex()).getResult();
        if(event.getInventory().getItem(slot) != null){
            event.getWhoClicked().getInventory().addItem(event.getInventory().getItem(slot));
        }
        if (slot == 0 ) {
            for (int i = 1; i <= 64; i++){
                if(!hasMoney((Player) event.getWhoClicked(), getPrice(toAdd)*i)){
                    break;
                }
                toAdd.setAmount(i);
            }
        }

        event.getInventory().setItem(slot, toAdd);
        Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem((Player) event.getWhoClicked()), 1);

    }
}
