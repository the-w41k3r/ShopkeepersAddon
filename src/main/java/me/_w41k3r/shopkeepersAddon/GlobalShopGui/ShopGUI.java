package me._w41k3r.shopkeepersAddon.GlobalShopGui;



import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import me._w41k3r.shopkeepersAddon.InvUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class ShopGUI {
    public static void openShopGUI(Player p, MenuType mt)
    {
        Inventory inv;
        switch (mt)
        {
            //11,15
            case MAIN_MENU:
                ShopInv holder = new ShopInv(MenuType.MAIN_MENU, 27, "§8Main Menu");
                inv = holder.getInventory();
                inv.setItem(11, InvUtils.customPlayerHead(
                        PlayerHeadSkins.PLAYER_SHOP_MAIN_MENU.toString(),
                        Arrays.asList(""),
                        "Player Shop"
                ));
                inv.setItem(13, InvUtils.customPlayerHead(
                        PlayerHeadSkins.ADMIN_SHOP_MAIN_MENU.toString(),
                        Arrays.asList(""),
                        "Admin Shop"
                ));
                inv.setItem(15, InvUtils.customPlayerHead(
                        PlayerHeadSkins.ITEM_SHOP_MAIN_MENU.toString(),
                        Arrays.asList(""),
                        "Item Shop"
                ));
                for (int i=0 ;i<inv.getSize() ;i++){
                    if(inv.getItem(i)==null)
                        inv.setItem(i,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                }
                p.openInventory(inv);
                break;
            case PLAYER_SHOPS:
                ShopInv holder4 = new ShopInv(MenuType.MAIN_MENU, 27, "§8Player Shops");
                inv = holder4.getInventory();
                inv.setItem(11, InvUtils.customPlayerHead(
                        PlayerHeadSkins.PLAYER_SHOP_MAIN_MENU.toString(),
                        Arrays.asList(""),
                        "Player Shop"
                ));
                inv.setItem(15, InvUtils.customPlayerHead(
                        PlayerHeadSkins.ITEM_SHOP_MAIN_MENU.toString(),
                        Arrays.asList(""),
                        "Item Shop"
                ));
                for (int i=0 ;i<inv.getSize() ;i++){
                    if(inv.getItem(i)==null)
                        inv.setItem(i,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                }
                p.openInventory(inv);
                break;
            case ITEM_SHOP:
                ShopInv holder1 = new ShopInv(MenuType.ITEM_SHOP, 54, "§8ITEM SHOP");
                inv = holder1.getInventory();
                List<ItemStack> SortedItems = ShopkeeperSorter.getSortedResultItemStacks(p);
//                SortedItems.sort(new ItemStackComparator());

                    (new BukkitRunnable() {
                        @Override
                        public void run() {
                                holder1.setCachedPageItems(SortedItems);
                                if (SortedItems.size() <= 36) {
                                    inv.setItem(51, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                                } else {
                                    inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                                }
                                inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, "Main Menu"));
                                inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, 1, "Page 1", null));
                                int c = 0;
                                for (int i = 0; i < 54; i++) {
                                    if (i < 9) {
                                        inv.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

                                    } else if (i < 45 && !SortedItems.isEmpty() && c < SortedItems.size()) {
                                        inv.setItem(i, SortedItems.get(c));
                                        c++;
                                    } else if (i > 44) {
                                        if (inv.getItem(i) == null) {
                                            inv.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                                        }
                                    }

                                }
                                p.openInventory(inv);
                        }

                    }).runTask(ShopkeepersPlugin.getInstance());

                break;
            case REMOTE_ADMIN_SHOP:
                ShopInv holder5 = new ShopInv(MenuType.REMOTE_ADMIN_SHOP, 54, "§8Remote ADMIN SHOP");
                inv = holder5.getInventory();
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<ItemStack> items = ShopkeeperSorter.getVisualRepresentationOfShopkeepers(ShopkeeperSorter.getAllAdminShopkeepers(), p);
                        holder5.setCachedPageItems(items);
                        if (items.size() <= 36) {
                            inv.setItem(51, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                        } else {
                            inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                        }
//                        inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, "Main Menu"));
                        inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, 1, "Page 1", null));
                        int c = 0;
                        for (int i = 0; i < 54; i++) {
                            if (i < 9) {
                                inv.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

                            } else if (i < 45 && !items.isEmpty() && c < items.size()) {
                                inv.setItem(i, items.get(c));
                                c++;
                            } else if (i > 44) {
                                if (inv.getItem(i) == null) {
                                    inv.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                                }
                            }

                        }
                        p.openInventory(inv);
                    }
                }).runTask(ShopkeepersPlugin.getInstance());
                break;
            case ADMIN_SHOP:
                ShopInv holder2 = new ShopInv(MenuType.ADMIN_SHOP, 54, "§8ADMIN SHOP");
                inv = holder2.getInventory();
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<ItemStack> items = ShopkeeperSorter.getVisualRepresentationOfShopkeepers(ShopkeeperSorter.getAllAdminShopkeepers(), p);
                        holder2.setCachedPageItems(items);
                        if (items.size() <= 36) {
                            inv.setItem(51, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                        } else {
                            inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                        }
                        inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, "Main Menu"));
                        inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, 1, "Page 1", null));
                        int c = 0;
                        for (int i = 0; i < 54; i++) {
                            if (i < 9) {
                                inv.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

                            } else if (i < 45 && !items.isEmpty() && c < items.size()) {
                                inv.setItem(i, items.get(c));
                                c++;
                            } else if (i > 44) {
                                if (inv.getItem(i) == null) {
                                    inv.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                                }
                            }

                        }
                        p.openInventory(inv);
                    }
                }).runTask(ShopkeepersPlugin.getInstance());
                break;
            case PLAYER_SHOP:
                ShopInv holder3 = new ShopInv(MenuType.PLAYER_SHOP, 54, "§8PLAYER SHOP");
                inv = holder3.getInventory();
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<ItemStack> items = ShopkeeperSorter.getSortedVisualsOfStringList(ShopkeeperSorter.getAllShopOwners());
                        holder3.setCachedPageItems(items);
                        if (items.size() <= 36) {
                            inv.setItem(51, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                        } else {
                            inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                        }
                        inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, "Main Menu"));
                        inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, 1, "Page 1", null));
                        int c = 0;
                        for (int i = 0; i < 54; i++) {
                            if (i < 9) {
                                inv.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

                            } else if (i < 45 && !items.isEmpty() && c < items.size()) {
                                inv.setItem(i, items.get(c));
                                c++;
                            } else if (i > 44) {
                                if (inv.getItem(i) == null) {
                                    inv.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                                }
                            }

                        }
                        p.openInventory(inv);
                    }
                }).runTask(ShopkeepersPlugin.getInstance());
                break;




        }
    }
    public static void openShopGUI(Player p, MenuType mt, MenuType previousMenu, ItemStack item)
    {
        Inventory inv;
        switch (mt)
        {
            case SHOPKEEPERS:
                ShopInv holder1 = new ShopInv(MenuType.SHOPKEEPERS, 54, "§8SHOPKEEPERS");
                holder1.setPreviousMenuType(previousMenu);
                inv = holder1.getInventory();
                List<Shopkeeper> shopkeepers = ShopkeeperSorter.getShopkeepersSellingItemStack(item, p);
                (new BukkitRunnable(){
                    @Override
                    public void run() {
                        List<ItemStack> items = ShopkeeperSorter.getVisualRepresentationOfShopkeepers(shopkeepers, p, item);
                        holder1.setCachedPageItems(items);
                        if(items.size()<=36){
                            inv.setItem(51,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                        }
                        else {
                            inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                        }
                        inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, previousMenu.toString()));
                        inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, 1, "Page 1", null));
                        int c=0;
                        for(int i=0;i<54;i++){
                            if(i<9){
                                inv.setItem(i,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

                            }
                            else if(i < 45 && !items.isEmpty() && c < items.size()){
                                inv.setItem(i, items.get(c));
                                c++;
                            }
                            else if(i>44){
                                if(inv.getItem(i)==null){
                                    inv.setItem(i,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                                }
                            }

                        }
                        p.openInventory(inv);
                    }
                }).runTask(ShopkeepersPlugin.getInstance());


        }
    }
    public static void openShopGUI(Player p, MenuType mt, MenuType previousMenu, String ownerName)
    {
        Inventory inv;
        switch (mt)
        {
            case SHOPKEEPERS:
                ShopInv holder1 = new ShopInv(MenuType.SHOPKEEPERS, 54, "§8SHOPKEEPERS");
                holder1.setPreviousMenuType(previousMenu);
                inv = holder1.getInventory();
                List<Shopkeeper> shopkeepers = ShopkeeperSorter.getShopkeepersOwnedByPlayer(ownerName);
                (new BukkitRunnable(){
                    @Override
                    public void run() {
                        List<ItemStack> items = ShopkeeperSorter.getVisualRepresentationOfShopkeepers(shopkeepers, p);
                        holder1.setCachedPageItems(items);
                        if(items.size()<=36){
                            inv.setItem(51,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                        }
                        else {
                            inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                        }
                        inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, previousMenu.toString()));
                        inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, 1, "Page 1", null));
                        int c=0;
                        for(int i=0;i<54;i++){
                            if(i<9){
                                inv.setItem(i,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

                            }
                            else if(i < 45 && !items.isEmpty() && c < items.size()){
                                inv.setItem(i, items.get(c));
                                c++;
                            }
                            else if(i>44){
                                if(inv.getItem(i)==null){
                                    inv.setItem(i,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                                }
                            }

                        }
                        p.openInventory(inv);
                    }
                }).runTask(ShopkeepersPlugin.getInstance());


        }
    }
}
