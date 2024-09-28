package me.w41k3r.shopkeepersaddon.General;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Objects;

import static me.w41k3r.shopkeepersaddon.General.UIHandler.*;
import static me.w41k3r.shopkeepersaddon.General.Utils.*;

public class Listeners implements Listener {

    // GUI Click Event for UI Navigations
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null
                || event.getCurrentItem() == null
                || !(event.getClickedInventory().getHolder() instanceof VirtualOwner)) {
            return;
        }

        event.setCancelled(true);
        if (hasData(event.getCurrentItem(), "closeInventory", PersistentDataType.STRING)) {
            event.getWhoClicked().closeInventory();
            return;
        }

        ItemStack item = event.getCurrentItem();
        item.setAmount(1);

        if (hasData(event.getCurrentItem(), "shopkeeperID", PersistentDataType.STRING)){
            teleportToShop((Player) event.getWhoClicked(), getData(event.getCurrentItem(), "shopkeeperID"), true);
            event.getWhoClicked().closeInventory();
            return;
        }

        if (hasData(event.getCurrentItem(), "ownerID", PersistentDataType.STRING)){
            teleportToShop((Player) event.getWhoClicked(), getData(event.getCurrentItem(), "ownerID"), false);
            event.getWhoClicked().closeInventory();
            return;
        }


        if (event.getSlot() < 44
                && (playerShopItems.containsKey(item) || adminShopItems.containsKey(item))
        ){
            Inventory toOpen = playerShopItems.containsKey(item) ? playerShopItems.get(item).get(0) : adminShopItems.get(item).get(0);
            event.getWhoClicked().openInventory(toOpen);
        }




        if (event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)){
            if(getShopName(event.getCurrentItem()).equals("null")
            ){
                return;
            }

            if (getShopName(event.getCurrentItem()).equals("NextPage") || getShopName(event.getCurrentItem()).equals("PreviousPage")) {
                int newPage = getShopName(event.getCurrentItem()).equals("NextPage") ? Integer.parseInt(getData(event.getClickedInventory().getItem(49), "currentPage")) + 1 : Integer.parseInt(getData(event.getClickedInventory().getItem(49), "currentPage")) - 1;
                switch (getData(event.getClickedInventory().getItem(49), "inventoryType").toLowerCase()){
                    case "admin-shops":
                        event.getWhoClicked().openInventory(adminShops.get(newPage));
                        break;
                    case "player-shops":
                        event.getWhoClicked().openInventory(playerShops.get(newPage));
                        break;
                    case "admin-items":
                        event.getWhoClicked().openInventory(adminItems.get(newPage));
                        break;
                    case "player-items":
                        event.getWhoClicked().openInventory(playerItems.get(newPage));
                        break;
                    case "admin-sellers":
                        event.getWhoClicked().openInventory(playerShopItems.get(event.getClickedInventory().getItem(47)).get(newPage));
                        break;
                }
            }

            if (!getShopName(event.getCurrentItem()).isEmpty() || !getShopName(event.getCurrentItem()).equalsIgnoreCase("null")) {
                Inventory open;
                switch (getShopName(event.getCurrentItem())) {
                    case "AdminSelUI":
                        debugLog("Opening Admin Section");
                        open = adminShopTypes;
                        break;
                    case "PlayerSelUI":
                        debugLog("Opening Player Section");
                        open = playerShopTypes;
                        break;
                    case "AdminShopsUI":
                        debugLog("Opening Admin Shops");
                        open = adminShops.get(0);
                        break;
                    case "AdminItemsUI":
                        debugLog("Opening Admin Items Shops");
                        open = adminItems.get(0);
                        break;
                    case "PlayerShopsUI":
                        debugLog("Opening Player Shops");
                        open = playerShops.get(0);
                        break;
                    case "PlayerItemsUI":
                        debugLog("Opening Player Items Shops");
                        open = playerItems.get(0);
                        break;
                    default:
                        return;
                }

                try {
                    event.getWhoClicked().openInventory(open);
                } catch (Exception e) {
                    errorLog("No Shops Found");
                }

            }

        }
    }
}
