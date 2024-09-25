package me.w41k3r.shopkeepersaddon.General;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static me.w41k3r.shopkeepersaddon.General.Utils.*;
import static me.w41k3r.shopkeepersaddon.Main.virtualOwner;

public class UIHandler {

    public static Inventory ShopsUI = Bukkit.createInventory(virtualOwner, 27, configData("messages.shops.shops-ui"));
    static Inventory selectPlayerShopListType = Bukkit.createInventory(virtualOwner, 27, configData("messages.shops.player-shops-ui"));
    static Inventory selectAdminShopListType = Bukkit.createInventory(virtualOwner, 27, configData("messages.shops.admin-shops-ui"));
    static HashMap<Integer, Inventory> allPlayerShops = new HashMap<>();
    static HashMap<Integer, Inventory> allPlayerItems = new HashMap<>();
    static HashMap<Integer, Inventory> allAdminShops = new HashMap<>();
    static HashMap<Integer, Inventory> allAdminItems = new HashMap<>();
    static Set<String> PlayerShopsList = new HashSet<>();
    static HashMap<String, ItemStack> heads = new HashMap<>();
    public static Set<ItemStack> playerItems = new HashSet<>();
    public static Set<ItemStack> adminItems = new HashSet<>();

    // List of inventories or pages for each item
    static HashMap<ItemStack, HashMap<Integer, Inventory>> itemInventories  = new HashMap<>();


    static void addShopstoUI(Map<String, Object> obj) {
        switch (obj.get("type").toString().toLowerCase()) {
            case "admin":
                debugLog("Adding admin shop to UI");
                Inventory adminPage = getOrCreatePage(allAdminShops, "admin");
//                 ItemStack adminIcon = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
                Map<String, Object> adminShopkeeper = (Map<String, Object>) obj.get("object");
                // This needs to be updated in the future to have custom texture for each type of shopkeeper and varients
                ItemStack adminIcon = getIcon(obj.get("uniqueId").toString(), "adminshop", ((Map<?, ?>) obj.get("object")).get("type").toString());
                adminPage.addItem(adminIcon);
                allAdminShops.put(allAdminShops.size() - 1, adminPage);
                break;
            default:
                debugLog("Adding player shop to UI");
                if (PlayerShopsList.contains(obj.get("owner"))) {
                    debugLog("PlayerShopsUI already contains " + obj.get("owner") + "'s Shop");
                    break;
                }
                Inventory playerPage = getOrCreatePage(allPlayerShops, "player");
                ItemStack playerIcon = getIcon(obj.get("uniqueId").toString(), "playershop", ((Map<?, ?>) obj.get("object")).get("type").toString());
                playerPage.addItem(playerIcon);
                allPlayerShops.put(allPlayerShops.size() - 1, playerPage);
                PlayerShopsList.add((String) obj.get("owner"));
                debugLog("Added " + obj.get("owner") + "'s Shop to PlayerShopsUI" + " Page: " + (allPlayerShops.size() - 1));
                break;
        }

    }

    static void addItemsToUI(Map<String, Object> obj) {
        Map<String, Object> recipes = (Map<String, Object>) (obj.containsKey("recipes") ? obj.get("recipes") : obj.get("offers"));
        for (Map.Entry<String, Object> entry : recipes.entrySet()) {
            Map<String, Object> recipeData = (Map<String, Object>) entry.getValue();
            ItemStack item = createItemStackFromYaml((Map<String, Object>) recipeData.get("resultItem"));

            if (item == null) {
                debugLog("Failed to create ItemStack from YAML");
                continue;
            }

            item.setAmount(1);
            ItemStack temp = item.clone();
            temp.setAmount(1);

            if (adminItems.contains(temp) || playerItems.contains(temp)) {
                debugLog("Item already exists in Items List");
                continue;
            }

            Inventory page;
            String type = obj.get("type").toString().toLowerCase();
            String uniqueId = obj.get("uniqueId").toString();
            String itemType = ((Map<?, ?>) obj.get("object")).get("type").toString();

            switch (type) {
                case "admin":
                    page = getOrCreatePage(allAdminItems, "adminItems");
                    allAdminItems.put(allAdminItems.size() - 1, page);
                    adminItems.add(item);

                    HashMap<Integer, Inventory> allAdminShopkeepers = itemInventories.getOrDefault(item, new HashMap<>());
                    Inventory adminPage = getOrCreatePage(allAdminShopkeepers, "adminItems");
                    ItemStack adminIcon = getIcon(uniqueId, "adminshop", itemType);
                    adminPage.addItem(adminIcon);
                    allAdminShopkeepers.put(allAdminShopkeepers.size() - 1, adminPage);
                    itemInventories.put(item, allAdminShopkeepers);
                    break;

                default:
                    page = getOrCreatePage(allPlayerItems, "playerItems");
                    allPlayerItems.put(allPlayerItems.size() - 1, page);
                    playerItems.add(item);

                    HashMap<Integer, Inventory> allPlayerShopkeepers = itemInventories.getOrDefault(item, new HashMap<>());
                    ItemStack playerIcon = getIcon(uniqueId, "playershop", itemType);
                    Inventory playerPage = getOrCreatePage(allPlayerShopkeepers, "playerItems");
                    playerPage.addItem(playerIcon);
                    allPlayerShopkeepers.put(allPlayerShopkeepers.size() - 1, playerPage);
                    itemInventories.put(item, allPlayerShopkeepers);
                    break;
            }

            page.addItem(item);
        }
    }




    static Inventory getOrCreatePage(HashMap<Integer, Inventory> ShopsUI, String type) {
        if (ShopsUI == null){
            ShopsUI = new HashMap<>();
        }
        int currentPage = ShopsUI.size() - 1;
        Inventory page = ShopsUI.get(currentPage);

        if (page == null || page.firstEmpty() == -1) {
            if (page != null) {
                debugLog("Adding Next Page Button to " + type + " shop at Page: " + currentPage);
                addNextPageButton(ShopsUI, currentPage);
            }
            currentPage++;

            switch (type){
                case "player":
                    debugLog("Creating new PlayerShopsUI Page: " + currentPage);
                    page = Bukkit.createInventory(virtualOwner, 54, configData("messages.shops.player-shops-ui") + " - Page " + (currentPage + 1));
                    setLastRowAsStainedGlassPane(page, currentPage, type);
                    ShopsUI.put(currentPage, page);
                    break;
                case "admin":
                    debugLog("Creating new AdminShopsUI Page: " + currentPage);
                    page = Bukkit.createInventory(virtualOwner, 54, configData("messages.shops.admin-shops-ui") + " - Page " + (currentPage + 1));
                    setLastRowAsStainedGlassPane(page, currentPage, type);
                    ShopsUI.put(currentPage, page);
                    break;
                case "playerItems":
                    debugLog("Creating new PlayerItemsUI Page: " + currentPage);
                    page = Bukkit.createInventory(virtualOwner, 54, configData("messages.shops.player-items-ui") + " - Page " + (currentPage + 1));
                    setLastRowAsStainedGlassPane(page, currentPage, type);
                    ShopsUI.put(currentPage, page);
                    break;
                case "adminItems":
                    debugLog("Creating new AdminItemsUI Page: " + currentPage);
                    page = Bukkit.createInventory(virtualOwner, 54, configData("messages.shops.admin-items-ui") + " - Page " + (currentPage + 1));
                    setLastRowAsStainedGlassPane(page, currentPage, type);
                    ShopsUI.put(currentPage, page);
                    break;
                case "playersList":
                    debugLog("Creating new PlayersList Page: " + currentPage);
                    page = Bukkit.createInventory(virtualOwner, 54, configData("messages.shops.players-list-ui") + " - Page " + (currentPage + 1));
                    setLastRowAsStainedGlassPane(page, currentPage, type);
                    ShopsUI.put(currentPage, page);
                    break;
                default:
                    break;
            }

            return page;
        }
        return page;
    }

    private static void setLastRowAsStainedGlassPane(Inventory inventory, int currentPage, String type) {
        ItemStack stainedGlassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta stainedGlassPaneMeta = stainedGlassPane.getItemMeta();
        stainedGlassPaneMeta.setDisplayName(" ");
        stainedGlassPane.setItemMeta(stainedGlassPaneMeta);
        for (int i = 45; i < 54; i++) {
            if (i == 45 && currentPage > 0) {
                ItemStack previousPageButton = getCustomHead("PreviousPage", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjllYTFkODYyNDdmNGFmMzUxZWQxODY2YmNhNmEzMDQwYTA2YzY4MTc3Yzc4ZTQyMzE2YTEwOThlNjBmYjdkMyJ9fX0=");
                ItemMeta previousPageButtonMeta = previousPageButton.getItemMeta();
                previousPageButtonMeta.setDisplayName(configData("messages.previous-page-button"));
                previousPageButton.setItemMeta(previousPageButtonMeta);
                inventory.setItem(i, previousPageButton);
                continue;
            }
            if (i == 49) {
                ItemStack closeInventoryButton = new ItemStack(Material.BARRIER, 1);
                ItemMeta closeInventoryButtonMeta = closeInventoryButton.getItemMeta();
                closeInventoryButtonMeta.setDisplayName(configData("messages.close-inventory-button"));
                closeInventoryButtonMeta = setData(closeInventoryButtonMeta, "closeInventory", "true");
                closeInventoryButtonMeta = setData(closeInventoryButtonMeta, "currentPage", String.valueOf(currentPage));
                closeInventoryButtonMeta = setData(closeInventoryButtonMeta, "inventoryType", type);
                closeInventoryButton.setItemMeta(closeInventoryButtonMeta);
                inventory.setItem(i, closeInventoryButton);
                continue;
            }
            inventory.setItem(i, stainedGlassPane);
        }
    }

    static void addNextPageButton(Map<Integer, Inventory> ShopsUI,int currentPage) {
        ItemStack nextPageButton = getCustomHead("NextPage", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI3MWE0NzEwNDQ5NWUzNTdjM2U4ZTgwZjUxMWE5ZjEwMmIwNzAwY2E5Yjg4ZTg4Yjc5NWQzM2ZmMjAxMDVlYiJ9fX0=");
        ItemMeta nextPageButtonMeta = nextPageButton.getItemMeta();
        nextPageButtonMeta.setDisplayName(configData("messages.next-page-button"));
        nextPageButton.setItemMeta(nextPageButtonMeta);
        ShopsUI.get(currentPage).setItem(53, nextPageButton);
    }

    static void createShopsUI() {
        ItemStack playerShopSelectionIcon = getCustomHead("PlayerSelUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFiZTRiNWI1OTJmZWUyMWE2NWUwZjkwMzAzOGM1MzMzYmUzODgyMzRhNDM3MzFkNGFkZmU1ZDU3ZDM2NDRlNSJ9fX0=");
        ItemMeta playerShopSelectionIconMeta = playerShopSelectionIcon.getItemMeta();
        playerShopSelectionIconMeta.setDisplayName(configData("messages.shops.player-shops-ui"));
        playerShopSelectionIcon.setItemMeta(playerShopSelectionIconMeta);
        ShopsUI.setItem(11, playerShopSelectionIcon);

        ItemStack adminShopSelectionIcon = getCustomHead("AdminSelUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI3ODQzMDdiODkyZjUyYjkyZjc0ZmE5ZGI0OTg0YzRmMGYwMmViODFjNjc1MmU1ZWJhNjlhZDY3ODU4NDI3ZSJ9fX0=");
        ItemMeta adminShopSelectionIconMeta = adminShopSelectionIcon.getItemMeta();
        adminShopSelectionIconMeta.setDisplayName(configData("messages.shops.admin-shops-ui"));
        adminShopSelectionIcon.setItemMeta(adminShopSelectionIconMeta);
        ShopsUI.setItem(15, adminShopSelectionIcon);
    }

    static void selectionUIs() {
        ItemStack playerShopsIcon = getCustomHead("PlayerShopsUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThjZGRjZmY0NDBlMjc5MmQ5OTE0ZTFkZDExN2ZhODJiZDg1OWIyOWVkMzlkZGRlMzQ1MzU0Mzg2YjAyYjc3NiJ9fX0=");
        ItemMeta playerShopsIconMeta = playerShopsIcon.getItemMeta();
        playerShopsIconMeta.setDisplayName(configData("messages.shops.player-shops-ui"));
        playerShopsIcon.setItemMeta(playerShopsIconMeta);
        selectPlayerShopListType.setItem(11, playerShopsIcon);
        ItemStack playerItemsIcon = getCustomHead("PlayerItemsUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU0Nzg0ZmI0NzM0Nzg0NzQzYmYzMDI2NDlmYjU3MDAxNzFlYWY2Njc4NjFhYjJiMjVjZTlhZWU3YTEzODE2MCJ9fX0=");
        ItemMeta playerItemsIconMeta = playerItemsIcon.getItemMeta();
        playerItemsIconMeta.setDisplayName(configData("messages.shops.players-item-shops-ui"));
        playerItemsIcon.setItemMeta(playerItemsIconMeta);
        selectPlayerShopListType.setItem(15, playerItemsIcon);



        ItemStack adminShopsIcon = getCustomHead("AdminShopsUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFmNzE0MTMzY2U3OGQxMTgxYzRkNWQzZTUzNzExZWNlMTBjNGM5YTI4MjAxMTg4ZWUxYTZmMzVjYzBmYTNjYSJ9fX0=");
        ItemMeta adminShopsIconMeta = adminShopsIcon.getItemMeta();
        adminShopsIconMeta.setDisplayName(configData("messages.shops.admin-shops-ui"));
        adminShopsIcon.setItemMeta(adminShopsIconMeta);
        selectAdminShopListType.setItem(11, adminShopsIcon);
        ItemStack adminItemsIcon = getCustomHead("AdminItemsUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU0Nzg0ZmI0NzM0Nzg0NzQzYmYzMDI2NDlmYjU3MDAxNzFlYWY2Njc4NjFhYjJiMjVjZTlhZWU3YTEzODE2MCJ9fX0=");
        ItemMeta adminItemsIconMeta = adminItemsIcon.getItemMeta();
        adminItemsIconMeta.setDisplayName(configData("messages.shops.admin-item-shops-ui"));
        adminItemsIcon.setItemMeta(adminItemsIconMeta);
        selectAdminShopListType.setItem(15, adminItemsIcon);

    }

    static void initializeUIs() {
        allPlayerShops.clear();
        allAdminShops.clear();
        allPlayerItems.clear();
        allAdminItems.clear();
        PlayerShopsList.clear();
        playerItems.clear();
        adminItems.clear();
        itemInventories.clear();
        Inventory page = getOrCreatePage(allPlayerShops, "player");
        allPlayerShops.put(0, page);
        page = getOrCreatePage(allAdminShops, "admin");
        allAdminShops.put(0, page);
        page = getOrCreatePage(allPlayerItems, "playerItems");
        allPlayerItems.put(0, page);
        page = getOrCreatePage(allAdminItems, "adminItems");
        allAdminItems.put(0, page);
    }
}



