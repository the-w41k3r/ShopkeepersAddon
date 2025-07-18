package me.w41k3r.shopkeepersAddon.gui;

import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import me.w41k3r.shopkeepersAddon.gui.objects.VirtualInventoryOwner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.UUID;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.config;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.gui.BaseGUIHandlers.getFillerItem;
import static me.w41k3r.shopkeepersAddon.gui.init.InitPlayerPages.getPlayerShop;
import static me.w41k3r.shopkeepersAddon.gui.managers.SkinsManager.*;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.setCurrentPage;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.setTarget;
import static org.bukkit.Bukkit.createInventory;

public class ItemsListHandlers {
    public static void openItemOwnersList(ItemStack clickedItem, Player player, String guiName) {
        // Get item display name
        String itemName = getItemDisplayName(clickedItem);

        // Create virtual inventory owner and inventory
        VirtualInventoryOwner virtualInventoryOwner = new VirtualInventoryOwner(guiName + player.getName());
        Inventory inventory = createInventory(virtualInventoryOwner, 54, itemName);

        // Add filler items
        addFillerItems(inventory);

        // Add back button
        ItemStack backButton = createBackButton(guiName, itemName);

        // Add owners based on GUI type
        debugLog("Opening " + guiName.toLowerCase() + " for: " + clickedItem.getType());
        switch (guiName) {
            case "ADMIN_ITEMS_LIST":
                backButton = setCurrentPage(backButton, "ADMIN_SHOPS_LIST");
                addAdminShopOwners(inventory, clickedItem);
                break;
            case "PLAYER_ITEMS_LIST":
                backButton = setCurrentPage(backButton, "PLAYER_SHOPS_LIST");
                addPlayerShopOwners(inventory, clickedItem);
                break;
            default:
                debugLog("Unknown GUI type: " + guiName);
                return;
        }
        inventory.setItem(BACK_BUTTON_SLOT, backButton);

        // Set current item and open inventory
        inventory.setItem(CURR_ITEM_SLOT, setTarget(clickedItem, "ignore"));
        player.openInventory(inventory);
    }

    private static String getItemDisplayName(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name();
    }

    private static void addFillerItems(Inventory inventory) {
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, getFillerItem(config.getString("adminShop.filler")));
        }
    }

    private static ItemStack createBackButton(String guiType, String itemName) {
        ItemStack backButton = getIcon("messages.back",
                config.getString("heads.back"),
                guiType, null);
        debugLog("backButton: " + guiType);
        return setCurrentPage(backButton, itemName);
    }

    private static void addAdminShopOwners(Inventory inventory, ItemStack clickedItem) {
        ArrayList<UUID> owners = adminItemOwners.get(clickedItem);
        if (owners == null) {
            debugLog("No admin owners found for this item");
            return;
        }

        for (UUID adminShopkeeperUUID : owners) {
            AdminShopkeeper adminShopkeeper = (AdminShopkeeper) registry.getShopkeeperByUniqueId(adminShopkeeperUUID);
            if (adminShopkeeper == null) continue;

            String displayName = config.getString("messages.adminShops.buttons.title")
                    .replace("%shop_name%", adminShopkeeper.getDisplayName()).replace(" ", "_");

            ItemStack shopIcon = getIcon("messages.adminShops.buttons",
                    config.getString("heads.adminShops.Shops"),
                    displayName,
                    adminShopkeeper.getDisplayName());
            shopIcon = setTarget(shopIcon, adminShopkeeper.getUniqueId().toString());

            if (!inventory.contains(shopIcon)) {
                inventory.addItem(shopIcon);
            }
        }
    }

    private static void addPlayerShopOwners(Inventory inventory, ItemStack clickedItem) {
            ArrayList<UUID> owners = playerItemOwners.get(clickedItem);
            if (owners == null) {
                debugLog("No player owners found for this item");
                return;
            }

            for (UUID playerShopkeeperUUID : owners) {
                PlayerShopkeeper playerShopkeeper = (PlayerShopkeeper) registry.getShopkeeperByUniqueId(playerShopkeeperUUID);
                if (playerShopkeeper == null) continue;

                String ownerName = playerShopkeeper.getOwnerName();
                UUID ownerUUID = playerShopkeeper.getOwnerUUID();
                String displayName = config.getString("messages.playerShops.buttons.title")
                        .replace("%player_name%", ownerName);

                YamlConfiguration shopConfig = getPlayerShop(ownerUUID.toString());
                ItemStack shopIcon = createPlayerShopIcon(ownerName, ownerUUID, displayName,
                        shopConfig == null? null : shopConfig.getStringList("shopName"));

                shopIcon = setTarget(shopIcon, ownerUUID.toString());
                if (!inventory.contains(shopIcon)) {
                    inventory.addItem(shopIcon);
                }
            }
        }


}