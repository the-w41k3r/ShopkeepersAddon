package me.w41k3r.shopkeepersaddon.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

import static me.w41k3r.shopkeepersaddon.Economy.EcoUtils.hasMoney;
import static me.w41k3r.shopkeepersaddon.Economy.EcoUtils.removeEconomyItem;
import static me.w41k3r.shopkeepersaddon.General.Utils.getPrice;
import static me.w41k3r.shopkeepersaddon.General.Utils.hasData;
import static me.w41k3r.shopkeepersaddon.Main.*;

public class EcoUIHandler {

    static void setItemsOnTradeSlots(TradeSelectEvent event, int slot){
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
