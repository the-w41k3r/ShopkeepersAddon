package me.w41k3r.shopkeepersaddon.Economy;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.events.ShopkeeperOpenUIEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;

import static me.w41k3r.shopkeepersaddon.Economy.EcoUIHandler.clearCurrencyItems;
import static me.w41k3r.shopkeepersaddon.Economy.EcoUIHandler.setItemsOnTradeSlots;
import static me.w41k3r.shopkeepersaddon.Economy.EcoUtils.hasMoney;
import static me.w41k3r.shopkeepersaddon.General.Utils.*;
import static me.w41k3r.shopkeepersaddon.Main.*;

public class EcoListeners implements Listener {


    @EventHandler
    public void onTradeSelect(TradeSelectEvent event) {
        MerchantRecipe recipe = event.getMerchant().getRecipe(event.getIndex());
        Player player = (Player) event.getWhoClicked();
        if (!hasData(recipe.getIngredients().get(0), "itemprice", PersistentDataType.DOUBLE)
            && !hasData(recipe.getResult(), "itemprice", PersistentDataType.DOUBLE)){
            BukkitScheduler scheduler = player.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                clearCurrencyItems(event.getWhoClicked().getInventory());
            }, 2L);
            return;
        }
        debugLog(recipe.getIngredients().get(0).getItemMeta().toString());



        if(hasData(recipe.getIngredients().get(0), "itemprice", PersistentDataType.DOUBLE)){
            if (!hasMoney(player, getPrice(event.getMerchant().getRecipe(event.getIndex()).getIngredients().getFirst()))){
                sendPlayerMessage(player,setting().getString("messages.no-money"));
                event.setCancelled(true);
                return;
            }
            BukkitScheduler scheduler = player.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                setItemsOnTradeSlots(event, 0);
            }, 5L);
        }
    }

    @EventHandler
    public void onTrade(ShopkeeperTradeEvent event) {
        debugLog("Trading now!");
        TradingRecipe recipe = event.getTradingRecipe();
        Player player = event.getPlayer();
        Shopkeeper shopkeeper = event.getShopkeeper();
        boolean isAdminShopkeeper = shopkeeper instanceof AdminShopkeeper;
        Double price;
        if (hasData(recipe.getItem1().copy(), "itemprice", PersistentDataType.DOUBLE)){
            price = getPrice(recipe.getItem1().copy());
            Money.withdrawPlayer(player, price);
            sendPlayerMessage(player, setting().getString("messages.buy-success"));
            if (!isAdminShopkeeper){
                Money.depositPlayer(((PlayerShopkeeper) shopkeeper).getOwner(), price);
            }
            return;
        }
        if (hasData(recipe.getResultItem().copy(), "itemprice", PersistentDataType.DOUBLE)){
            price = getPrice(recipe.getResultItem().copy());
            if (!isAdminShopkeeper){
                if (!Money.has(((PlayerShopkeeper) shopkeeper).getOwner(), price)){
                    sendPlayerMessage(player, setting().getString("messages.no-money-owner"));
                    event.setCancelled(true);
                    return;
                }
                Money.withdrawPlayer(((PlayerShopkeeper) shopkeeper).getOwner(), price);
            }
            Money.depositPlayer(player, price);
            event.getClickEvent().getClickedInventory().removeItem(recipe.getItem1().copy());
            if (recipe.hasItem2()){
                event.getClickEvent().getClickedInventory().removeItem(recipe.getItem2().copy());
            }
            sendPlayerMessage(player, setting().getString("messages.buy-success"));
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void OpenEditorUI(ShopkeeperOpenUIEvent event){
        if (!(event.getUIType().equals(ShopkeepersAPI.getDefaultUITypes().getEditorUIType()))
                || !plugin.setting().getBoolean("economy.enabled")) {
            return;
        }

        debugLog("Shopkeeper opened editor UI" + event.getUIType());

        ShopEditTask shopEditor= new ShopEditTask(event.getPlayer(), event.getShopkeeper());
        shopEditor.startEdit();
    }

    @EventHandler
    public void InventoryClose(InventoryCloseEvent event){
        clearCurrencyItems(event.getPlayer().getInventory());
    }
}
