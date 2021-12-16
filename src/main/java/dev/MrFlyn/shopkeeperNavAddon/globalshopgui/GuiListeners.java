package dev.MrFlyn.shopkeeperNavAddon.globalshopgui;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.events.ShopkeeperEditedEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperRemoveEvent;
import dev.MrFlyn.shopkeeperNavAddon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiListeners implements Listener {
    public boolean firstJoin = true;
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(firstJoin){
            ShopkeeperSorter.updateSellingItemStacks(e.getPlayer());
            ShopkeeperSorter.updateVisualRepresentationOfShopkeepers(e.getPlayer());
            ShopkeeperSorter.updateShopkeeperItemstacks(e.getPlayer());
            firstJoin = false;
        }
    }@EventHandler
    public void onShopkeeperEdit(ShopkeeperEditedEvent e){
            ShopkeeperSorter.updateSellingItemStacks(e.getPlayer());
            ShopkeeperSorter.updateVisualRepresentationOfShopkeepers(e.getPlayer());
            ShopkeeperSorter.updateShopkeeperItemstacks(e.getPlayer());
    }@EventHandler
    public void onShopkeeperDelete(ShopkeeperRemoveEvent e){
        if(Bukkit.getServer().getOnlinePlayers().size()>0) {
            Player p = Bukkit.getServer().getOnlinePlayers().stream().findAny().get();
            ShopkeeperSorter.updateSellingItemStacks(p);
            ShopkeeperSorter.updateVisualRepresentationOfShopkeepers(p);
            ShopkeeperSorter.updateShopkeeperItemstacks(p);
        }
        firstJoin = true;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(e.getClickedInventory() == null)
        {

            return;
        }
        if(e.getClickedInventory().getHolder() == null)
        {
            return;
        }
        if(e.getCurrentItem() == null)
        {
            return;
        }
        if(e.getClickedInventory().getHolder() instanceof ShopInv){
            ShopInv holder = (ShopInv) e.getClickedInventory().getHolder();
            e.setCancelled(true);
            switch (holder.getMenuType()){
                case MAIN_MENU:
                    if(e.getCurrentItem().getType() != Material.PLAYER_HEAD) {
                        return;
                    }
                    if(e.getCurrentItem().getItemMeta().getDisplayName().equals("Item Shop")){
                        (new BukkitRunnable(){
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.ITEM_SHOP);
                            }
                        }).runTask(SKShopkeepersPlugin.getInstance());
                        return;
                    }
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Admin Shop")) {
                        (new BukkitRunnable() {
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.ADMIN_SHOP);
                            }
                        }).runTask(SKShopkeepersPlugin.getInstance());
                        return;
                    }
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Player Shop")) {
                        (new BukkitRunnable() {
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.PLAYER_SHOP);
                            }
                        }).runTask(SKShopkeepersPlugin.getInstance());
                        return;
                    }
                    break;
                case PLAYER_SHOP:
                    if(e.getCurrentItem().getType() != Material.PLAYER_HEAD) {
                        return;
                    }
                    if (e.getSlot() == 51) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.PLAYER_SHOP);
                                }
                            }).runTask(SKShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.PLAYER_SHOP);
                                }
                            }).runTask(SKShopkeepersPlugin.getInstance());
                        }
                    }
                    else {
                        (new BukkitRunnable(){
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.SHOPKEEPERS, MenuType.PLAYER_SHOP,
                                        e.getCurrentItem().getItemMeta().getDisplayName());
                            }
                        }).runTask(SKShopkeepersPlugin.getInstance());
                    }
                    break;
                case ITEM_SHOP:
                    if (e.getSlot() == 51) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.ITEM_SHOP);
                                }
                            }).runTask(SKShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.ITEM_SHOP);
                                }
                            }).runTask(SKShopkeepersPlugin.getInstance());
                        }
                    }
                    else if (e.getSlot()>8&&e.getSlot()<45) {
                        (new BukkitRunnable(){
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.SHOPKEEPERS, MenuType.ITEM_SHOP, e.getCurrentItem());
                            }
                        }).runTask(SKShopkeepersPlugin.getInstance());
                    }
                    break;
                case SHOPKEEPERS:
                    if (e.getSlot() == 51) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD){
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.SHOPKEEPERS);
                                }
                            }).runTask(SKShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Item Shop")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.ITEM_SHOP);
                                return;
                            }if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Player Shop")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.PLAYER_SHOP);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.SHOPKEEPERS);
                                }
                            }).runTask(SKShopkeepersPlugin.getInstance());
                        }
                    }
                    else {
                        NamespacedKey key = new NamespacedKey(Main.plugin, "shopLocation");
                        ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
                        CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();

                        if(tagContainer.hasCustomTag(key , ItemTagType.STRING)) {
                            String locString = tagContainer.getCustomTag(key, ItemTagType.STRING);
                            Location loc = new Location(Bukkit.getWorld(locString.split(" ")[0]), Double.parseDouble(locString.split(" ")[1]),
                                    Double.parseDouble(locString.split(" ")[2]), Double.parseDouble(locString.split(" ")[3]));
                            if(Main.plugin.hooks==null){
                                e.getWhoClicked().teleport(loc);
                            }
                            else {
                                (new BukkitRunnable(){
                                    @Override
                                    public void run() {

                                            Main.plugin.hooks.teleportPlayerPlotSquared((Player) e.getWhoClicked(), loc);
                                    }
                                }).runTask(Main.plugin);
                            }
                        }
                    }
                    break;
                case ADMIN_SHOP:
                    if (e.getSlot() == 51) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD){
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.ADMIN_SHOP);
                                }
                            }).runTask(SKShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.ADMIN_SHOP);
                                }
                            }).runTask(SKShopkeepersPlugin.getInstance());
                        }
                    }
                    else {
                        NamespacedKey key = new NamespacedKey(Main.plugin, "shopLocation");
                        ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
                        CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();

                        if (tagContainer.hasCustomTag(key, ItemTagType.STRING)) {
                            String locString = tagContainer.getCustomTag(key, ItemTagType.STRING);
                            Location loc = new Location(Bukkit.getWorld(locString.split(" ")[0]), Double.parseDouble(locString.split(" ")[1]),
                                    Double.parseDouble(locString.split(" ")[2]), Double.parseDouble(locString.split(" ")[3]));
                            if (Main.plugin.hooks == null) {
                                e.getWhoClicked().teleport(loc);
                            } else {
                                (new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Main.plugin.hooks.teleportPlayerPlotSquared((Player) e.getWhoClicked(), loc);
                                    }
                                }).runTask(Main.plugin);
                            }
                        }
                    }
                    break;

            }
        }
    }
}
