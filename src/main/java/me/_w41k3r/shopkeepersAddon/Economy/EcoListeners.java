package me._w41k3r.shopkeepersAddon.Economy;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.events.PlayerOpenUIEvent;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.api.ui.UIType;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import me._w41k3r.shopkeepersAddon.InvUtils;
import me._w41k3r.shopkeepersAddon.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class EcoListeners implements Listener {
    @EventHandler
    public void onUIClick(InventoryClickEvent e){
        EcoHandler.onTrade(e);
        if(e.getClickedInventory()!=null) {
            if (ShopkeepersAPI.getUIRegistry().getUISession((Player) e.getWhoClicked()) != null) {
                Player p = (Player) e.getWhoClicked();
                UIType type = ShopkeepersAPI.getUIRegistry().getUISession(p).getUIType();
                if(type == DefaultUITypes.EDITOR()) {
                    if(e.getClickedInventory() == e.getView().getTopInventory()) {
                        EcoHandler.onTradeSetup(p, e);
                    }
                }
                else if(type == DefaultUITypes.TRADING()){
                    if(e.getCurrentItem()==null)
                        return;
                    if(e.getSlot()<2&& InvUtils.hasPersistentData("ItemPrice", e.getCurrentItem(), PersistentDataType.DOUBLE))
                        e.setCancelled(true);
                }
            }
        }
    }
    //to prevent PriceItem increment on click
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUIClick1(InventoryClickEvent e){
        if (e.getCurrentItem() == null || e.getClickedInventory() == null)
            return;
        if (ShopkeepersAPI.getUIRegistry().getUISession((Player) e.getWhoClicked()) == null)
            return;
        Player p = (Player) e.getWhoClicked();

        if (ShopkeepersAPI.getUIRegistry().getUISession(p).getUIType() == DefaultUITypes.EDITOR()) {
            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                if(Main.plugin.getConfig().getBoolean("EconomyHook.BlockItemBasedTrades")) {
                    if (e.getSlot() > 8 && e.getSlot() < 27) {
                        if (!(InvUtils.hasPersistentData("ItemPrice", e.getCurrentItem(), PersistentDataType.DOUBLE) ||
                                InvUtils.hasPersistentData("ItemPrice", e.getCursor(), PersistentDataType.DOUBLE))) {
//                        e.setCurrentItem(null);
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
                if(ShopkeepersAPI.getUIRegistry().getUISession(p).getShopkeeper() instanceof PlayerShopkeeper) {

                    if (e.getCursor() == null || e.getCursor().getType() == Material.AIR) {
                        if (InvUtils.hasPersistentData("ItemPrice", e.getCurrentItem(), PersistentDataType.DOUBLE)) {
                            e.getCurrentItem().setAmount(1);
                            e.setCancelled(true);
                            p.updateInventory();
                            EcoHandler.onPriceItemClick(p, e.getCurrentItem(), ShopkeepersAPI.getUIRegistry().getUISession(p).getShopkeeper());
//                        e.getInventory().setItem(e.getSlot(), e.getCurrentItem());
                        }

                    }
                }
                else if (ShopkeepersAPI.getUIRegistry().getUISession(p).getShopkeeper() instanceof AdminShopkeeper) {
                    if (e.getCursor() == null || e.getCursor().getType() == Material.AIR) {
                        if (InvUtils.hasPersistentData("ItemPrice", e.getCurrentItem(), PersistentDataType.DOUBLE)) {
                            e.setCancelled(true);
                            p.updateInventory();
                            if(e.getClick() == ClickType.RIGHT) {
                                e.getCurrentItem().setAmount(0);
                                return;
                            }
                            EcoHandler.onPriceItemClick(p, e.getCurrentItem(), ShopkeepersAPI.getUIRegistry().getUISession(p).getShopkeeper());
                        }

                    }
                    else{
                        if (InvUtils.hasPersistentData("ItemPrice", e.getCurrentItem(), PersistentDataType.DOUBLE)) {
                            e.getCurrentItem().setAmount(0);
                        }
                    }
                }


            }
        }

    }
    @EventHandler
    public void onUIOpen(PlayerOpenUIEvent e){
        if(e.getUIType() != DefaultUITypes.TRADING())
            return;
        if(!Main.plugin.getConfig().getBoolean("EconomyHook.Geyser-Compat.Enabled")){
            return;
        }
        Player p = e.getPlayer();
        (new BukkitRunnable(){
            @Override
            public void run() {
                if(p.getName().indexOf(Main.plugin.getConfig().getString("EconomyHook.Geyser-Compat.Name-Prefix"))==0){
                    if(!p.getInventory().addItem(InvUtils.ItemBuilder(Material.valueOf(
                                    Main.plugin.messages.getString("Currency-Item.Material")), 1,
                            Main.plugin.messages.getString("Currency-Item.Name-Format").replace("[amount]",
                                    Main.plugin.vaultHook.formattedMoney(0.0)),
                            Main.plugin.messages.getStringList("Currency-Item.Lore"),
                            "GeyserCompat", 0.0)).isEmpty()){
                        p.closeInventory();
                        p.sendMessage(Main.plugin.messages.getString("Player-Inventory-Full"));
                        return;
                    }
                    p.updateInventory();
                }
            }
        }).runTaskLater(Main.plugin, 2L);


    }
    //detect click on the trade sidebar
    @EventHandler
    public void onTradeSelect(TradeSelectEvent e){

        if (e.getInventory().getSelectedRecipe() != null && e.getInventory().getSelectedRecipe().equals(e.getMerchant().getRecipe(e.getIndex()))) {
            return;
        }
        if (ShopkeepersAPI.getUIRegistry().getUISession((Player) e.getWhoClicked()) == null ||
                !(ShopkeepersAPI.getUIRegistry().getUISession(((Player) e.getWhoClicked()).getPlayer()).getUIType() == DefaultUITypes.TRADING()))
            return;
        MerchantRecipe s1 = e.getMerchant().getRecipe(e.getIndex());

        boolean thirdItem = s1.getIngredients().size() < 2  ? false : InvUtils.hasPersistentData("ItemPrice", s1.getIngredients().get(1), PersistentDataType.DOUBLE);

        if (!(InvUtils.hasPersistentData("ItemPrice", s1.getResult(), PersistentDataType.DOUBLE) ||
                InvUtils.hasPersistentData("ItemPrice", s1.getIngredients().get(0), PersistentDataType.DOUBLE) ||
                thirdItem
        )){
            return;
        }


        (new BukkitRunnable() {
            @Override
            public void run() {
                EcoHandler.onTradeSelect(e);
            }
        }).runTask(Main.plugin);

    }
    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        if(InvUtils.hasPersistentData("GeyserCompat", e.getItemDrop().getItemStack(), PersistentDataType.DOUBLE)){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onInvOpen(InventoryOpenEvent e){
       PlayerInventory inv = e.getPlayer().getInventory();
       for(ItemStack item : inv){
           if(InvUtils.hasPersistentData("ItemPrice", item, PersistentDataType.DOUBLE)||
                   InvUtils.hasPersistentData("GeyserCompat", item, PersistentDataType.DOUBLE))
               item.setAmount(0);
       }
        ((Player) e.getPlayer()).updateInventory();
    }

    @EventHandler
    public void onUIClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (ShopkeepersAPI.getUIRegistry().getUISession(p) == null)
            return;
        (new BukkitRunnable(){
            @Override
            public void run() {
                PlayerInventory inv = p.getInventory();
                for (ItemStack item : inv) {
                    if (InvUtils.hasPersistentData("ItemPrice", item, PersistentDataType.DOUBLE)||
                            InvUtils.hasPersistentData("GeyserCompat", item, PersistentDataType.DOUBLE))
                        item.setAmount(0);
                }
                p.updateInventory();
            }
        }).runTask(Main.plugin);

    }
    @EventHandler
    public void onInvClose(InventoryCloseEvent e){
        if (ShopkeepersAPI.getUIRegistry().getUISession((Player) e.getPlayer()) == null)
            return;
        Player p = (Player) e.getPlayer();
        if (ShopkeepersAPI.getUIRegistry().getUISession(p).getUIType() == DefaultUITypes.TRADING()) {
//            e.setCancelled(true);
            for(ItemStack item : e.getView().getTopInventory()){
                if(InvUtils.hasPersistentData("ItemPrice", item, PersistentDataType.DOUBLE)){
//                    e.getView().getTopInventory().setItem(0, null);
                    item.setAmount(0);
                    p.updateInventory();
                }
            }
        }

    }




    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (PriceChatInput.allPriceChats.containsKey(p)) {
            PriceChatInput.allPriceChats.get(p).setChat(e.getMessage());
            e.setCancelled(true);
        }
    }
}
