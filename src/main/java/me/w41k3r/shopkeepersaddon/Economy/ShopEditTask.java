package me.w41k3r.shopkeepersaddon.Economy;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;

import static me.w41k3r.shopkeepersaddon.Economy.EcoUtils.getCurrencyItem;
import static me.w41k3r.shopkeepersaddon.Economy.EcoUtils.removeEconomyItem;
import static me.w41k3r.shopkeepersaddon.General.Utils.debugLog;
import static me.w41k3r.shopkeepersaddon.General.Utils.hasData;
import static me.w41k3r.shopkeepersaddon.Main.*;

public class ShopEditTask implements Listener, PriceInputCallback {

    private final Player player;

    private final Shopkeeper shopkeeper;


    public ShopEditTask(Player player, Shopkeeper shopkeeper) {
        this.player = player;
        this.shopkeeper = shopkeeper;
    }

    public void startEdit(){
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEditShop(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)
                || event.getClickedInventory() == null
        ){
            return;
        }

        if (hasData(event.getCurrentItem(), "itemprice", PersistentDataType.DOUBLE) ){

            debugLog(event.getCursor().toString());

            if (event.getCursor() != null && !event.getCursor().getType().isAir()){
                event.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    event.getClickedInventory().setItem(event.getSlot(), event.getCursor());
                    player.getItemOnCursor().setAmount(0);

                }, 1);
                return;
            }
            SetPriceTask setPriceTask = new SetPriceTask(player, event.getSlot(), this);
            setPriceTask.startEdit();
            sendPlayerMessage(player,setting().getString("messages.set-price"));
            player.closeInventory();
            return;
        }


        if (event.getCurrentItem() != null && !event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.GRAY_STAINED_GLASS_PANE)){return;}
        int slot = event.getRawSlot();
        if (slot<9){
            debugLog("Price Item Slot: " + event.getClickedInventory().getItem(slot+18));
            if (event.getClickedInventory().getItem(slot + 18) == null
                    || event.getClickedInventory().getItem(slot + 18).getType().isAir()
                    || event.getClickedInventory().getItem(slot + 18).getType().equals(Material.GRAY_STAINED_GLASS_PANE)
            ){
                if (shopkeeper instanceof PlayerShopkeeper){
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        debugLog("Setting default price item" + event.getClickedInventory().getItem(slot + 18));
                        setDefaultPriceItem(event.getClickedInventory(), slot + 18);
                    }, 2);
                    return;
                }
                debugLog("Setting default price item" + event.getClickedInventory().getItem(slot + 18));
                setDefaultPriceItem(event.getClickedInventory(), slot + 18);
                return;
            }
            return;
        }
        if (slot > 17 && slot < 27){
            debugLog("Price Item Slot: " + event.getClickedInventory().getItem(slot-18));
            if (event.getClickedInventory().getItem(slot - 18) == null
                    || event.getClickedInventory().getItem(slot - 18).getType().isAir()
                    || event.getClickedInventory().getItem(slot - 18).getType().equals(Material.GRAY_STAINED_GLASS_PANE)
            ){
                if (shopkeeper instanceof PlayerShopkeeper){
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        debugLog("Setting default price item" + event.getClickedInventory().getItem(slot - 18));
                        setDefaultPriceItem(event.getClickedInventory(), slot - 18);
                    }, 2);
                    return;
                }
                debugLog("Setting default price item" + event.getClickedInventory().getItem(slot - 18));
                setDefaultPriceItem(event.getClickedInventory(), slot - 18);
            }
        }
    }

    private void setDefaultPriceItem(Inventory inventory, int slot) {
        ItemStack item = getCurrencyItem(1.0);
        inventory.setItem(slot, item);
    }

    @Override
    public void onPriceSet(double price, int slot) {
        sendPlayerMessage(player,setting().getString("messages.price-changed").replace("%price%", String.valueOf(price)));
        shopkeeper.openEditorWindow(player);
        ItemStack priceItem = getCurrencyItem(price);
        player.getOpenInventory().getTopInventory().setItem(slot, priceItem);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!event.getPlayer().equals(player)){
            return;
        }
        debugLog("Inventory closed! " + ShopkeepersAPI.getUIRegistry().getUISession(player));
        if (ShopkeepersAPI.getUIRegistry().getUISession(player) != null){
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem(player), 1);
        }
    }
}
