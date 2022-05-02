package me._w41k3r.shopkeepersAddon.GlobalShopGui;

import me._w41k3r.shopkeepersAddon.InvUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;


public class PagingCalculations {
    //calculates from page2 onwards
    public static void nextPage(Inventory inv, Player p, MenuType mt){
        switch (mt){
            case REMOTE_ADMIN_SHOP:
            case PLAYER_SHOP:
            case ADMIN_SHOP:
            case SHOPKEEPERS:
            case ITEM_SHOP:
                ItemStack page = inv.getItem(49);
                int pageNumber = Integer.parseInt(page.getItemMeta().getDisplayName().split(" ")[1]);
                inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, page.getAmount()+1,
                        "Page "+ (pageNumber+1), null));
                ShopInv holder = (ShopInv)inv.getHolder();
                List<ItemStack> cachedItems = holder.getCachedPageItems();
                List<ItemStack> pageItems = cachedItems.subList(pageNumber*36, cachedItems.size()-1);
                if(pageItems.size()<=36){
                    inv.setItem(51,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                }
                else {
                    inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                }
                inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, "Previous Page"));
                for(int i = 0; i<36; i++){
                    if(i<pageItems.size())
                        inv.setItem(i + 9, pageItems.get(i));
                    else
                      inv.setItem(i+9,new ItemStack(Material.BARRIER, 0));

                }
                break;

        }

    }
    public static void previousPage(Inventory inv, Player p, MenuType mt){
        switch (mt){
            case PLAYER_SHOP:
            case ADMIN_SHOP:
            case SHOPKEEPERS:
            case ITEM_SHOP:
                ItemStack page = inv.getItem(49);
                int pageNumber = Integer.parseInt(page.getItemMeta().getDisplayName().split(" ")[1]);
                inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, page.getAmount()-1,
                        "Page "+ (pageNumber-1), null));
                ShopInv holder = (ShopInv)inv.getHolder();
                List<ItemStack> cachedItems = holder.getCachedPageItems();
                List<ItemStack> pageItems = cachedItems.subList((pageNumber-2)*36, cachedItems.size()-1);
                if(pageItems.size()<=36){
                    inv.setItem(51,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                }
                else {
                    inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                }
                if((pageNumber-2)*36==0) {
                    inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, (mt == MenuType.SHOPKEEPERS)?
                            holder.getPreviousMenuType().toString():"Main Menu"));
                }
                else {
                    inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, "Previous Page"));
                }
                for(int i = 0; i<36; i++){
                    if(i<pageItems.size())
                        inv.setItem(i+9,pageItems.get(i));
                    else
                        inv.setItem(i+9,new ItemStack(Material.BARRIER, 0));

                }
                break;
            case REMOTE_ADMIN_SHOP:
                ItemStack page1 = inv.getItem(49);
                int pageNumber1 = Integer.parseInt(page1.getItemMeta().getDisplayName().split(" ")[1]);
                inv.setItem(49, InvUtils.ItemBuilder(Material.PAPER, page1.getAmount()-1,
                        "Page "+ (pageNumber1-1), null));
                ShopInv holder1 = (ShopInv)inv.getHolder();
                List<ItemStack> cachedItems1 = holder1.getCachedPageItems();
                List<ItemStack> pageItems1 = cachedItems1.subList((pageNumber1-2)*36, cachedItems1.size()-1);
                if(pageItems1.size()<=36){
                    inv.setItem(51,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                }
                else {
                    inv.setItem(51, InvUtils.customPlayerHead(PlayerHeadSkins.NEXT_PAGE.toString(), null, "Next Page"));
                }
                if((pageNumber1-2)*36==0) {
                    inv.setItem(47,new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                }
                else {
                    inv.setItem(47, InvUtils.customPlayerHead(PlayerHeadSkins.PREVIOUS_PAGE.toString(), null, "Previous Page"));
                }
                for(int i = 0; i<36; i++){
                    if(i<pageItems1.size())
                        inv.setItem(i+9,pageItems1.get(i));
                    else
                        inv.setItem(i+9,new ItemStack(Material.BARRIER, 0));

                }
                break;
        }

    }
}
