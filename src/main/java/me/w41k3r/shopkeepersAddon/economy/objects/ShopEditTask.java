package me.w41k3r.shopkeepersAddon.economy.objects;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.economy.EconomyManager.getCurrencyItem;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.isEconomyItem;
import static me.w41k3r.shopkeepersAddon.gui.managers.Utils.removeEconomyItem;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.blacklistedItems;

public class ShopEditTask implements Listener, PriceInputCallback {

    private final Player player;

    private final Shopkeeper shopkeeper;

    static int page = 1;


    public ShopEditTask(Player player, Shopkeeper shopkeeper) {
        this.player = player;
        this.shopkeeper = shopkeeper;

    }

    public void startEdit(){
        debugLog("Blacklisted items: " + blacklistedItems);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEditShop(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)
                || event.getClickedInventory() == null
        ){
            return;
        }

        if(event.getCurrentItem() != null && blacklistedItems.contains(event.getCursor().getType().name()) && shopkeeper instanceof PlayerShopkeeper){
            sendPlayerMessage(player, config.getString("messages.blacklistedItem", "This item is blacklisted!"));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        event.getClickedInventory().setItem(event.getSlot(), new ItemStack( Material.BARRIER));
                    } , 1
                    );
            event.setCancelled(true);
            return;
        }


        if (isEconomyItem(event.getCurrentItem())){
            debugLog(event.getCursor().toString());
            if (event.getCursor() != null && !event.getCursor().getType().isAir()){
                event.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    event.getClickedInventory().setItem(event.getSlot(), event.getCursor());
                    player.getItemOnCursor().setAmount(0);

                }, 1);
                return;
            }
            page = event.getClickedInventory().getItem(31).getAmount();
            SetPriceTask setPriceTask = new SetPriceTask(player, event.getSlot(), this);
            setPriceTask.startEdit();
            sendPlayerMessage(player,config.getString("messages.setPrice"));
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
        ItemStack item = getCurrencyItem(1.0, slot < 9);
        inventory.setItem(slot, item);
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

    @Override
    public void onPriceSet(double price, int rawSlot) {
        sendPlayerMessage(player,config.getString("messages.priceChanged").replace("%price%", String.valueOf(price)));
        shopkeeper.openEditorWindow(player);
        while (player.getOpenInventory().getTopInventory().getItem(31).getAmount() != page) {
            simulateClick(player, player.getOpenInventory().getTopInventory(), 35);
        }
        ItemStack priceItem = getCurrencyItem(price, rawSlot < 9);
        player.getOpenInventory().getTopInventory().setItem(rawSlot, priceItem);
    }


    public void simulateClick(Player player, Inventory inventory, int slot) {
        ItemStack item = inventory.getItem(slot);

        if (item != null) {
            InventoryView view = player.getOpenInventory();
            InventoryClickEvent clickEvent = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, ClickType.LEFT, null);
            Bukkit.getPluginManager().callEvent(clickEvent);

        }
    }
}
