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

        debugLog("Clicked Slot: " + event.getSlot() + " Clicked Item: " + Objects.requireNonNull(event.getCurrentItem().getItemMeta()).getDisplayName());
        event.setCancelled(true);
        if (hasData(event.getCurrentItem(), "closeInventory", PersistentDataType.STRING)) {
            event.getWhoClicked().closeInventory();
            return;
        }

        ItemStack item = event.getCurrentItem();
        item.setAmount(1);

        if (itemInventories.containsKey(item)){
            HashMap<Integer, Inventory> inv = itemInventories.get(item);
            event.getWhoClicked().openInventory(inv.get(0));
            return;
        }

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



        if (event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)){
            if(getShopName(event.getCurrentItem()).equals("null")){
                return;
            }

            if (getShopName(event.getCurrentItem()).equals("NextPage") || getShopName(event.getCurrentItem()).equals("PreviousPage")) {
                int newPage = getShopName(event.getCurrentItem()).equals("NextPage") ? Integer.parseInt(getData(event.getClickedInventory().getItem(49), "currentPage")) + 1 : Integer.parseInt(getData(event.getClickedInventory().getItem(49), "currentPage")) - 1;
                switch (getData(event.getClickedInventory().getItem(49), "inventoryType")){
                    case "admin":
                        event.getWhoClicked().openInventory(allAdminShops.get(newPage));
                        break;
                    case "player":
                        event.getWhoClicked().openInventory(allPlayerShops.get(newPage));
                        break;
                    case "adminItems":
                        event.getWhoClicked().openInventory(allAdminItems.get(newPage));
                        break;
                    case "playerItems":
                        event.getWhoClicked().openInventory(allPlayerItems.get(newPage));
                        break;
                }
            }

            if (!getShopName(event.getCurrentItem()).isEmpty() || !getShopName(event.getCurrentItem()).equalsIgnoreCase("null")) {
                Inventory open;
                switch (getShopName(event.getCurrentItem())) {
                    case "AdminSelUI":
                        debugLog("Opening Admin Section");
                        open = selectAdminShopListType;
                        break;
                    case "PlayerSelUI":
                        debugLog("Opening Player Section");
                        open = selectPlayerShopListType;
                        break;
                    case "AdminShopsUI":
                        debugLog("Opening Admin Shops");
                        open = allAdminShops.get(0);
                        break;
                    case "AdminItemsUI":
                        debugLog("Opening Admin Items Shops");
                        open = allAdminItems.get(0);
                        break;
                    case "PlayerShopsUI":
                        debugLog("Opening Player Shops");
                        open = allPlayerShops.get(0);
                        break;
                    case "PlayerItemsUI":
                        debugLog("Opening Player Items Shops");
                        open = allPlayerItems.get(0);
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
